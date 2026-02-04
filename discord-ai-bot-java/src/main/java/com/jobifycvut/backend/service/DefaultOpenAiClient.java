package com.jobifycvut.backend.service;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DefaultOpenAiClient implements OpenAiClient {
    @Override
    public String chatJson(String apiKey, String model, List<Map<String, String>> messages, JsonObject jsonSchema)
            throws IOException {
        OpenAiChatClient client = new OpenAiChatClient(apiKey, model);
        return client.chatJson(messages, jsonSchema);
    }
}
