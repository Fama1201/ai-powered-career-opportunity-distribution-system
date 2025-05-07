package storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ProfileStorage {
    private static final String DATA_FILE = "user_profiles.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Map<String, Map<String, Object>> loadProfiles() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void saveProfiles(Map<String, Map<String, Object>> profiles) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(DATA_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(profiles, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
