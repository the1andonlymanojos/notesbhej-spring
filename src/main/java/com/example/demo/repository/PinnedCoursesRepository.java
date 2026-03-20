package com.example.demo.repository;


import com.example.demo.dto.PinnedCourseDTO;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface PinnedCoursesRepository extends JpaRepository<PinnedCourses, Long> {
    List<PinnedCourses> findByUser(User user);

    Optional<PinnedCourses> findByUserAndCourse(User user, Course course);
    @Query("""
SELECT new com.example.demo.dto.PinnedCourseDTO(
    pc.id,
    c.id,
    c.title,
    c.code,
    pc.pinnedAt
)
FROM PinnedCourses pc
JOIN pc.course c
WHERE pc.user.id = :userId
ORDER BY pc.pinnedAt DESC
""")
    List<PinnedCourseDTO> findPinnedCoursesForUser(Long userId);
    void deletePinnedCoursesByUserAndCourse(User user, Course course);
}
