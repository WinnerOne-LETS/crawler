package com.yanolja_final.crawler.application;

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
            "Max Reservation Count", "Schedules", "Reviews"
        };
        for (int i = 0; i < headers.length; i++) {
            row.createCell(i).setCellValue(headers[i]);
        }
    }

    private void fillPackageDataRow(Workbook workbook, Row row, PackageData data, int rowNum) {
        String url = "https://travel.interpark.com/tour/goods?goodsCd=" + data.code().goodsCode();
        createHyperlinkCell(workbook, row, 0, url);
        LocalDate departureDate = data.departureDate();
        row.createCell(1).setCellValue(departureDate == null ? "null" : departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        LocalTime departureTime = data.departureTime();
        row.createCell(2).setCellValue(departureTime == null ? "null" : departureTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        LocalTime endTime = data.endTime();
        row.createCell(3).setCellValue(endTime == null ? "null" : endTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        row.createCell(4).setCellValue(data.nationName());
        String imageUrlCount = data.imageUrls().size() + " item(s)";
        row.createCell(5).setCellValue(imageUrlCount + " (See Image URLs Sheet Row " + rowNum + ")");
        row.createCell(6).setCellValue(data.title());
        row.createCell(7).setCellValue(data.transportation());
        row.createCell(8).setCellValue(data.info());
        String introImageUrlCount = data.introImageUrls().size() + " item(s)";
        row.createCell(9).setCellValue(introImageUrlCount + " (See Intro Image URLs Sheet Row " + rowNum + ")");
        row.createCell(10).setCellValue(data.lodgeDays());
        row.createCell(11).setCellValue(data.tripDays());
        row.createCell(12).setCellValue(data.inclusionList());
        row.createCell(13).setCellValue(data.exclusionList());
        row.createCell(14).setCellValue(data.shoppingCount());
        row.createCell(15).setCellValue(data.optionalTourCount());
        row.createCell(16).setCellValue(data.adultPrice());
        row.createCell(17).setCellValue(data.infantPrice());
        row.createCell(18).setCellValue(data.babyPrice());
        String departuresCount = data.departures().size() + " item(s)";
        row.createCell(19).setCellValue(departuresCount + " (See Departures Sheet Row " + rowNum + ")");
        row.createCell(20).setCellValue(data.reservationCount());
        row.createCell(21).setCellValue(data.minReservationCount());
        row.createCell(22).setCellValue(data.maxReservationCount());
        String schedulesCount = data.schedules().size() + " item(s)";
        row.createCell(23).setCellValue(schedulesCount + " (See Schedules Sheet Row " + rowNum + ")");
        String reviewsCount = data.reviews().size() + " item(s)";
        row.createCell(24).setCellValue(reviewsCount + " (See Reviews Sheet Row " + rowNum + ")");
        createAdditionalSheets(workbook, data, rowNum);
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

    private void createAdditionalSheets(Workbook workbook, PackageData data, int rowNum) {
        createListSheet(workbook, "Image URLs", data.imageUrls(), rowNum);
        createListSheet(workbook, "Intro Image URLs", data.introImageUrls(), rowNum);
        createDeparturesSheet(workbook, "Departures", data.departures(), rowNum);
        createSchedulesSheet(workbook, "Schedules", data.schedules(), rowNum);
        createReviewsSheet(workbook, "Reviews", data.reviews(), rowNum);
    }

    private void createListSheet(Workbook workbook, String sheetName, List<String> list, int rowNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }

        for (String item : list) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(item.substring(0, Math.min(item.length(), 32766)));
        }
    }

    private void createDeparturesSheet(Workbook workbook, String sheetName, List<DepartureData> list, int rowNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Package Row Num");
            headerRow.createCell(1).setCellValue("Departure Date");
            headerRow.createCell(2).setCellValue("Price Diff");
        }

        for (DepartureData item : list) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(item.departureDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            row.createCell(2).setCellValue(item.priceDiff());
        }
    }

    private void createSchedulesSheet(Workbook workbook, String sheetName, List<ScheduleData> list, int rowNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Package Row Num");
            headerRow.createCell(1).setCellValue("Day");
            headerRow.createCell(2).setCellValue("Schedule Summaries");
            headerRow.createCell(3).setCellValue("Breakfast");
            headerRow.createCell(4).setCellValue("Lunch");
            headerRow.createCell(5).setCellValue("Dinner");
        }

        for (ScheduleData item : list) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(item.day());
            row.createCell(2).setCellValue(String.join(", ", item.scheduleSummaries()));
            row.createCell(3).setCellValue(item.breakfast());
            row.createCell(4).setCellValue(item.lunch());
            row.createCell(5).setCellValue(item.dinner());
        }
    }

    private void createReviewsSheet(Workbook workbook, String sheetName, List<ReviewData> list, int rowNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Package Row Num");
            headerRow.createCell(1).setCellValue("Content");
            headerRow.createCell(2).setCellValue("Product Score");
            headerRow.createCell(3).setCellValue("Schedule Score");
            headerRow.createCell(4).setCellValue("Guide Score");
            headerRow.createCell(5).setCellValue("Appointment Score");
            headerRow.createCell(6).setCellValue("Created At");
        }

        for (ReviewData item : list) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(item.content());
            row.createCell(2).setCellValue(item.productScore());
            row.createCell(3).setCellValue(item.scheduleScore());
            row.createCell(4).setCellValue(item.guideScore());
            row.createCell(5).setCellValue(item.appointmentScore());
            row.createCell(6).setCellValue(item.createdAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
