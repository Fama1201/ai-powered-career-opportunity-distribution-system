package com.jobifycvut.backend.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenAiChatClient {
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient http;
    private final Gson gson;
    private final String apiKey;
    private final String model;

    public OpenAiChatClient(String apiKey, String model) {
        this.http = new OkHttpClient();
        this.gson = new GsonBuilder().create();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chatJson(List<Map<String, String>> messages, JsonObject jsonSchema) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);

        JsonArray arr = new JsonArray();
        for (Map<String, String> msg : messages) {
            JsonObject obj = new JsonObject();
            obj.addProperty("role", msg.get("role"));
            obj.addProperty("content", msg.get("content"));
            arr.add(obj);
        }
        payload.add("messages", arr);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_schema");

        JsonObject schemaWrapper = new JsonObject();
        schemaWrapper.addProperty("name", "cv_match_result");
        schemaWrapper.addProperty("strict", true);
        schemaWrapper.add("schema", jsonSchema);

        responseFormat.add("json_schema", schemaWrapper);
        payload.add("response_format", responseFormat);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response resp = http.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                int code = resp.code();
                String respBody = resp.body() != null ? resp.body().string() : "";
                System.err.println("❌ OpenAI error " + code + " body: " + respBody);
                throw new IOException("OpenAI error " + code + ": " + respBody);
            }

            String respBody = resp.body() != null ? resp.body().string() : "";
            JsonObject root = gson.fromJson(respBody, JsonObject.class);
            return root
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString()
                    .trim();
        }
    }
}
