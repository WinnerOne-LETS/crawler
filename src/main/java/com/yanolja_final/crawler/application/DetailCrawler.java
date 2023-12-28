package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.util.ApiResponseFetcher;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DetailCrawler {

    public void crawle(List<PackageCode> codes) {
        for (PackageCode code : codes) {
            crawle(code);
        }
    }

    private void crawle(PackageCode code) {
        String imageResponse = crawleImage(code);
        log.info("imageResponse\n{}\n", imageResponse);
        String countResponse = crawleCount(code);
        log.info("countResponse\n{}\n", countResponse);

        String goodsResponse = crawleGoods(code); // TrafficSeq 가지고 있음
        String trafficSeq = goodsResponse.split("TrafficSeq\":\"")[1].split("\"")[0];
        log.info("goodsResponse\n{}\n", goodsResponse);

        String scheduleResponse = crawleSchedule(code, trafficSeq);
        String startDate = goodsResponse.split("StartDate\":\"")[1].split("\"")[0];
        log.info("scheduleResponse\n{}\n", scheduleResponse);

        String infoResponse = crawleInfo(code, trafficSeq);
        log.info("infoResponse\n{}\n", infoResponse);

        String otherGoodsResponse = crawleOtherGoods(code, startDate);
        log.info("otherGoodsResponse\n{}\n", otherGoodsResponse);

        String reviewResponse = crawleReview(code);
        log.info("reviewResponse\n{}\n", reviewResponse);

        String calendarResponse = crawleCalendar(code);
        log.info("calendarResponse\n{}\n", calendarResponse);
    }

    private String crawleImage(PackageCode code) {
        return ApiResponseFetcher
            .get("https://travel.interpark.com/api-package/goods/image/" + code.baseGoodsCode()
                + "?pageNo=1&pageSize=30");
    }

    private String crawleCount(PackageCode code) {
        return ApiResponseFetcher
            .get("https://travel.interpark.com/api-package/goods/reservationCount?baseGoodsCd=" + code.baseGoodsCode());
    }

    private String crawleGoods(PackageCode code) {
        String response = ApiResponseFetcher.get(
            "https://travel.interpark.com/tour/goods?goodsCd=" + code.goodsCode());
        assertContains(response, "ReserveCnt", "RemainSeat", "MinStartNum", "ProductFeature",
            "GoodsDetailTraffic", "GoodsDetailHotel", "GoodsDetailTour", "GoodsDetailMeal",
            "GoodsDetailEtc", "InclusionList", "ExclusionList", "CodeKRNM", "Remark",
            "ProductAttention", "CancelCommission", "PassVisa");

        return response;
    }

    private String crawleSchedule(PackageCode code, String trafficSeq) {
        String response = ApiResponseFetcher.get(
            "https://travel.interpark.com/api-package/goods/schedule/" + code.goodsCode() + "/"
                + trafficSeq);
        assertContains(response, "DaySeq", "SimpleDesc", "Breakfast", "Lunch", "Dinner");

        return response;
    }

    private String crawleInfo(PackageCode code, String trafficSeq) {
        String url = "https://travel.interpark.com/api-package/goods/tripInfo/" + code.goodsCode() + "/" + trafficSeq;
        String response = ApiResponseFetcher.get(url);

        assertContains(response, "GoodsName", "CurrencyTypeNM", "Adult", "GoodsPrice", "FuelTax", "AirTax");
        return response;
    }

    private String crawleOtherGoods(PackageCode code, String startDate) {
        String response = ApiResponseFetcher.post(
            "https://travel.interpark.com/api-package/goods/otherGoods",
            "{\"baseGoodsCD\":\"" + code.baseGoodsCode() + "\",\"startDate\":\"" + startDate
                + "\",\"endDate\":\"" + startDate
                + "\",\"sort\":\"L\",\"pageNum\":\"1\",\"pageSize\":\"20\",\"sortMode\":\"A\"}"
        );
        assertContains(response, "DepartureDT", "ArrivalDT");

        return response;
    }

    private String crawleReview(PackageCode code) {
        String response = ApiResponseFetcher.get(
            "https://travel.interpark.com/api-package/goods/review/" + code.baseGoodsCode()
                + "?pageNo=1&pageSize=10");
        assertContains(response, "success");

        return response;
    }

    private String crawleCalendar(PackageCode code) {
        // TODO 주의: 2023 12 날짜에 의존적
        String response = ApiResponseFetcher.get(
            "https://travel.interpark.com/api-package/calendar/2023/12?resveCours=p&baseGoodsCD="
                + code.baseGoodsCode());
        assertContains(response, "success");
        return response;
    }

    private void assertContains(String str, String... mustExistKeywords) {
        for (String keyword : mustExistKeywords) {
            if (!str.contains(keyword)) {
                throw new RuntimeException(keyword + "가 없음\n" + str + "\n");
            }
        }
    }
}
