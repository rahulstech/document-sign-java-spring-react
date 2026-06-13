package com.github.rahulstech.document_sign.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AWSConfig {

    @Value("${AWS_S3_REGION}")
    private String s3Region;

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.of(s3Region))
                .build();
    }

    @Bean
    public S3Presigner getS3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(s3Region))
                .build();
    }
}
