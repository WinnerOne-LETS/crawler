package com.yanolja_final.crawler.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * 브라우저에 렌더링 된 후의 HTML 소스 코드를 긁는 클래스
 */
public class RenderedHtml {

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

    public static String fetch(String url) {
        driver.get(url);
        return driver.getPageSource();
    }
}
