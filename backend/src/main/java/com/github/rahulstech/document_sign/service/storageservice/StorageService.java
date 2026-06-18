package com.github.rahulstech.document_sign.service.storageservice;

import com.github.rahulstech.document_sign.dto.CreateUploadUrlRequest;
import com.github.rahulstech.document_sign.dto.CreateUploadUrlResponse;
import com.github.rahulstech.document_sign.exception.HttpException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger("UploadServiceProdImpl");

    private static final long SIGNATURE_DURATION_SECONDS = 24 * 3600; // 24 hours

    private static final HashMap<String, String> CONTENT_TYPE_TO_EXTENSION = new HashMap<>();

    static {
        CONTENT_TYPE_TO_EXTENSION.put("application/pdf", ".pdf");
        CONTENT_TYPE_TO_EXTENSION.put("image/jpg", ".jpg");
        CONTENT_TYPE_TO_EXTENSION.put("image/jpeg", ".jpeg");
        CONTENT_TYPE_TO_EXTENSION.put("image/png", ".png");
        CONTENT_TYPE_TO_EXTENSION.put("image/webp", ".webp");
    }

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_S3_TEMP_DIR}")
    private String tempDir;

    @Value("${AWS_S3_PUBLIC_DIR}")
    private String publicDir;

    @Value("${CDN_BASE_URL}")
    private String cdnBaseUrl;

    private final S3Client client;
    private final S3Presigner presigner;


    /**
     * Generates a pre-signed S3 URL for direct client upload to the temporary storage location.
     */
    public CreateUploadUrlResponse createUploadUrl(CreateUploadUrlRequest req) {
        var key = createTemporaryKey();
        var cmd = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(req.type())
                .contentLength(req.size())
                .build();

        var o = presigner.presignPutObject(builder ->
                builder.putObjectRequest(cmd)
                        .signatureDuration(Duration.ofSeconds(SIGNATURE_DURATION_SECONDS))
        );

        var url = o.url().toString();

        return new CreateUploadUrlResponse(url, key);
    }

    /**
     * Finalizes a file upload by copying it from temporary storage to the public directory
     * and returns the final public URL, content type, and content length.
     */
    public SaveUploadResult saveUpload(String srcKey, String... prefixes) {
        var info = getFileInfo(srcKey);
        var contentType = (String) info.get("content-type");
        var contentLength = (Long) info.get("content-length");
        var ext = getExtensionName(contentType);
        var publicKey = createRandomPublicKey(ext, prefixes);
        var destKey = createDestinationKey(publicKey);
        var publicUrl = createPublicUrl(publicKey);

        // copy from temp to public directory
        copy(srcKey, destKey);

        return new SaveUploadResult(publicUrl, contentType, contentLength);
    }

    /**
     * Directly uploads a local file to S3 in the private directory and returns its CDN public URL.
     */
    public String upload(File file, String contentType, String suffix, String... prefixes) {
        var publicKey = createPublicKey(suffix, prefixes);
        var destKey = createDestinationKey(publicKey);
        var publicUrl = createPublicUrl(publicKey);

        logger.info("Uploading file to S3: bucket={}, key={}, file={}", bucketName, destKey, file.getAbsolutePath());

        var putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(destKey)
                .contentType(contentType)
                .contentLength(file.length())
                .build();

        client.putObject(putReq, file.toPath());

        return publicUrl;
    }

    /**
     * Downloads a file from the S3 storage to a local destination file.
     */
    public void download(String url, File destination) {
         String key = getS3KeyFromUrl(url);
         logger.info("Downloading file from S3: bucket={}, key={}, destination={}", bucketName, key, destination.getAbsolutePath());
         var getReq = GetObjectRequest.builder()
                  .bucket(bucketName)
                  .key(key)
                  .build();
         client.getObject(getReq, destination.toPath());
    }

    /**
     * Generates a temporary (5 minutes validity) pre-signed download URL for a given S3 key.
     *
     * @param key The S3 object key.
     * @return The pre-signed download URL.
     */
    public String getTemporaryDownloadUrl(String key) {
        var getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        var presignedGetReq = presigner.presignGetObject(builder ->
                builder.getObjectRequest(getReq)
                        .signatureDuration(Duration.ofMinutes(5))
        );

        return presignedGetReq.url().toString();
    }

    /**
     * Resolves the S3 object key from a given public URL or raw path.
     */
    public String getS3KeyFromUrl(String url) {
        if (url == null) {
            return null;
        }
        String base = cdnBaseUrl;
        if (base != null && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base != null && url.startsWith(base)) {
            String publicKey = url.substring(base.length());
            if (publicKey.startsWith("/")) {
                publicKey = publicKey.substring(1);
            }
            return publicDir + publicKey;
        }

        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (path != null) {
                String bucketPrefix = "/" + bucketName + "/";
                if (path.startsWith(bucketPrefix)) {
                    return path.substring(bucketPrefix.length());
                }
                if (path.startsWith("/")) {
                    return path.substring(1);
                }
                return path;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse URL: {}", url, e);
        }

        return url;
    }

    /**
     * Retrieves the file extension associated with a content type, defaulting to ".bin".
     */
    public String getExtensionName(String contentType) {
        return CONTENT_TYPE_TO_EXTENSION.getOrDefault(contentType, ".bin");
    }

    /**
     * Generates a temporary path for uploading files.
     */
    private String createTemporaryKey() {
        var suffix = UUID.randomUUID().toString();
        return tempDir + suffix;
    }

    /**
     * Retrieves metadata for an S3 object (e.g. content-type, content-length).
     */
    private Map<String, Object> getFileInfo(String key) {
        var cmd = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        var headRes = client.headObject(cmd);
        var httpRes = headRes.sdkHttpResponse();

        if (!httpRes.isSuccessful()) {
            logger.info("HeadObjectRequest failed with status-code={}", httpRes.statusCode());
            throw HttpException.internalServerError("get upload metadata failed");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("content-length", headRes.contentLength());
        info.put("content-type", headRes.contentType());

        return info;
    }

    /**
     * Copies an object within S3 from a source key to a destination key.
     */
    private void copy(String srcKey, String destKey) {
        var cmd = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(srcKey)
                .destinationBucket(bucketName)
                .destinationKey(destKey)
                .build();
        var httpRes = client.copyObject(cmd).sdkHttpResponse();

        if (!httpRes.isSuccessful()) {
            logger.info("CopyObjectRequest failed with status-code={}", httpRes.statusCode());
            throw HttpException.internalServerError("save upload failed");
        }
    }

    private String createRandomPublicKey(String ext, String... prefixes) {
        var suffix = UUID.randomUUID() + ext;
        return createPublicKey(suffix);
    }

    /**
     * Constructs a public S3 key using optional path prefixes and a unique UUID.
     */
    private String createPublicKey(String suffix, String... prefixes) {
        var prefix = Arrays.stream(prefixes)
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .collect(Collectors.joining("/"));
        if (prefix.isBlank()) {
            return suffix;
        } else {
            return prefix + "/" + suffix;
        }
    }

    /**
     * Generates the destination key within the S3 bucket by appending the public prefix.
     */
    private String createDestinationKey(String suffix) {
        return publicDir + suffix;
    }

    /**
     * Builds the public CDN URL using the base URL and S3 public key.
     */
    private String createPublicUrl(String publicKey) {
        return cdnBaseUrl + "/" + publicKey;
    }
}
