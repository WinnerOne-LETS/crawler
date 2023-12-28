package com.yanolja_final.crawler;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Main implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/gimjinhong/Public/crawler/src/main/resources/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=ko");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");

        // WebDriver 생성.
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://travel.interpark.com/tour/goods?baseGoodsCd=A6018442");

        String source = driver.getPageSource();

        log.info(source);
    }
}
