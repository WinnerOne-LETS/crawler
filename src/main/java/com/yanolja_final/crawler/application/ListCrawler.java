package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.util.ApiResponseFetcher;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListCrawler {

    public void crawle() {
        List<PackageCode> total = new ArrayList<>();
        for (int i = 0; i < 8001; i += 100) {
            total.addAll(crawle(i));
            log.info("현재 {}개 완료", total.size());
        }
        
        save(new HashSet<>(total));
    }

    private void save(Set<PackageCode> total) {
        StringBuilder sb = new StringBuilder();

        for (PackageCode code : total) {
            sb.append(code.goodsCode())
                .append(',')
                .append(code.baseGoodsCode())
                .append('\n');
        }

        saveFile("list.txt", sb.toString());
        log.info("{}개 저장됨", total.size());
    }

    public static void saveFile(String fileName, String content) {
        String filePath = Paths.get(System.getProperty("user.dir"), fileName).toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PackageCode> crawle(int from) {
        String listResponse = ApiResponseFetcher.post(
            "https://travel.interpark.com/api-package/search",
            "{\"q\":\"\",\"domain\":\"r\",\"resveCours\":\"p\",\"start\":" + from
                + ",\"rows\":100,\"sort\":\"score desc\",\"filter\":[]}"
        );

        String[] baseArr = listResponse.split("\"baseGoodsCode\":\"");
        List<String> baseGoodsCodes = IntStream.range(0, baseArr.length)
            .skip(1)
            .filter(i -> i % 2 == 0)
            .mapToObj(i -> baseArr[i].split("\"")[0])
            .toList();

        String[] goodsArr = listResponse.split("\"goodsCode\":\"");
        List<String> goodsCodes = IntStream.range(0, goodsArr.length)
            .skip(1)
            .filter(i -> i % 2 == 0)
            .mapToObj(i -> goodsArr[i].split("\"")[0])
            .toList();

        if (baseGoodsCodes.size() != goodsCodes.size()) {
            throw new RuntimeException("데이터 사이즈가 다름");
        }

        List<PackageCode> packageCodes = new ArrayList<>();
        for (int i = 0; i < baseGoodsCodes.size(); i++) {
            PackageCode packageCode = new PackageCode(baseGoodsCodes.get(i), goodsCodes.get(i));
            packageCodes.add(packageCode);
        }

        return packageCodes;
    }
}
