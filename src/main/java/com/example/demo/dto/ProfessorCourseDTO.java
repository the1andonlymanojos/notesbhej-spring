package com.example.demo.dto;

import com.example.demo.entity.UserCourseInteraction;

import java.time.Instant;

public record ProfessorCourseDTO(
        Long professorId,
        String professorName,
        String professorEmail,
        Long courseId,
        String courseTitle,
        String courseCode
) {}





