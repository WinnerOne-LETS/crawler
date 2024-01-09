package com.yanolja_final.crawler.application.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ScheduleData(
    int day,
    List<String> scheduleSummaries,
    String breakfast,
    String lunch,
    String dinner
) {

    @Override
    public String toString() {
        return String.format("[%d일차] ", day)
            + IntStream.range(0, scheduleSummaries.size())
                .mapToObj(i -> String.format("%d. %s", i + 1, scheduleSummaries.get(i)))
                .collect(Collectors.joining(" / "));
    }
}
