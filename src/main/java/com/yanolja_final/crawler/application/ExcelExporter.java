package com.yanolja_final.crawler.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yanolja_final.crawler.application.dto.DepartureData;
import com.yanolja_final.crawler.application.dto.PackageData;
import com.yanolja_final.crawler.application.dto.ReviewData;
import com.yanolja_final.crawler.application.dto.ScheduleData;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExcelExporter {

    public void export(List<PackageData> packageDataList, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet mainSheet = workbook.createSheet("Package Data");
            createMainSheetHeader(mainSheet.createRow(0));

            int rowNum = 1;
            for (PackageData data : packageDataList) {
                Row row = mainSheet.createRow(rowNum++);
                fillPackageDataRow(workbook, row, data, rowNum);
            }

            autoSizeColumns(mainSheet);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("엑셀 내보내기 완료");
    }

    private void createMainSheetHeader(Row row) {
        String[] headers = {
            "URL", "Departure Date", "Departure Time", "End Time", "Nation Name", "Image URLs", "Title",
            "Transportation", "Info", "Intro Image URLs", "Lodge Days", "Trip Days", "Inclusion List",
            "Exclusion List", "Shopping Count", "Optional Tour Count", "Adult Price", "Infant Price",
            "Baby Price", "Departures", "Reservation Count", "Min Reservation Count",
            "Max Reservation Count", "Schedules", "Reviews", "Code"
        };
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void fillPackageDataRow(Workbook workbook, Row row, PackageData data, int rowNum)
        throws JsonProcessingException {
        String url = "https://travel.interpark.com/tour/goods?goodsCd=" + data.code().goodsCode();
        createHyperlinkCell(workbook, row, 0, url);
        LocalDate departureDate = data.departureDate();
        row.createCell(1).setCellValue(departureDate == null ? "null" : departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        LocalTime departureTime = data.departureTime();
        row.createCell(2).setCellValue(departureTime == null ? "null" : departureTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        LocalTime endTime = data.endTime();
        row.createCell(3).setCellValue(endTime == null ? "null" : endTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        row.createCell(4).setCellValue(data.nationName());
        row.createCell(6).setCellValue(data.title());
        row.createCell(7).setCellValue(data.transportation());
        row.createCell(8).setCellValue(data.info());
        row.createCell(10).setCellValue(data.lodgeDays());
        row.createCell(11).setCellValue(data.tripDays());
        row.createCell(12).setCellValue(data.inclusionList());
        row.createCell(13).setCellValue(data.exclusionList());
        row.createCell(14).setCellValue(data.shoppingCount());
        row.createCell(15).setCellValue(data.optionalTourCount());
        row.createCell(16).setCellValue(data.adultPrice());
        row.createCell(17).setCellValue(data.infantPrice());
        row.createCell(18).setCellValue(data.babyPrice());
        row.createCell(20).setCellValue(data.reservationCount());
        row.createCell(21).setCellValue(data.minReservationCount());
        row.createCell(22).setCellValue(data.maxReservationCount());
        row.createCell(25).setCellValue(data.code().goodsCode());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String imageUrlsJson = mapper.writeValueAsString(data.imageUrls());
        String introImageUrlsJson = mapper.writeValueAsString(data.introImageUrls());
        String departuresJson = mapper.writeValueAsString(data.departures());
        String reviewsJson = mapper.writeValueAsString(data.reviews());

        // JSON 문자열을 적절한 셀에 설정
        row.createCell(5).setCellValue(imageUrlsJson);
        row.createCell(9).setCellValue(introImageUrlsJson.substring(0, Math.min(introImageUrlsJson.length(), 32766)));
        row.createCell(19).setCellValue(departuresJson);
        row.createCell(23).setCellValue(
            data.schedules().stream()
                .map(ScheduleData::toString)
                .collect(Collectors.joining("\n"))
        );
        row.createCell(24).setCellValue(reviewsJson);
    }

    private void createHyperlinkCell(Workbook workbook, Row row, int cellIndex, String url) {
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(url);

        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(url);
        cell.setHyperlink(hyperlink);

        // Optionally, style the cell to look like a typical hyperlink
        CellStyle hlinkStyle = workbook.createCellStyle();
        Font hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        cell.setCellStyle(hlinkStyle);
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
