package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.DepartureData;
import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.application.dto.PackageData;
import com.yanolja_final.crawler.application.dto.ReviewData;
import com.yanolja_final.crawler.application.dto.ScheduleData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PackageDataParser {

    public List<PackageData> parse(List<PackageCode> codes) {
        int idx = 1;

        List<PackageData> datas = new ArrayList<>();
        for (PackageCode code : codes) {
            log.info("{} {}", idx++, code);
            datas.add(parse(code));
        }
        return datas;
    }

    public PackageData parse(PackageCode code) {
        String renderedHtml = read(code, "html");
        // imageUrls, transportation, info, lodgeDays, tripDays
        List<String> imageUrls = Arrays.stream(renderedHtml.split("<section class=\"packageDetail\">")[1].split("<img src=\""))
            .skip(1)
            .map(t -> {
                String imageUrl = t.split("\"")[0];
                if (imageUrl.startsWith("//")) {
                    return "https:" + imageUrl;
                }
                return imageUrl;
            })
            .limit(10)
            .toList();
        String transportation = !renderedHtml.contains("교통편</dt><dd>") ? "항공불포함" : renderedHtml.split("교통편</dt><dd>")[1].split("</dd>")[0];
        String[] infos = Arrays.stream(renderedHtml.split("<ul class=\"pictogram\">")[1].split("</ul>")[0].split("<span class=\"text\">"))
            .skip(1)
            .map(t -> t.split("</span>")[0])
            .toArray(String[]::new);
        String info = String.format("%s\n%s\n%s\n%s", infos[0], infos[1], infos[3], infos[4]);
        int lodgeDays = Integer.parseInt(infos[2].split("박")[0]);
        int tripDays = Integer.parseInt(infos[2].split("박")[1].split("일")[0]);

        log.info("html\n썸네일: {}\n교통편: {}\n요약 정보: {}\n{}박 {}일", imageUrls, transportation, info.replace("\n", "\\n"), lodgeDays, tripDays);

        // introImageUrls, inclusionList, exclusionList, reservationCount, maxReservationCount, minReservationCount
        String goodsJson = read(code, "goodsResponse");

        String tmpProductFeature = goodsJson.split("\"ProductFeature\":\"")[1].split("ProductPriviledge")[0];
        List<String> introImageUrls = Arrays.stream(tmpProductFeature.split("img src=\\\\\""))
            .skip(1)
            .map(t -> t.split("\\\\\"")[0])
            .toList();

        String inclusionList = goodsJson.split("\"InclusionList\":")[1].split(",\"InfantPrice\"")[0];
        String exclusionList = goodsJson.split("\"ExclusionList\":")[1].split(",\"FamilyPack\"")[0];

        int reservationCount = Integer.parseInt(goodsJson.split("\"ReserveCnt\":\"")[1].split("\"")[0]);
        int maxReservationCount = reservationCount + Integer.parseInt(goodsJson.split("\"RemainSeat\":\"")[1].split("\"")[0]);
        int minReservationCount = Integer.parseInt(goodsJson.split("\"MinStartNum\":\"")[1].split("\"")[0]);

        log.info("goods\n상품소개이미지: {}\n포함: {}\n불포함: {}\n{}명 예약, 최대 {} 예약 가능, {}명 이상 되어야 출발", introImageUrls, inclusionList, exclusionList, reservationCount, maxReservationCount, minReservationCount);

        // nationName, title, shoppingCount, adultPrice, infantPrice, babyPrice
        String infoJson = read(code, "infoResponse");

        String nationName = !infoJson.contains("NationInfo\":[{\"Name\":\"") ? "" : infoJson.split("\"NationInfo\":\\[\\{\"Name\":\"")[1].split("\"")[0];
        String title = infoJson.split("GoodsName\":\"")[1].split("\"")[0];
        int shoppingCount = Integer.parseInt(infoJson.split("\"ShoppingCnt\":")[1].split(",")[0]);

        String tmpTotalPrice = infoJson.split("Total")[1].split("Local")[0];
        int adultPrice = Integer.parseInt(tmpTotalPrice.split("\"Adult\":")[1].split(",")[0]);
        int infantPrice = Integer.parseInt(tmpTotalPrice.split("\"Infant\":")[1].split(",")[0]);
        int babyPrice = Integer.parseInt(tmpTotalPrice.split("\"Baby\":")[1].split("}")[0]);

        log.info("info\n나라: {}\n제목: {}\n쇼핑횟수: {}\n성인 {}원, 소아 {}원, 유아 {}원", nationName, title, shoppingCount, adultPrice, infantPrice, babyPrice);

        // departureDate, departureTime, endTime
        String otherGoodsJson = read(code, "otherGoodsResponse");

        String strDepartureDate = otherGoodsJson.split("DepartureDT\":\"")[1].split("\"")[0];
        LocalDate departureDate = strDepartureDate.trim().isEmpty() ? null : LocalDate.parse(strDepartureDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

        String strDepartureTime = otherGoodsJson.split("DepartTime\":\"")[1].split("\"")[0];
        LocalTime departureTime = strDepartureTime.trim().isEmpty() ? null : LocalTime.parse(strDepartureTime, DateTimeFormatter.ofPattern("HHmm"));

        String strEndTime = otherGoodsJson.split("LocalArrivalTime\":\"")[1].split("\"")[0];
        LocalTime endTime = strEndTime.trim().isEmpty() ? null : LocalTime.parse(strEndTime, DateTimeFormatter.ofPattern("HHmm"));

        log.info("otherGoods\n출발일: {}\n출발시간: {}\n도착시간: {}", departureDate, departureTime, endTime);

        // departures (departureDate, priceDiff)
        String calendarJson = read(code, "calendarResponse");

        List<DepartureData> departures = new ArrayList<>();
        JSONObject calendarObject = new JSONObject(calendarJson);
        JSONArray months = calendarObject.getJSONArray("data");
        for (Object object : months) {
            JSONObject month = (JSONObject) object;

            JSONArray days = month.getJSONArray("days");
            for (Object o : days) {
                JSONObject day = (JSONObject) o;

                int price = day.getInt("price");
                if (price == 0)
                    continue; // 예약 불가

                int _year = day.getInt("year");
                int _month = day.getInt("month");
                int _day = day.getInt("day");
                LocalDate _departureDate = LocalDate.of(_year, _month, _day);

                int priceDiff = price - adultPrice;

                DepartureData departureData = new DepartureData(_departureDate, priceDiff);
                departures.add(departureData);
            }
        }

        log.info("calendar\n출발일정들: {}",departures);

        // reviews (content, productScore, scheduleScore, guideScore, appointmentScore, createdAt)
        String reviewJson = read(code, "reviewResponse");

        List<ReviewData> reviews = new ArrayList<>();

        JSONArray reviewArr = new JSONObject(reviewJson).getJSONObject("data").getJSONArray("EvalList");
        for (Object o : reviewArr) {
            JSONObject reviewObj = (JSONObject) o;

            String content = reviewObj.getString("Title");
            int productScore = Integer.parseInt(reviewObj.getString("Item2"));
            int scheduleScore = Integer.parseInt(reviewObj.getString("Item4"));
            int guideScore = Integer.parseInt(reviewObj.getString("Item5"));
            int appointmentScore = Integer.parseInt(reviewObj.getString("Item6"));

            String strCreatedAt = reviewObj.getString("RegDT");
            LocalDateTime createdAt = LocalDateTime.parse(strCreatedAt,
                DateTimeFormatter.ofPattern("yyyy-MM-dd a h:mm:ss", Locale.KOREAN));

            ReviewData reviewData = new ReviewData(content, productScore, scheduleScore, guideScore,
                appointmentScore, createdAt);
            reviews.add(reviewData);
        }

        log.info("reviews\n리뷰: {}", reviews);

        // optionalTourCount, schedules (day, scheduleSummaries, breakfast, lunch, dinner)
        String scheduleJson = read(code, "scheduleResponse");

        List<ScheduleData> scheduleDatas = new ArrayList<>();
        int optionalTourCount = 0;

        JSONArray dayScheduleArr = new JSONObject(scheduleJson).getJSONObject("data").getJSONArray("ScheduleDaysList");
        for (Object object : dayScheduleArr) {
            JSONObject daySchedule = (JSONObject) object;

            int daySeq = daySchedule.getInt("DaySeq");
            String breakfast = daySchedule.getString("Breakfast");
            String lunch = daySchedule.getString("Lunch");
            String dinner = daySchedule.getString("Dinner");
            List<String> scheduleSummaries = new ArrayList<>();

            for (Object o : daySchedule.getJSONArray("GoodsScheduleDaysDetailList")) {
                JSONObject scheduleDetail = (JSONObject) o;

                String scheduleSummary = scheduleDetail.getString("SimpleDesc");
                scheduleSummaries.add(scheduleSummary);
            }

            ScheduleData scheduleData = new ScheduleData(daySeq, scheduleSummaries, breakfast, lunch, dinner);
            scheduleDatas.add(scheduleData);

            optionalTourCount += daySchedule.getJSONArray("SelTourList").length();
        }
        log.info("schedules\n선택관광 수: {}\n일정들: {}", optionalTourCount, scheduleDatas);

        return new PackageData(
            code,
            departureDate,
            departureTime,
            endTime,
            nationName,
            imageUrls,
            title,
            transportation,
            info,
            introImageUrls,
            lodgeDays,
            tripDays,
            inclusionList,
            exclusionList,
            shoppingCount,
            optionalTourCount,
            adultPrice,
            infantPrice,
            babyPrice,
            departures,
            reservationCount,
            minReservationCount,
            maxReservationCount,
            scheduleDatas,
            reviews
        );
    }

    public String read(PackageCode code, String type) {
        return readFile(code.baseGoodsCode() + "," + code.goodsCode() + "_" + type + ".txt");
    }
    
    public String readFile(String fileName) {
        String filePath = Paths.get(System.getProperty("user.dir"), "/details/", fileName).toString();
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return contentBuilder.toString();
    }
}
