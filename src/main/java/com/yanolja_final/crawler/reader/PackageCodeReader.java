package com.yanolja_final.crawler.reader;

import com.yanolja_final.crawler.application.dto.PackageCode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

public class PackageCodeReader {

    public static List<PackageCode> read() {
        String content = readFile("codes.txt");

        List<PackageCode> codes = new ArrayList<>();
        String[] strCodes = content.split("\n");

        for (String strCode : strCodes) {
            String[] splited = strCode.split(",");
            PackageCode code = new PackageCode(splited[1], splited[0]);
            codes.add(code);
        }

        return codes;
    }

    private static String readFile(String fileName) {
        ClassPathResource resource = new ClassPathResource(fileName);
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }
}
