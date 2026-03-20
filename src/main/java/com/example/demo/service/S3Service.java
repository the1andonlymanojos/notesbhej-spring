package com.example.demo.service;

import com.example.demo.dto.GetUploadURLResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLConnection;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Presigner presigner;

    @Value("${app.bucket}")
    private String bucket;

    @Value("${app.public-url}")
    private String publicUrl;

    public S3Service(S3Presigner presigner) {
        this.presigner = presigner;
    }

    public GetUploadURLResponseDTO generateUploadUrl(String fileName, String fileType) {

        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1)
                : "";

        String key = UUID.randomUUID().toString();
        if (fileType == null) {
            key = key + "." + extension;
            fileType = URLConnection.guessContentTypeFromName(fileName);
            if (fileType == null) fileType = "application/octet-stream";
        }

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(fileType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .putObjectRequest(objectRequest)
                        .signatureDuration(Duration.ofHours(1))
                        .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        String signedUrl = presignedRequest.url().toString();
        String publicFileUrl = publicUrl + "/" + key;

        return new GetUploadURLResponseDTO(signedUrl,fileName,publicFileUrl);
    }
}
