package bot.api;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OpportunityClient {

    private static final String API_URL = "https://experts.ai/ai.unico.platform.rest/api/common/edumatch/318923/opportunity";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static Set<Opportunity> searchMultipleKeywords(String keywords) {
        Set<Opportunity> allResults = new HashSet<>();
        String[] terms = keywords.toLowerCase().replace(",", " ").split("\\s+");

        for (String term : terms) {
            if (term.isBlank()) continue;
            try {
                List<Opportunity> partial = search(term);
                allResults.addAll(partial);
            } catch (IOException e) {
                System.out.println("❌ Error searching for keyword '" + term + "': " + e.getMessage());
            }
        }
        return allResults;
    }

    private static List<Opportunity> search(String query) throws IOException {
        System.out.println("🔍 Searching for: " + query);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_URL).newBuilder();
        urlBuilder.addQueryParameter("query", query);
        urlBuilder.addQueryParameter("page", "1");
        urlBuilder.addQueryParameter("limit", "5");
        urlBuilder.addQueryParameter("includeApplications", "false");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("API error: " + response.code());

            JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();

            if (!root.has("opportunityPreviewDtos") || root.get("opportunityPreviewDtos").isJsonNull()) {
                return List.of();
            }

            JsonArray items = root.getAsJsonArray("opportunityPreviewDtos");
            List<Opportunity> results = new ArrayList<>();

            for (JsonElement el : items) {
                JsonObject obj = el.getAsJsonObject();

                String name = obj.get("opportunityName").getAsString();
                String description = obj.get("opportunityDescription").getAsString();

                String company = "Unknown";
                if (obj.has("organizationBaseDtos")) {
                    JsonArray orgs = obj.getAsJsonArray("organizationBaseDtos");
                    if (orgs.size() > 0) {
                        JsonObject org = orgs.get(0).getAsJsonObject();
                        company = org.get("organizationName").getAsString();
                    }
                }

                String jobType = "N/A";
                if (obj.has("jobTypes") && obj.get("jobTypes").isJsonArray()) {
                    JsonArray jobArray = obj.getAsJsonArray("jobTypes");
                    if (jobArray.size() > 0) jobType = "Type " + jobArray.get(0).getAsInt();
                }

                String deadline = "N/A";
                if (obj.has("opportunitySignupDate") && !obj.get("opportunitySignupDate").isJsonNull()) {
                    long ts = obj.get("opportunitySignupDate").getAsLong();
                    deadline = dateFormat.format(new Date(ts));
                }

                results.add(new Opportunity(name, company, jobType, deadline, description));
            }

            return results;
        }
    }

    public static class Opportunity {
        public final String title, company, type, deadline, description;

        public Opportunity(String title, String company, String type, String deadline, String description) {
            this.title = title;
            this.company = company;
            this.type = type;
            this.deadline = deadline;
            this.description = description;
        }

        @Override
        public String toString() {
            return "📌 **" + title + "** at " + company + "\n"
                    + "💼 " + type + " | 📅 " + deadline + "\n"
                    + description.substring(0, Math.min(description.length(), 1000)).replace("\n", " ") + "...\n";
        }

        // 🔁 Para evitar duplicados en HashSet
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Opportunity other)) return false;
            return Objects.equals(title, other.title) && Objects.equals(company, other.company);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, company);
        }
    }
}
