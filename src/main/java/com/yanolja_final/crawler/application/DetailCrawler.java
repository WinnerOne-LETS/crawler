package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.application.dto.PackageJsons;
import com.yanolja_final.crawler.util.ApiResponseFetcher;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DetailCrawler {

    private String curUrl = "";
    private PackageJsons jsons;

    public void crawle(List<PackageCode> codes) {
        int order = 1;
        for (PackageCode code : codes) {
            crawle(code);

            order++;
            log.info("{} 중 {}번째", codes.size(), order);
        }
    }

    private void crawle(PackageCode code) {
        if (alreadyExists(code)) {
            return;
        }

        String imageResponse = crawleImage(code);
//        log.info("imageResponse\n{}", imageResponse);
        String countResponse = crawleCount(code);
//        log.info("countResponse\n{}", countResponse);

        String goodsResponse = crawleGoods(code); // TrafficSeq 가지고 있음
        if ("-1".equals(goodsResponse)) {
            return;
        }
        String trafficSeq = goodsResponse.split("TrafficSeq\":\"")[1].split("\"")[0];
//        log.info("goodsResponse\n{}", goodsResponse);


        String infoResponse = crawleInfo(code, trafficSeq);
//        log.info("infoResponse\n{}", infoResponse);

        String scheduleResponse = crawleSchedule(code, trafficSeq);
        String startDate = goodsResponse.split("StartDate\":\"")[1].split("\"")[0];
//        log.info("scheduleResponse\n{}", scheduleResponse);

        String otherGoodsResponse = crawleOtherGoods(code, startDate);
//        log.info("otherGoodsResponse\n{}", otherGoodsResponse);

        String reviewResponse = crawleReview(code);
//        log.info("reviewResponse\n{}", reviewResponse);

        String calendarResponse = crawleCalendar(code);
//        log.info("calendarResponse\n{}", calendarResponse);

        PackageJsons jsons = new PackageJsons(imageResponse, countResponse, goodsResponse, infoResponse, scheduleResponse, otherGoodsResponse, reviewResponse, calendarResponse);
        save(code, jsons);
    }

    private boolean alreadyExists(PackageCode code) {
        Path path = Paths.get(System.getProperty("user.dir"), "/details/", code.baseGoodsCode() + "," + code.goodsCode() + "_imageResponse.txt");
        return Files.exists(path);
    }

    private void save(PackageCode code, PackageJsons jsons) {
        try {
            RecordComponent[] components = jsons.getClass().getRecordComponents();
            for (RecordComponent component : components) {
                String name = component.getName();
                String value = (String) jsons.getClass().getMethod(name).invoke(jsons);

                saveFile(code.baseGoodsCode() + "," + code.goodsCode() + "_" + name + ".txt", value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(String fileName, String content) {
        String filePath = Paths.get(System.getProperty("user.dir"), "/details/", fileName).toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String crawleImage(PackageCode code) {
        String url = "https://travel.interpark.com/api-package/goods/image/" + code.baseGoodsCode()
            + "?pageNo=1&pageSize=30";
        this.curUrl = url;
        return ApiResponseFetcher
            .get(url);
    }

    private String crawleCount(PackageCode code) {
        String url = "https://travel.interpark.com/api-package/goods/reservationCount?baseGoodsCd="
            + code.baseGoodsCode();
        this.curUrl = url;

        return ApiResponseFetcher
            .get(url);
    }

    private String crawleGoods(PackageCode code) {
        String url = "https://travel.interpark.com/tour/goods?goodsCd=" + code.goodsCode();
        this.curUrl = url;

        String response = ApiResponseFetcher.get(curUrl);

        boolean isTrashData = !response.contains("CodeKRNM");
        if (isTrashData) {
            return "-1";
        }

        assertContains(response, "ReserveCnt", "RemainSeat", "MinStartNum", "ProductFeature",
            "GoodsDetailTraffic", "GoodsDetailHotel", "GoodsDetailTour", "GoodsDetailMeal",
            "GoodsDetailEtc", "InclusionList", "ExclusionList", "CodeKRNM", "Remark",
            "ProductAttention", "CancelCommission", "PassVisa");

        return response;
    }

    private String crawleSchedule(PackageCode code, String trafficSeq) {
        String url ="https://travel.interpark.com/api-package/goods/schedule/" + code.goodsCode() + "/" + trafficSeq;
        this.curUrl = url;

        String response = ApiResponseFetcher.get(url);
        assertContains(response, "DaySeq", "SimpleDesc", "Breakfast", "Lunch", "Dinner");

        return response;
    }

    private String crawleInfo(PackageCode code, String trafficSeq) {
        String url = "https://travel.interpark.com/api-package/goods/tripInfo/" + code.goodsCode() + "/" + trafficSeq;
        this.curUrl = url;

        String response = ApiResponseFetcher.get(url);

        assertContains(response, "GoodsName", "CurrencyTypeNM", "Adult", "GoodsPrice", "FuelTax", "AirTax");
        return response;
    }

    private String crawleOtherGoods(PackageCode code, String startDate) {
        String url = "https://travel.interpark.com/api-package/goods/otherGoods";
        this.curUrl = url;

        String response = ApiResponseFetcher.post(
            url,
            "{\"baseGoodsCD\":\"" + code.baseGoodsCode() + "\",\"startDate\":\"" + startDate
                + "\",\"endDate\":\"" + startDate
                + "\",\"sort\":\"L\",\"pageNum\":\"1\",\"pageSize\":\"20\",\"sortMode\":\"A\"}"
        );
        assertContains(response, "DepartureDT", "ArrivalDT");

        return response;
    }

    private String crawleReview(PackageCode code) {
        String url = "https://travel.interpark.com/api-package/goods/review/" + code.baseGoodsCode() + "?pageNo=1&pageSize=10";
        this.curUrl = url;

        String response = ApiResponseFetcher.get(
            url);
        assertContains(response, "success");

        return response;
    }

    private String crawleCalendar(PackageCode code) {
        // TODO 주의: 2023 12 날짜에 의존적
        String url = "https://travel.interpark.com/api-package/calendar/2023/12?resveCours=p&baseGoodsCD=" + code.baseGoodsCode();
        this.curUrl = url;

        String response = ApiResponseFetcher.get(url);
        assertContains(response, "success");
        return response;
    }

    private void assertContains(String str, String... mustExistKeywords) {
        for (String keyword : mustExistKeywords) {
            if (!str.contains(keyword)) {
                throw new RuntimeException(curUrl + " => " + keyword + "가 없음\n" + str + "\n");
            }
        }
    }
}
