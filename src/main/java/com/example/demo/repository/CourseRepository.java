package com.example.demo.repository;

import com.example.demo.dto.ProfessorCourseDTO;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseContent;
import com.example.demo.entity.Professor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface CourseRepository extends JpaRepository<Course, Long> {

}
