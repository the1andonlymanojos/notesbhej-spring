package com.example.demo.dto;

import com.example.demo.entity.UserCourseInteraction;

public class InteractionMapper {

    public static InteractionDTO toDTO(UserCourseInteraction i) {
        return new InteractionDTO(
                i.getCourse().getId(),
                i.getCourse().getTitle(),
                i.getContent() != null ? i.getContent().getId() : null,
                i.getContent() != null ? i.getContent().getTitle() : null,"",
                i.getCreatedAt()
        );
    }
}
