package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.DepartureData;
import com.yanolja_final.crawler.application.dto.PackageData;
import com.yanolja_final.crawler.application.dto.ScheduleData;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SqlConverter {

    public String convert(List<PackageData> datas) {
        StringBuilder sb = new StringBuilder();


        for (PackageData data : datas) {
            String url = "https://travel.interpark.com/tour/goods?goodsCd=" + data.code().goodsCode();
            sb.append("-- ").append(url)
                .append('\n');
            String title = data.title();
            sb.append("-- ").append(title)
                .append('\n');

            sb.append(convert(data))
                .append('\n');
        }

        return sb.toString();
    }

    public String convert(PackageData data) {
        StringBuilder sb = new StringBuilder();

        // Assuming you have a method to get nationId and continentId from nationName
        int nationId = getNationId(data.nationName());
        int continentId = getContinentId(data.nationName());

        // Convert inclusionList and exclusionList to JSON format if needed
        String inclusionListJson = data.inclusionList();
        String exclusionListJson = data.exclusionList();

        // Convert schedules to JSON format
        String schedulesJson = convertSchedulesToJson(data.schedules());

        // INSERT INTO package
        String code = data.code().goodsCode();
        sb.append("INSERT INTO package (id, departure_time, end_time, nation_id, continent_id, title, transportation, info, intro_image_url, lodge_days, trip_days, inclusion_list, exclusion_list, viewed_count, purchased_count, monthly_purchased_count, shopping_count, schedules) ")
            .append("VALUES (")
            .append(code).append(", ")
            .append("'").append(data.departureTime()).append("', ")
            .append("'").append(data.endTime()).append("', ")
            .append(nationId).append(", ")
            .append(continentId).append(", ")
            .append("'").append(data.title()).append("', ")
            .append("'").append(data.transportation()).append("', ")
            .append("'").append(data.info().replace("\n", "\\n")).append("', ")
            .append("'").append(data.introImageUrls().get(0)).append("', ")
            .append(data.lodgeDays()).append(", ")
            .append(data.tripDays()).append(", ")
            .append("'").append(inclusionListJson).append("', ")
            .append("'").append(exclusionListJson).append("', ")
            .append("0, 0, 0, ") // Assuming viewed_count, purchased_count, monthly_purchased_count are initialized to 0
            .append(data.shoppingCount()).append(", ")
            .append("'").append(schedulesJson).append("');\n");

        // INSERT INTO package_image
        for (String imageUrl : data.imageUrls()) {
            sb.append("INSERT INTO package_image (package_id, image_url) VALUES (")
                .append(code).append(", '")
                .append(imageUrl).append("');\n");
        }

        // INSERT INTO package_departure_option
        for (DepartureData departure : data.departures()) {
            sb.append("INSERT INTO package_departure_option (package_id, departure_date, adult_price, infant_price, baby_price, current_reservation_count, min_reservation_count, max_reservation_count) ")
                .append("VALUES (")
                .append(code).append(", '")
                .append(departure.departureDate()).append("', ")
                .append(data.adultPrice()).append(", ")
                .append(data.infantPrice()).append(", ")
                .append(data.babyPrice()).append(", ")
                .append(data.reservationCount()).append(", ")
                .append(data.minReservationCount()).append(", ")
                .append(data.maxReservationCount()).append(");\n");
        }

        // INSERT INTO package_intro_image
        for (String introImageUrl : data.introImageUrls()) {
            sb.append("INSERT INTO package_intro_image (package_id, image_url) VALUES (")
                .append(code).append(", '")
                .append(introImageUrl).append("');\n");
        }

        return sb.toString();
    }

    private int getNationId(String nationName) {
        return switch (nationName) {
            case "베트남" -> 1;
            case "말레이시아" -> 2;
            case "필리핀" -> 3;
            case "사이판" -> 4;
            case "일본" -> 5;
            case "프랑스" -> 6;
            case "몰디브" -> 7;
            case "태국" -> 8;
            case "터키" -> 9;
            case "몽골" -> 10;
            case "영국" -> 11;
            case "싱가포르" -> 12;
            case "미국" -> 13;
            case "홍콩" -> 14;
            case "체코" -> 15;
            case "괌" -> 16;
            case "대만" -> 17;
            case "덴마크" -> 18;
            case "중국" -> 19;
            case "오스트레일리아" -> 20;
            case "스페인" -> 21;
            case "인도네시아" -> 22;
            case "라오스" -> 23;
            case "브루나이" -> 24;
            case "노르웨이" -> 25;
            case "이집트" -> 26;
            case "아랍에미리트" -> 27;
            case "스위스" -> 28;
            case "이탈리아" -> 29;
            case "사우디아라비아" -> 30;
            case "카타르" -> 31;
            case "핀란드" -> 32;
            case "그리스" -> 33;
            case "캄보디아" -> 34;
            case "오스트리아" -> 35;
            case "마카오" -> 36;
            case "포르투갈" -> 37;
            case "독일" -> 38;
            case "하와이" -> 39;
            case "바티칸" -> 40;
            default -> throw new RuntimeException(nationName + " 나라 분류 안됨");
        };
    }

    private int getContinentId(String nationName) {
        return switch (nationName) {
            // 아시아
            case "베트남", "말레이시아", "필리핀", "일본", "몰디브", "태국", "터키", "몽골", "싱가포르", "홍콩", "대만", "중국", "인도네시아", "라오스", "브루나이", "아랍에미리트", "사우디아라비아", "카타르", "캄보디아", "마카오" -> 1; // 아시아
            // 오세아니아
            case "호주", "사이판", "괌", "오스트레일리아" -> 2; // 오세아니아
            // 유럽
            case "프랑스", "영국", "체코", "덴마크", "스페인", "노르웨이", "스위스", "이탈리아", "핀란드", "그리스", "오스트리아", "포르투갈", "독일", "바티칸" -> 3; // 유럽
            // 아프리카
            case "이집트" -> 4; // 아프리카
            // 북미
            case "미국" -> 5; // 북미
            // 남미
            default -> throw new RuntimeException(nationName + " 대륙 분류 안됨");
        };
    }

    private String convertSchedulesToJson(List<ScheduleData> schedules) {
        JSONArray jsonSchedules = new JSONArray();

        for (ScheduleData schedule : schedules) {
            JSONObject jsonSchedule = new JSONObject();
            jsonSchedule.put("day", schedule.day());
            jsonSchedule.put("schedule", new JSONArray(schedule.scheduleSummaries()));
            jsonSchedule.put("breakfast", schedule.breakfast().isEmpty() ? "불포함" : schedule.breakfast());
            jsonSchedule.put("lunch", schedule.lunch().isEmpty() ? "불포함" : schedule.lunch());
            jsonSchedule.put("dinner", schedule.dinner().isEmpty() ? "불포함" : schedule.dinner());

            jsonSchedules.put(jsonSchedule);
        }

        return jsonSchedules.toString();
    }
}
