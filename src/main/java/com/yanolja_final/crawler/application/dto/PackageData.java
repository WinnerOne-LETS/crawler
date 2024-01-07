package com.yanolja_final.crawler.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record PackageData(

    PackageCode code,
    LocalDate departureDate, // otherGoods
    LocalTime departureTime, // otherGoods
    LocalTime endTime, // otherGoods
    String nationName, // info
    List<String> imageUrls, // html
    String title, // info
    String transportation, // html
    String info, // html
    List<String> introImageUrls, // goods
    int lodgeDays, // html
    int tripDays, // html
    String inclusionList, // goods
    String exclusionList, // goods
    int shoppingCount, // info
    int optionalTourCount, // optionalTourCount
    int adultPrice, // info
    int infantPrice, // info
    int babyPrice, // info
    List<DepartureData> departures, // calendar
    int reservationCount,
    int minReservationCount, // goods
    int maxReservationCount, // goods
    List<ScheduleData> schedules, // info
    List<ReviewData> reviews // review
) {

}

