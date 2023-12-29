package com.yanolja_final.crawler.application;

import com.yanolja_final.crawler.application.dto.PackageCode;
import com.yanolja_final.crawler.util.RenderedHtmlFetcher;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RenderedHtmlCrawler {

    private PackageCode curPackage;

    public void crawle(List<PackageCode> codes) {
        int order = 1;
        for (PackageCode code : codes) {
            this.curPackage = code;
            crawle(code);
            log.info("{}: {} 중 {}번째", code, codes.size(), order);
            order++;
        }
    }

    private void crawle(PackageCode code) {
        if (alreadyExists(code)) {
            return;
        }

        String url = "https://travel.interpark.com/tour/goods?goodsCd=" + code.goodsCode();
        String html = RenderedHtmlFetcher.fetch(url);

        assertContains(html, "btnNext", "일정표");

        save(code, html);
    }

    private boolean alreadyExists(PackageCode code) {
        Path path = Paths.get(System.getProperty("user.dir"), "/details/", code.baseGoodsCode() + "," + code.goodsCode() + "_html.txt");
        return Files.exists(path);
    }

    private void save(PackageCode code, String html) {
        saveFile(code.baseGoodsCode() + "," + code.goodsCode() + "_html.txt", html);
    }

    public static void saveFile(String fileName, String content) {
        String filePath = Paths.get(System.getProperty("user.dir"), "/details/", fileName)
            .toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertContains(String str, String... mustExistKeywords) {
        for (String keyword : mustExistKeywords) {
            if (!str.contains(keyword)) {
                throw new RuntimeException(curPackage + " => " + keyword + "가 없음\n" + str + "\n");
            }
        }
    }
}
