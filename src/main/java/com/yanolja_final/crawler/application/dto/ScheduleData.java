package com.yanolja_final.crawler.application.dto;

import java.util.List;

public record ScheduleData(
    int day,
    List<String> scheduleSummaries,
    String breakfast,
    String lunch,
    String dinner
) {

}
