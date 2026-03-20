package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class AnnouncementReadDTO {
    private Long announcementId;

    public AnnouncementReadDTO(Long announcementId) {
        this.announcementId = announcementId;
    }

}
