package com.yanolja_final.crawler.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ApiResponseFetcherTest {

    @ParameterizedTest
    @MethodSource("providedSourceForGet")
    @DisplayName("GET 메서드 요청에 대해 Http ResponseBody가 잘 들어온다")
    void get(String url, String keyword) throws Exception {
        String responseBody = ApiResponseFetcher.get(url);
        assertThat(responseBody).contains(keyword);
    }

    private static Stream<Arguments> providedSourceForGet() {
        return Stream.of(
            Arguments.of(
                "https://travel.interpark.com/api-package/goods/image/B4010646?pageNo=1&pageSize=30",
                "B4010646_5_813.jpg"
            ),
            Arguments.of(
                "https://travel.interpark.com/api-package/goods/reservationCount?baseGoodsCd=B4010646",
                "B4010646"
            ),
            Arguments.of(
                "https://travel.interpark.com/api-package/goods/schedule/24030517228/18",
                "오전출발 3박4일 티웨이항공 괌 PIC 자유일정!"
            ),
            Arguments.of(
                "https://travel.interpark.com/api-package/goods/review/B4010646?pageNo=1&pageSize=10",
                "5세 쌍둥이아이들 데리고 처음한 해외여행이었습니다."
            ),
            Arguments.of(
                "https://travel.interpark.com/api-package/goods/keyword/187/187/3",
                "체크,법인,기업,기프트카드 제외"
            ),
            Arguments.of(
                "https://travel.interpark.com/api-package/calendar/2023/12?resveCours=p&baseGoodsCD=B4010646",
                "lowestOfMonth"
            )
        );
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
