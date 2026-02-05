package com.example.httpPractice.nasaExample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class Nasa {
    public static String KEY = "Tj1whakmT4pyhSp9SH5uCUGxl4jUZsMZJ1OfNLqa";
    public static String NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=" + KEY;

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = CloseableHttpClient()) {
            HttpGet request = new HttpGet(NASA_URL);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                System.out.println("Анализ HTTP-ответа: ");
                System.out.println("Стартовая строка: " + response.getStatusLine());
                System.out.println("    Заголовки: ");
                for (var header : response.getAllHeaders()) {
                    System.out.println("    " + header.getName() + " : " + header.getValue());
                }
                String jsonResponse = EntityUtils.toString(response.getEntity());
                System.out.println("Message body: " + jsonResponse);

                System.out.println();

                PostNasa post = mapper.readValue(jsonResponse, PostNasa.class);

                String fileUrl = post.getUrl();
                String fileName = Paths.get(fileUrl).getFileName().toString();
                System.out.println("Скачиваем: " + fileName);

                HttpGet fileRequest = new HttpGet(fileUrl);
                try (CloseableHttpResponse fileResponse = httpClient.execute(fileRequest)) {
                    byte[] content = EntityUtils.toByteArray(fileResponse.getEntity());
                    try (FileOutputStream fos = new FileOutputStream(fileName)) {
                        fos.write(content);
                        System.out.println("Файл " + fileName + " успешно сохранен!");
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при скачивании файла: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при работе с сетью или файлами: " + e.getMessage());
        }
    }

    public static CloseableHttpClient CloseableHttpClient() {
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(true)
                        .build())
                .build();
    }
}