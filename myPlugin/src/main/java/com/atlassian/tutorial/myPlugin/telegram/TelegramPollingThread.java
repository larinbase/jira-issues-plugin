package com.atlassian.tutorial.myPlugin.telegram;

//import com.atlassian.tutorial.myPlugin.service.IssueService;

import com.atlassian.tutorial.myPlugin.service.IssueService;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class TelegramPollingThread extends Thread {
    private final String token;
    private long lastUpdateId = 0;
    private final TelegramSender sender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramPollingThread(String token) {
        this.token = token;
        this.sender = new TelegramSender(token);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                String url = String.format(
                        "https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=30",
                        token, lastUpdateId + 1
                );
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = reader.lines().collect(Collectors.joining());
                reader.close();

                JsonNode root = objectMapper.readTree(response);
                JsonNode updates = root.get("result");

                if (updates != null && updates.isArray()) {
                    onUpdateReceived(updates);
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

    private void onUpdateReceived(JsonNode updates) {
        for (JsonNode update : updates) {
            lastUpdateId = update.get("update_id").asLong();

            JsonNode message = update.get("message");
            if (message == null || !message.has("text")) continue;

            String text = message.get("text").asText();
            String chatId = message.get("chat").get("id").asText();

            System.out.println("Received message: " + text + " from chatId: " + chatId);

            if("/start".equalsIgnoreCase(text.trim())) {
                sender.sendMessage(chatId, "Привет! Введите /issues, чтобы получить список открытых задач.");
                return;
            }

            if ("/issues".equalsIgnoreCase(text.trim())) {
                try {
                    String issues = IssueService.getOpenIssuesAsText();
                    String response = "Открытые задачи в Jira:\n" + issues;
                    sender.sendMessage(chatId, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(chatId, "Ошибка при получении задач.");
                }
                return;
            }
            sender.sendMessage(chatId, "Команда не распознана.");
        }
    }

}
