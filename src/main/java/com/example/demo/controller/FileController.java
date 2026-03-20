package com.example.demo.controller;

import com.example.demo.dto.GetUploadURLResponseDTO;
import com.example.demo.service.S3Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload-url")
    @PreAuthorize("isAuthenticated()")
    public GetUploadURLResponseDTO getUploadUrl(
            @RequestParam String fileName,
            @RequestParam(required = false) String fileType
    ) {
        return s3Service.generateUploadUrl(fileName, fileType);
    }
}