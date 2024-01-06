package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.application.dto.PackageData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

        log.info("{}\n{}\n{}\n{}\n{}", imageUrls, transportation, info.replace("\n", "\\n"), lodgeDays, tripDays);

        // introImageUrl, inclusionList, exclusionList, optionalTourCount, minReservationCount, maxReservationCount
        String goodsJson = read(code, "goodsResponse");

        // departureDate, departureTime, endTime
        String otherGoodsJson = read(code, "otherGoodsResponse");

        // departures
        String calendarJson = read(code, "calendarResponse");

        // review
        String reviewJson = read(code, "reviewResponse");

        // nationName, title, shoppingCount, adultPrice, infantPrice, babyPrice
        String infoJson = read(code, "infoResponse");

        // schedules
        String scheduleJson = read(code, "scheduleResponse");

        return null;
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
