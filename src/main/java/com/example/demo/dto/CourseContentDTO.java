package com.example.demo.dto;

import com.example.demo.entity.ContentVisibility;
import com.example.demo.entity.CourseContent;
import com.example.demo.entity.Professor;
import com.example.demo.entity.Tag;

import java.util.List;

public record CourseContentDTO(
        Long id,
        String title,
        Integer year,
        Integer semesterNumber,
        String batch,
        String fileType,
        String resourceUrl,
        String r2Url,
        String visibility,
        Long professorId,
        Boolean anonUpload,
        Tag[] tags
) {}




