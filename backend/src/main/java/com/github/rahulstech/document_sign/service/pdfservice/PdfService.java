package com.github.rahulstech.document_sign.service.pdfservice;

import com.github.rahulstech.document_sign.service.storageservice.StorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    // Signature background color. Apply signature text Background color so that signature text
    // do not hide in pdf content, and it is cleary readable.
    private static final Color PRIMARY_COLOR = new Color(0xE0, 0x3C, 0x31);
    // Text on background
    private static final Color TEXT_ON_PRIMARY = Color.WHITE;

    private static final float MIN_FONT_SIZE = 10f;
    private static final float LABEL_PADDING = 4f;

    private final StorageService storageService;

    /**
     * Creates a signature-annotated PDF by downloading the original document and
     * all signature images concurrently on background IO threads, then overlaying
     * signature images with ID labels, and uploading the result to S3.
     * If any download fails, the entire operation fails.
     */
    public String createSignatureAnnotatedPdf(PdfData data) {
        var documentUrl = data.documentUrl();
        var signatures = data.signatures();
        var root = getTempDir();

        try (ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Download all files concurrently on virtual IO threads
            var documentFileFuture = CompletableFuture.supplyAsync(
                    () -> downloadFileUnchecked(root, documentUrl), ioExecutor
            );

            // Maintain insertion order so signatures are annotated in the original sequence
            Map<PdfData.Signature, CompletableFuture<File>> signatureFileFutures = new LinkedHashMap<>();
            for (var signature : signatures) {
                var future = CompletableFuture.supplyAsync(
                        () -> downloadFileUnchecked(root, signature.url()), ioExecutor
                );
                signatureFileFutures.put(signature, future);
            }


            var documentFile = documentFileFuture.join();

            // Build annotations list with percentage layout info
            List<Annotation> annotations = new ArrayList<>();
            for (var entry : signatureFileFutures.entrySet()) {
                var signature = entry.getKey();
                var signatureFile = entry.getValue().join();
                annotations.add(new Annotation(
                        signature.id().toString(),
                        signature.pageNumber(),
                        new Bound(
                                signature.left().floatValue(),
                                signature.top().floatValue(),
                                signature.width().floatValue(),
                                signature.height().floatValue()
                        ),
                        signatureFile
                ));
            }

            // Annotate PDF sequentially (PDDocument is not thread-safe)
            File signedDocumentFile = createAnnotatedPdf(documentFile, annotations);

            // Upload the annotated PDF to S3
            String signedUrl = uploadSignedPdf(signedDocumentFile, documentUrl);
            log.debug("Uploaded annotated PDF to S3: {}", signedUrl);

            return signedUrl;
        }
        catch (Exception e) {
            // TODO: may be more precise error message
            throw new RuntimeException("create signature annotated pdf failed with exception", e);
        }
        finally {
            deleteDirectory(root);
        }
    }

    /**
     * Annotates a local PDF document with signature images and ID labels sequentially,
     * saving the result to a signed PDF file in the same directory.
     *
     * @param documentFile The original unsigned PDF file.
     * @param annotations  The list of annotations to apply.
     * @return The local signed PDF file.
     * @throws Exception if loading, drawing, or saving fails.
     */
    File createAnnotatedPdf(File documentFile, List<Annotation> annotations) throws Exception {
        var signedDocumentFile = new File(documentFile.getParentFile(), "signed.pdf");

        try (var document = Loader.loadPDF(documentFile)) {
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            for (var annotation : annotations) {
                var pageIndex = annotation.pageNumber() - 1;
                var page = document.getPage(pageIndex);
                var bound = calculateSignatureBound(page, annotation.bound());

                try (var content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    // Draw signature image
                    drawImage(document, content, annotation.signatureFile(), bound);

                    // Draw signature ID label below the image
                    drawText(content, annotation.signatureId(), font, bound);
                }
            }

            // save document to temp output file to avoid corruption
            document.save(signedDocumentFile, CompressParameters.NO_COMPRESSION);
            log.debug("Saved signature annotated pdf to {}", signedDocumentFile);
        }

        return signedDocumentFile;
    }

    /**
     * Draws a signature image onto the PDF page at the given bound.
     */
    private void drawImage(
            PDDocument document,
            PDPageContentStream content,
            File source,
            Bound bound
    ) throws IOException {
        var image = PDImageXObject.createFromFileByContent(source, document);
        content.drawImage(image, bound.x, bound.y, bound.width, bound.height);
    }

    /**
     * Draws a text label below the image bound with a semi-transparent primary-color
     * background and white text. Font size is auto-calculated to fit the image width;
     * if the calculated size falls below MIN_FONT_SIZE, the text is wrapped into two lines.
     */
    private void drawText(
            PDPageContentStream content,
            String text,
            PDType1Font font,
            Bound imageBound
    ) throws IOException {
        float fontSize = calculateFontSize(font, text, imageBound.width);
        List<String> lines = generateLines(text, fontSize);

        float lineHeight = fontSize * 1.2f;
        float totalTextHeight = lineHeight * lines.size();
        float bgHeight = totalTextHeight + 2 * LABEL_PADDING;
        float bgY = imageBound.y - bgHeight;
        var textBound = new Bound(imageBound.x, bgY, imageBound.width, bgHeight);

        // Draw background rectangle with 60% opacity primary color
        drawTextBackground(content, textBound);

        // Draw text lines centered within the image width
        drawTextLines(content, font, fontSize, lines, textBound, lineHeight);
    }

    /**
     * Calculates the optimal font size so that the text fits within the available width.
     * Returns MIN_FONT_SIZE if a single-line fit would require a size smaller than that.
     */
    private float calculateFontSize(PDType1Font font, String text, float imageWidth) throws IOException {
        float availableWidth = imageWidth - 2 * LABEL_PADDING;
        if (availableWidth <= 0) availableWidth = imageWidth;

        // font.getStringWidth returns width in thousandths of a point at 1pt
        float widthAtOnePt = font.getStringWidth(text) / 1000f;
        float calculated = availableWidth / widthAtOnePt;

        return Math.max(calculated, MIN_FONT_SIZE);
    }

    /**
     * Splits the text into lines. If the calculated font size equals MIN_FONT_SIZE
     * (meaning one line doesn't fit), the text is split into two lines at the nearest
     * hyphen for UUID readability. Otherwise, returns a single-line list.
     */
    private List<String> generateLines(String text, float fontSize) {
        List<String> lines = new ArrayList<>();

        if (fontSize > MIN_FONT_SIZE) {
            // Single line fits comfortably
            lines.add(text);
        } else {
            // Split into two lines at a hyphen near the middle
            int midpoint = text.length() / 2;

            int splitAt = text.indexOf('-', midpoint);
            if (splitAt < 0 || splitAt == text.length() - 1) {
                splitAt = text.lastIndexOf('-', midpoint);
            }
            if (splitAt <= 0) {
                splitAt = midpoint; // fallback: split at exact middle
            } else {
                splitAt += 1; // include the hyphen on the first line
            }

            lines.add(text.substring(0, splitAt));
            lines.add(text.substring(splitAt));
        }

        return lines;
    }

    /**
     * Draws a semi-transparent primary-color background rectangle for the text label.
     */
    private void drawTextBackground(
            PDPageContentStream content,
            Bound bound
    ) throws IOException {
        PDExtendedGraphicsState transparentState = new PDExtendedGraphicsState();
        transparentState.setNonStrokingAlphaConstant(0.6f);
        content.setGraphicsStateParameters(transparentState);

        content.setNonStrokingColor(PRIMARY_COLOR);
        content.addRect(bound.x, bound.y, bound.width, bound.height);
        content.fill();

        // Reset to full opacity for subsequent drawing
        PDExtendedGraphicsState opaqueState = new PDExtendedGraphicsState();
        opaqueState.setNonStrokingAlphaConstant(1.0f);
        content.setGraphicsStateParameters(opaqueState);
    }

    /**
     * Renders the text lines centered horizontally within the background area.
     */
    private void drawTextLines(
            PDPageContentStream content,
            PDType1Font font,
            float fontSize,
            List<String> lines,
            Bound bound,
            float lineHeight
    ) throws IOException {
        content.setNonStrokingColor(TEXT_ON_PRIMARY);
        content.setFont(font, fontSize);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float textWidth = font.getStringWidth(line) / 1000f * fontSize;
            float textX = bound.x + (bound.width - textWidth) / 2f;
            float textY = bound.y + bound.height - LABEL_PADDING
                    - lineHeight * (i + 1)
                    + (lineHeight - fontSize) / 2f
                    + fontSize * 0.2f;

            content.beginText();
            content.newLineAtOffset(textX, textY);
            content.showText(line);
            content.endText();
        }
    }

    /**
     * Creates and returns a unique temporary directory for working files.
     */
    private File getTempDir() {
        var dir = new File("temp", UUID.randomUUID().toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Converts percentage-based signature placement into absolute PDF page coordinates.
     */
    private Bound calculateSignatureBound(PDPage page, Bound pctBound) {
        var mediaBox = page.getMediaBox();
        var pageWidth = mediaBox.getWidth();
        var pageHeight = mediaBox.getHeight();
        var x = pctBound.x() * pageWidth / 100;
        var y = (100 - pctBound.y() - pctBound.height()) * pageHeight / 100;
        var width = pctBound.width() * pageWidth / 100;
        var height = pctBound.height() * pageHeight / 100;
        var bound = new Bound(x, y, width, height);

        log.debug("pageSize(WxH) = ({}, {}) pctBound = {}, bound = {}",  pageWidth, pageHeight, pctBound, bound);

        return bound;
    }

    /**
     * Unchecked wrapper around {@link #downloadFile} for use in CompletableFuture lambdas.
     * Converts checked exceptions to RuntimeException so failures propagate through join().
     */
    private File downloadFileUnchecked(File root, String url) {
        try {
            return downloadFile(root, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download: " + url, e);
        }
    }

    /**
     * Downloads a file from the given URL into the specified root directory.
     */
    private File downloadFile(File root, String url) throws Exception {
        log.info("Downloading file from URL: {}", url);
        String fileName = getLastPathSegment(url);
        var destination = new File(root, fileName);
        storageService.download(url, destination);
        log.info("Downloaded file successfully to: {}", destination);
        return destination;
    }

    /**
     * Extracts the last path segment of the given URL to use as the file name.
     */
    private String getLastPathSegment(@NonNull String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path != null && !path.isBlank()) {
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0) {
                    String segment = path.substring(lastSlash + 1);
                    if (!segment.isBlank()) {
                        return segment;
                    }
                } else if (!path.isBlank()) {
                    return path;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse URL/URI path segment: {}, using fallback", url, e);
        }
        // Fallback for edge cases where URI parsing is strict or fails
        try {
            int queryIdx = url.indexOf('?');
            String cleanUrl = queryIdx >= 0 ? url.substring(0, queryIdx) : url;
            int hashIdx = cleanUrl.indexOf('#');
            cleanUrl = hashIdx >= 0 ? cleanUrl.substring(0, hashIdx) : cleanUrl;
            if (cleanUrl.endsWith("/")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
            }
            int lastSlash = cleanUrl.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < cleanUrl.length() - 1) {
                String segment = cleanUrl.substring(lastSlash + 1);
                if (!segment.isBlank()) {
                    return segment;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "downloaded_file";
    }

    /**
     * Recursively deletes a directory and all its contents.
     */
    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }



    /**
     * Extracts the directory and original name from the original PDF URL,
     * prefixes the file name with "signed_", and uploads the signed PDF file to S3
     * under the same folder.
     *
     * @param signedPdf      The local signed PDF file to upload.
     * @param originalPdfUrl The original PDF's S3/public URL.
     * @return The public CDN/S3 URL of the uploaded signed PDF.
     */
    private String uploadSignedPdf(File signedPdf, String originalPdfUrl) {
        var uri = URI.create(originalPdfUrl);
        var path = uri.getRawPath();
        var lastIndex = path.lastIndexOf("/");
        var dir = path.substring(0, lastIndex);
        var fileName = path.substring(lastIndex+1);
        var signedFileName = "signed_" + fileName;
        return storageService.upload(signedPdf, "application/pdf", signedFileName, dir);
    }
    /**
     * Represents an absolute rectangular region on a PDF page (x, y, width, height in points).
     */
    record Bound(
            float x,
            float y,
            float width,
            float height
    ) {}

    /**
     * Represents a signature annotation package to be drawn on a specific page of a PDF document.
     */
    record Annotation(
            String signatureId,
            int pageNumber,
            Bound bound,
            File signatureFile
    ) {}
}
