package com.example.demo.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "course_content")
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Course course;

    @ManyToOne
    private Professor professor;

    @ManyToOne
    private User uploadedBy;
    // 🔹 version chain

    @ManyToOne
    @JsonIgnore
    private CourseContent prev;
    // 🔹 NEW: fast lookup of latest

    private Boolean isLatest = true;


    private Integer year;

    private String batch;

    private Integer semesterNumber;

    private String title;

    private String resourceUrl;

    private Boolean anonUpload;

    private String r2Url;

    private String fileType;

    private Instant createdAt;

    private Instant updatedAt;





    private Double orderIndex;

    @Enumerated(EnumType.STRING)
    private ContentVisibility visibility;

    @ManyToMany
    private List<Tag> tags;
}
