package com.example.demo.repository;

import com.example.demo.dto.ProfessorCourseDTO;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseContent;
import com.example.demo.entity.Professor;
import com.example.demo.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findAllByCourse_Id(Long courseId);
    @Query("""
SELECT DISTINCT new com.example.demo.dto.ProfessorCourseDTO(
    p.id,
    p.name,
    p.email,
    c.id,
    c.title,
    c.code
)
FROM CourseContent cc
JOIN cc.professor p
JOIN cc.course c
WHERE cc.visibility = 'VISIBLE'
ORDER BY p.name, c.title
""")
    List<ProfessorCourseDTO> findProfessorCourses(Pageable pageable);


    @Query("""
SELECT MAX(c.orderIndex)
FROM CourseContent c
WHERE c.course.id = :courseId
  AND c.year = :year
  AND c.semesterNumber = :semester
  AND c.batch = :batch
  AND c.professor.id = :professorId
""")
    Double findMaxOrderInGroup(
            Long courseId,
            Integer year,
            Integer semester,
            String batch,
            Long professorId
    );


    List<CourseContent> findAllByCourse_IdOrderByOrderIndexAsc(Long courseId);

    //List<CourseContent> findAllByCourse_IdAndIsLatestTrueOrderByOrderIndexAsc(Long id);
    List<CourseContent> findAllByUploadedBy(User uploadedBy);
}
