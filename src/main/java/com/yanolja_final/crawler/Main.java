package com.yanolja_final.crawler;

import com.yanolja_final.crawler.application.ListCrawler;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Main implements ApplicationRunner {

    @Autowired
    ListCrawler listCrawler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        listCrawler.crawle();
    }
}
