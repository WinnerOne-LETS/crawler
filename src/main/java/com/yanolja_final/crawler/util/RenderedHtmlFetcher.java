package com.yanolja_final.crawler.util;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RenderedHtmlFetcher {

    private static final WebDriver driver;

    static {
        System.setProperty("webdriver.chrome.driver", "/Users/gimjinhong/Public/crawler/src/main/resources/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=ko");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--headless");

        driver = new ChromeDriver(options);
    }

    /**
     * @return 렌더링 된 HTML 소스
     */
    public static String fetch(String url) {
        driver.get(url);
        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(ExpectedConditions.presenceOfElementLocated(By.className("swiper-slide-active")));
        return driver.getPageSource();
    }
}
