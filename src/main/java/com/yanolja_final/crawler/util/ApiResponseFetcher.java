package com.yanolja_final.crawler.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiResponseFetcher {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second

    public static String get(String url) {
        int retries = 0;
        while (true) {
            try {
                HttpURLConnection con = getConnection(url, "GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return getResponseBody(con);
                } else if (++retries >= MAX_RETRIES) {
                    throw new RuntimeException(url + " 200 응답이 오지 않음: " + con.getResponseCode());
                }

                Thread.sleep(RETRY_DELAY_MS);
            } catch (Exception e) {
                if (retries >= MAX_RETRIES) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

                retries++;
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

    public static String post(String url, String requestBody) {
        int retries = 0;
        while (true) {
            try {
                HttpURLConnection con = getConnection(url, "POST");
                write(con, requestBody);

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return getResponseBody(con);
                } else if (++retries >= MAX_RETRIES) {
                    throw new RuntimeException(url + " 200 응답이 오지 않음: " + con.getResponseCode());
                }

                Thread.sleep(RETRY_DELAY_MS);
            } catch (Exception e) {
                if (retries >= MAX_RETRIES) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

                retries++;
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
    }

//    public static String get(String url) {
//        try {
//            HttpURLConnection con = getConnection(url, "GET");
//
//            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                throw new RuntimeException(url + " 200 응답이 오지 않음: " + con.getResponseCode());
//            }
//
//            return getResponseBody(con);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new RuntimeException();
//        }
//    }
//
//    public static String post(String url, String requestBody) {
//        try {
//            HttpURLConnection con = getConnection(url, "POST");
//            write(con, requestBody);
//
//            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                throw new RuntimeException(url + " 200 응답이 오지 않음: " + con.getResponseCode());
//            }
//
//            return getResponseBody(con);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new RuntimeException();
//        }
//    }

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
