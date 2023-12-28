package com.yanolja_final.crawler.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseFetcherTest {

    @Test
    @DisplayName("GET 메서드 요청에 대해 Http ResponseBody가 잘 들어온다")
    void get() throws Exception {
        // given
        String url = "https://travel.interpark.com/api-package/goods/tripInfo/24030517228/18";

        // when
        String responseBody = ApiResponseFetcher.get(url);

        // then
        assertThat(responseBody).contains("[베스트셀러] 괌 PIC 골드카드+전일자유일정 오전출발4일_티웨이항공");
    }

    @Test
    @DisplayName("POST 메서드 요청에 대해 Http ResponseBody가 잘 들어온다")
    void post() throws Exception {
        // given
        String url = "https://travel.interpark.com/api-package/goods/otherGoods";
        String requestBody = "{\"baseGoodsCD\":\"B4010646\",\"startDate\":\"20240305\",\"endDate\":\"20240305\",\"sort\":\"L\",\"pageNum\":\"1\",\"pageSize\":\"20\",\"sortMode\":\"A\"}";

        // when
        String responseBody = ApiResponseFetcher.post(url, requestBody);

        // then
        assertThat(responseBody).contains("티웨이항공 전세기303");
    }
}
