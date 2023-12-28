package com.yanolja_final.crawler.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RenderedHtmlTest {

    @Test
    @DisplayName("렌더링 된 HTML 내용을 잘 가져온다")
    void get() {
        // given
        String url = "https://travel.interpark.com/tour/goods?baseGoodsCd=A6018442";
        String renderDependentKeyword = "//tourimage.interpark.com/Spot/200/16987/202203/6378381560352261090.JPG";

        String url2 = "https://travel.interpark.com/tour/goods?goodsCd=24030517228";
        String renderDependentKeyword2 = "//tourimage.interpark.com/Spot/183/15509/201602/6359028033778087440.jpg";

        // when
        String source = RenderedHtml.fetch(url);
        String source2 = RenderedHtml.fetch(url2);

        // then
        assertThat(source).contains(renderDependentKeyword);
        assertThat(source2).contains(renderDependentKeyword2);
    }
}
