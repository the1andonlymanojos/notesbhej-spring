package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public record CourseContentResponse(
        List<CourseContentDTO> content,
        Map<Long, ProfessorDTO> professors
) {}
