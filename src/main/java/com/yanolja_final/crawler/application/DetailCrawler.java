package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.application.dto.PackageJsons;
import com.yanolja_final.crawler.util.ApiResponseFetcher;
import java.io.BufferedReader;
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

    private int noSchedules = 0;
    private int goneOrCanceled = 0;
    private int noRemark = 0;

    public void crawle(List<PackageCode> codes) {
        int order = 1;
        int successCount = 0;
        int failCount = 0;
        for (PackageCode code : codes) {
            boolean isSuccess = crawle(code);
            if (isSuccess) {
                successCount++;
            } else {
                failCount++;
            }

            order++;
            log.info("{}: {} 중 {}번째 {} (현재 {}개 성공, {}개 실패) (일정표없음 {} 없음 {} R없음 {})", code, codes.size(), order, isSuccess ? "성공" : "실패", successCount, failCount, noSchedules, goneOrCanceled, noRemark);
        }
    }

    private boolean crawle(PackageCode code) {
        if (alreadyExists(code)) {
            return true;
        }

        String goodsResponse = crawleGoods(code); // TrafficSeq 가지고 있음
        if ("-1".equals(goodsResponse)) {
            removeFromFile(code);
            return false;
        }

        String trafficSeq = goodsResponse.split("TrafficSeq\":\"")[1].split("\"")[0];
//        log.info("goodsResponse\n{}", goodsResponse);


        String infoResponse = crawleInfo(code, trafficSeq);
//        log.info("infoResponse\n{}", infoResponse);

        String scheduleResponse = crawleSchedule(code, trafficSeq);
        if ("-1".equals(scheduleResponse)) {
            removeFromFile(code);
            return false;
        }
        String startDate = goodsResponse.split("StartDate\":\"")[1].split("\"")[0];
//        log.info("scheduleResponse\n{}", scheduleResponse);

        String otherGoodsResponse = crawleOtherGoods(code, startDate);
//        log.info("otherGoodsResponse\n{}", otherGoodsResponse);

        String reviewResponse = crawleReview(code);
//        log.info("reviewResponse\n{}", reviewResponse);

        String calendarResponse = crawleCalendar(code);
//        log.info("calendarResponse\n{}", calendarResponse);

        String imageResponse = crawleImage(code);
//        log.info("imageResponse\n{}", imageResponse);
        String countResponse = crawleCount(code);
//        log.info("countResponse\n{}", countResponse);

        PackageJsons jsons = new PackageJsons(imageResponse, countResponse, goodsResponse, infoResponse, scheduleResponse, otherGoodsResponse, reviewResponse, calendarResponse);
        save(code, jsons);
        return true;
    }

    private void removeFromFile(PackageCode code) {
        String keyword = String.format("%s,%s", code.goodsCode(), code.baseGoodsCode());
        StringBuilder content = new StringBuilder();

        // 파일 읽기
        String filePath = "./src/main/resources/codes.txt";
        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(keyword)) continue;
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("파일 읽기 중 오류 발생", e);
            throw new RuntimeException(e);
        }

        // 파일 쓰기
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(content.toString());
        } catch (IOException e) {
            log.error("파일 쓰기 중 오류 발생", e);
            throw new RuntimeException(e);
        }
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

        boolean isGone = response.contains("<title></title>");
        if (isGone) {
            log.info("{} 없음 TrashData {}", code, response.trim());
            goneOrCanceled++;
            return "-1";
        }
        boolean isTrashData = !response.contains("Remark");
        if (isTrashData) {
            log.info("{} Remark TrashData {}", code, response.trim());
            noRemark++;
            return "-1";
        }

        assertContains(response, "ReserveCnt", "RemainSeat", "MinStartNum", "ProductFeature",
            "GoodsDetailTraffic", "GoodsDetailHotel", "GoodsDetailTour", "GoodsDetailMeal",
            "GoodsDetailEtc", "InclusionList", "ExclusionList", "Remark",
            "ProductAttention", "CancelCommission", "PassVisa"); // CodeKRNM

        return response;
    }

    private String crawleSchedule(PackageCode code, String trafficSeq) {
        String url ="https://travel.interpark.com/api-package/goods/schedule/" + code.goodsCode() + "/" + trafficSeq;
        this.curUrl = url;

        String response = ApiResponseFetcher.get(url);

        boolean isTrashData = !response.contains("DaySeq");
        if (isTrashData) {
//            log.info("{} DaySeq TrashData {}", code, response.trim());
            noSchedules++;
//            return "-1";
        }

//        assertContains(response, "DaySeq", "SimpleDesc", "Breakfast", "Lunch", "Dinner");

        return response;
    }

    private String crawleInfo(PackageCode code, String trafficSeq) {
        String url = "https://travel.interpark.com/api-package/goods/tripInfo/" + code.goodsCode() + "/" + trafficSeq;
        this.curUrl = url;

        String response = ApiResponseFetcher.get(url);

        assertContains(response, "GoodsName", "Adult", "GoodsPrice", "FuelTax", "AirTax"); // CurrencyTypeNM
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
        String url = "https://travel.interpark.com/api-package/calendar/2024/1?resveCours=p&baseGoodsCD=" + code.baseGoodsCode();
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
