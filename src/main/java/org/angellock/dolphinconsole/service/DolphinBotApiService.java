/*
 * DolphinBot - https://github.com/NeonAngelThreads/DolphinBot
 * Copyright (C) 2025 NeonAngelThreads (https://github.com/NeonAngelThreads)
 *
 *    This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 *    License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any
 *    later version.
 *
 *    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *    implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 *    License for more details. You should have received a copy of the GNU General Public License along with this
 *    program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * https://space.bilibili.com/386644641
 */

package org.angellock.dolphinconsole.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DolphinBotApiService {

    @Value("${dolphinbot.api.url}")
    private String apiBaseUrl;

    @Value("${dolphinbot.api.timeout}")
    private int timeout;

    private OkHttpClient client;
    private Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final Type type = new TypeToken<Map<String, Object>>(){}.getType();

    @PostConstruct
    public void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public boolean isApiAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(apiBaseUrl + "/health")
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }

    public List<Map<String, Object>> getBots() throws IOException {
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bots")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed: " + response.code());
            }
            String responseBody = response.body().string();

            Map<String, Object> result = gson.fromJson(responseBody, type);
            return (List<Map<String, Object>>) result.get("data");
        }
    }

    public boolean startBot(String profileName) throws IOException {
        Map<String, Object> requestBody = Map.of("profileName", profileName);
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bots/start")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public boolean stopBot(String botName) throws IOException {
        Map<String, Object> requestBody = Map.of("botName", botName);
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bots/stop")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public boolean sendCommand(String botName, String command) throws IOException {
        Map<String, Object> requestBody = Map.of(
                "botName", botName,
                "command", command
        );
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bots/send-command")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public boolean createBot(Map<String, String> conf) throws IOException {
        RequestBody body = RequestBody.create(gson.toJson(conf), JSON);
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bot/create")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return true;
        }
    }

    public Map<String, Object> getConfig() throws IOException {
        Request request = new Request.Builder()
                .url(apiBaseUrl + "/bot/create")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API request failed: " + response.code());
            }
            String responseBody = response.body().string();
            Map<String, Object> result = gson.fromJson(responseBody, type);
            return (Map<String, Object>) result.get("data");
        }
    }
}
