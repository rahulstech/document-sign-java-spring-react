package com.github.rahulstech.document_sign.service.pdfservice;

import com.github.rahulstech.document_sign.service.pdfservice.PdfService.Bound;
import com.github.rahulstech.document_sign.service.pdfservice.PdfService.Annotation;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PdfServiceTest {

    private PdfService pdfService;

    private byte[] mockPdfBytes;
    private byte[] mockImageBytes;

    @TempDir
    File tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        // Since createAnnotatedPdf does not use storageService, we can pass null.
        pdfService = new PdfService(null);
        mockPdfBytes = createMockPdf(2); // 2 pages
        mockImageBytes = createMockImage();
    }

    @Test
    public void testSingleSignatureSigningAndIdAttachment() throws Exception {
        File documentFile = new File(tempDir, "original.pdf");
        Files.write(documentFile.toPath(), mockPdfBytes);

        File signatureFile = new File(tempDir, "sig.png");
        Files.write(signatureFile.toPath(), mockImageBytes);

        UUID sigId = UUID.randomUUID();
        Annotation annotation = new Annotation(sigId.toString(), 1, new Bound(10.0f, 20.0f, 15.0f, 10.0f), signatureFile);

        File signedFile = pdfService.createAnnotatedPdf(documentFile, List.of(annotation));

        assertNotNull(signedFile);
        assertTrue(signedFile.exists());
        assertEquals("signed.pdf", signedFile.getName());
        assertEquals(tempDir, signedFile.getParentFile());

        // Verify the content of the signed PDF
        try (PDDocument doc = Loader.loadPDF(signedFile)) {
            assertEquals(2, doc.getNumberOfPages());

            // Extract text and verify signature ID is present
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            
            // Normalize spaces/newlines to handle line breaks in ID text wrapping
            String normalizedText = text.replaceAll("\\s+", "");
            String normalizedSigId = sigId.toString().replaceAll("\\s+", "");
            
            assertTrue(normalizedText.contains(normalizedSigId), 
                    "Signed PDF should contain the signature ID: " + sigId);

            // Verify image is drawn on Page 1 (index 0)
            PDPage page1 = doc.getPage(0);
            int imagesOnPage1 = countImagesOnPage(page1);
            assertEquals(1, imagesOnPage1, "Page 1 should have exactly 1 signature image");

            // Page 2 (index 1) should have 0 images
            PDPage page2 = doc.getPage(1);
            int imagesOnPage2 = countImagesOnPage(page2);
            assertEquals(0, imagesOnPage2, "Page 2 should have 0 signature images");
        }
    }

    @Test
    public void testMultipleSignaturesOnMultiplePages() throws Exception {
        File documentFile = new File(tempDir, "original.pdf");
        Files.write(documentFile.toPath(), mockPdfBytes);

        File signatureFile = new File(tempDir, "sig.png");
        Files.write(signatureFile.toPath(), mockImageBytes);

        UUID sigId1 = UUID.randomUUID();
        UUID sigId2 = UUID.randomUUID();
        UUID sigId3 = UUID.randomUUID();

        // Annotation 1 on Page 1
        Annotation annotation1 = new Annotation(sigId1.toString(), 1, new Bound(10.0f, 20.0f, 15.0f, 10.0f), signatureFile);
        // Annotation 2 on Page 1
        Annotation annotation2 = new Annotation(sigId2.toString(), 1, new Bound(50.0f, 40.0f, 20.0f, 15.0f), signatureFile);
        // Annotation 3 on Page 2
        Annotation annotation3 = new Annotation(sigId3.toString(), 2, new Bound(30.0f, 30.0f, 25.0f, 12.0f), signatureFile);

        File signedFile = pdfService.createAnnotatedPdf(documentFile, List.of(annotation1, annotation2, annotation3));

        assertNotNull(signedFile);
        assertTrue(signedFile.exists());

        // Verify the content of the signed PDF
        try (PDDocument doc = Loader.loadPDF(signedFile)) {
            assertEquals(2, doc.getNumberOfPages());

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            String normalizedText = text.replaceAll("\\s+", "");

            // Verify all IDs are present in the PDF text
            assertTrue(normalizedText.contains(sigId1.toString().replaceAll("\\s+", "")));
            assertTrue(normalizedText.contains(sigId2.toString().replaceAll("\\s+", "")));
            assertTrue(normalizedText.contains(sigId3.toString().replaceAll("\\s+", "")));

            // Page 1 should have 2 signature images
            PDPage page1 = doc.getPage(0);
            assertEquals(2, countImagesOnPage(page1), "Page 1 should have exactly 2 signature images");

            // Page 2 should have 1 signature image
            PDPage page2 = doc.getPage(1);
            assertEquals(1, countImagesOnPage(page2), "Page 2 should have exactly 1 signature image");
        }
    }

    private byte[] createMockPdf(int pageCount) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < pageCount; i++) {
                doc.addPage(new PDPage());
            }
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                doc.save(bos);
                return bos.toByteArray();
            }
        }
    }

    private byte[] createMockImage() throws IOException {
        var image = new java.awt.image.BufferedImage(100, 50, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var g2d = image.createGraphics();
        g2d.setColor(java.awt.Color.BLUE);
        g2d.fillRect(0, 0, 100, 50);
        g2d.dispose();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        }
    }

    private int countImagesOnPage(PDPage page) throws IOException {
        int count = 0;
        var resources = page.getResources();
        if (resources != null) {
            for (org.apache.pdfbox.cos.COSName name : resources.getXObjectNames()) {
                var xobject = resources.getXObject(name);
                if (xobject instanceof PDImageXObject) {
                    count++;
                }
            }
        }
        return count;
    }
}
