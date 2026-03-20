package com.example.demo.repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import com.example.demo.entity.UserCourseInteraction;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserCourseInteractionRepository extends JpaRepository<UserCourseInteraction, Long> {

    List<UserCourseInteraction> findAllByUser(User user, Sort sort, Limit limit);

    List<UserCourseInteraction> findAllByUserAndCourse(
            User user,
            Course course,
            Sort sort,
            Limit limit
    );
}