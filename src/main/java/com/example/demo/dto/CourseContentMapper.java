package com.example.demo.dto;

import com.example.demo.entity.CourseContent;
import com.example.demo.entity.Professor;
import com.example.demo.entity.Tag;

public class CourseContentMapper {

    public static CourseContentDTO toDTO(CourseContent c, boolean isAuth) {
        return new CourseContentDTO(
                c.getId(),
                c.getTitle(),
                c.getYear(),
                c.getSemesterNumber(),
                c.getBatch(),
                c.getFileType(),
                isAuth ? c.getResourceUrl() : null,
                isAuth ? c.getR2Url() : null,
                c.getVisibility().name(),
                c.getProfessor() != null ? c.getProfessor().getId() : null,
                c.getAnonUpload(),
                c.getTags().toArray(new Tag[0])

        );
    }

    public static ProfessorDTO toProfessorDTO(Professor p) {
        return new ProfessorDTO(
                p.getId(),
                p.getName(),
                p.getDepartment(),
                p.getDesignation()
        );
    }
}

