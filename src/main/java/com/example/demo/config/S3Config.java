package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${app.use-dev}")
    private boolean useDev;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Value("${r2.endpoint}")
    private String r2Endpoint;

    @Value("${r2.access-key}")
    private String r2AccessKey;

    @Value("${r2.secret-key}")
    private String r2SecretKey;

    @Bean
    public S3Presigner s3Presigner() {

        return S3Presigner.builder()
                .endpointOverride(URI.create(useDev ? minioEndpoint : r2Endpoint))
                .region(Region.of(useDev ? "us-east-1" : "auto"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        useDev ? minioAccessKey : r2AccessKey,
                                        useDev ? minioSecretKey : r2SecretKey
                                )
                        )
                )
                .build();
    }
}