package com.yanolja_final.crawler.application.dto;

import java.time.LocalDate;

public record DepartureData(
    LocalDate departureDate,
    int priceDiff
) {

}
