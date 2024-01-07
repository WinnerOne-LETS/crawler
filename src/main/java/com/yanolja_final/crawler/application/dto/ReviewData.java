package com.yanolja_final.crawler.application.dto;

import java.time.LocalDateTime;

public record ReviewData(
    String content,
    int productScore,
    int scheduleScore,
    int guideScore,
    int appointmentScore,
    LocalDateTime createdAt
) {

}
