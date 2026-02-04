package com.jobifycvut.backend.service;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OpenAiClient {
    String chatJson(String apiKey, String model, List<Map<String, String>> messages, JsonObject jsonSchema)
            throws IOException;
}
