package com.yanolja_final.crawler.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiResponseFetcher {

    /**
     * @return Http ResponseBody
     */
    static String get(String url) throws Exception {
        HttpURLConnection con = getConnection(url, "GET");

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("200 응답이 오지 않음: " + con.getResponseCode());
        }

        return getResponseBody(con);
    }

    static String post(String url, String requestBody) throws Exception {
        HttpURLConnection con = getConnection(url, "POST");
        write(con, requestBody);

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("200 응답이 오지 않음: " + con.getResponseCode());
        }

        return getResponseBody(con);
    }

    private static void write(HttpURLConnection con, String requestBody) throws IOException {
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
        bw.write(requestBody);
        bw.flush();
        bw.close();
    }

    private static String getResponseBody(HttpURLConnection con) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static HttpURLConnection getConnection(String strUrl, String httpMethod) throws IOException {
        URL url = new URL(strUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(httpMethod);
        return con;
    }
}
