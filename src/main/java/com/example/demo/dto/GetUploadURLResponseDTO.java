package com.example.demo.dto;

public record GetUploadURLResponseDTO (
    String signedURL,
    String fileName,
    String publicFileUrl
){}
