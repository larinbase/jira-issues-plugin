package com.atlassian.tutorial.myPlugin.telegram;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramSender {
    private final String token;

    public TelegramSender(String token) {
        this.token = token;
    }

    public void sendMessage(String chatId, String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String urlStr = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s",
                    token, chatId, encodedText);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().reduce("", (acc, line) -> acc + line);
            in.close();

            System.out.println("Telegram sendMessage response code: " + responseCode);
            System.out.println("Telegram sendMessage response body: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
