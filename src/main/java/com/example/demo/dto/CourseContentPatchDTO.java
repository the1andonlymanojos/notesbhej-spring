package com.example.demo.dto;

import java.util.List;

public record CourseContentPatchDTO(
        String title,
        Integer year,
        String batch,
        Integer semesterNumber,

        Long professorId,
        List<Long> tagIds
) {}
