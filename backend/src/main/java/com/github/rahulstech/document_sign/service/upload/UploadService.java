package com.github.rahulstech.document_sign.service.upload;

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
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger("UploadServiceProdImpl");

    private static final long SIGNATURE_DURATION_SECONDS = 24 * 3600; // 24 hours

    private static final HashMap<String,String> CONTENT_TYPE_TO_EXTENSION = new HashMap<>();

    static {
        CONTENT_TYPE_TO_EXTENSION.put("application/pdf", ".pdf");
        CONTENT_TYPE_TO_EXTENSION.put("image/jpg",".jpg");
        CONTENT_TYPE_TO_EXTENSION.put("image/jpeg",".jpeg");
        CONTENT_TYPE_TO_EXTENSION.put("image/png",".png");
        CONTENT_TYPE_TO_EXTENSION.put("image/webp",".webp");
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

    private Map<String,Object> getFileInfo(String key) {
        var cmd = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        var headRes =  client.headObject(cmd);
        var httpRes = headRes.sdkHttpResponse();

        if (!httpRes.isSuccessful()) {
            logger.info("HeadObjectRequest failed with status-code={}", httpRes.statusCode());
            throw HttpException.internalServerError("get upload metadata failed");
        }

        Map<String,Object> info = new HashMap<>();
        info.put("content-length", headRes.contentLength());
        info.put("content-type", headRes.contentType());

        return info;
    }

    public SaveUploadResult saveUpload(String srcKey, String... prefixes) {
        var info = getFileInfo(srcKey);
        var contentType = (String) info.get("content-type");
        var contentLength = (Long) info.get("content-length");
        var ext = getExtensionName(contentType);
        var publicKey = createPublicKey(ext, prefixes);
        var destKey = createDestinationKey(publicKey);
        var publicUrl = createPublicUrl(publicKey);

        // copy from temp to public directory
        copy(srcKey, destKey);

        return new SaveUploadResult(publicUrl, contentType, contentLength);
    }


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

    private String createTemporaryKey() {
        var suffix = UUID.randomUUID().toString();
        return tempDir+suffix;
    }

    private String createPublicKey(String ext, String... prefixes) {
        var prefix = Arrays.stream(prefixes)
                .map(String::trim)
                .filter(String::isBlank)
                .collect(Collectors.joining("/"));
        var suffix = UUID.randomUUID()+ext;
        if (prefix.isBlank()) {
            return suffix;
        }
        else {
            return prefix+"/"+suffix;
        }
    }

    private String createDestinationKey(String suffix) {
        return publicDir+suffix;
    }

    private String createPublicUrl(String publicKey) {
        return cdnBaseUrl+"/"+publicKey;
    }

    public String getExtensionName(String contentType) {
        return CONTENT_TYPE_TO_EXTENSION.getOrDefault(contentType, ".bin");
    }
}

