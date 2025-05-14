package bot.api;

import com.google.gson.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class handles communication with the EXPERTS.AI opportunity API.
 * It fetches opportunities based on keywords and parses them into structured data.
 */
public class OpportunityClient {

    // API endpoint for fetching opportunities
    private static final String API_URL = "https://experts.ai/ai.unico.platform.rest/api/common/edumatch/318923/opportunity";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Searches for opportunities based on multiple keywords.
     * Each keyword is sent to the API individually (up to 3 pages per keyword).
     * @param keywords A space- or comma-separated string of keywords.
     * @return A set of opportunities collected from all keyword searches.
     */
    public static Set<Opportunity> searchMultipleKeywords(String keywords) {
        Set<Opportunity> allResults = new HashSet<>();
        String[] terms = keywords.toLowerCase().replace(",", " ").split("\\s+");

        for (String term : terms) {
            if (term.isBlank()) continue;
            for (int page = 1; page <= 3; page++) {
                System.out.println("🔎 Searching '" + term + "' page " + page);

                try {
                    List<Opportunity> partial = search(term, page);
                    allResults.addAll(partial);
                    // If the API returns fewer than 5 results, assume no more pages
                    if (partial.size() < 5) break;
                } catch (IOException e) {
                    System.out.println("❌ Error searching for keyword '" + term + "' on page " + page + ": " + e.getMessage());
                }
            }
        }
        System.out.println("✅ Total opportunities found: " + allResults.size());

        return allResults;
    }

    /**
     * Performs a search query to the opportunity API for a specific keyword and page.
     * Parses the result JSON into a list of Opportunity objects.
     * @param query The keyword to search for.
     * @param page The page number (pagination).
     * @return A list of parsed Opportunity objects.
     * @throws IOException if the API call fails.
     */
    private static List<Opportunity> search(String query, int page) throws IOException {
        System.out.println("🔍 Searching for: " + query + " (page " + page + ")");

        HttpUrl.Builder urlBuilder = HttpUrl.parse(API_URL).newBuilder();
        urlBuilder.addQueryParameter("query", query);
        urlBuilder.addQueryParameter("page", String.valueOf(page));
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

                // Required fields
                String id = obj.get("opportunityId").getAsString();
                String name = obj.get("opportunityName").getAsString();
                String description = obj.get("opportunityDescription").getAsString();

                // Optional: Company name
                String company = "Unknown";
                if (obj.has("organizationBaseDtos")) {
                    JsonArray orgs = obj.getAsJsonArray("organizationBaseDtos");
                    if (orgs.size() > 0) {
                        JsonObject org = orgs.get(0).getAsJsonObject();
                        company = org.get("organizationName").getAsString();
                    }
                }

                // Optional: Job type
                String jobType = "N/A";
                if (obj.has("jobTypes") && obj.get("jobTypes").isJsonArray()) {
                    JsonArray jobArray = obj.getAsJsonArray("jobTypes");
                    if (jobArray.size() > 0) jobType = "Type " + jobArray.get(0).getAsInt();
                }

                // Optional: Deadline
                String deadline = "N/A";
                if (obj.has("opportunitySignupDate") && !obj.get("opportunitySignupDate").isJsonNull()) {
                    long ts = obj.get("opportunitySignupDate").getAsLong();
                    deadline = dateFormat.format(new Date(ts));
                }

                // Other optional fields with fallbacks
                String extLink = obj.has("opportunityExtLink") && !obj.get("opportunityExtLink").isJsonNull()
                        ? obj.get("opportunityExtLink").getAsString()
                        : "";

                String wage = obj.has("opportunityWage") && !obj.get("opportunityWage").isJsonNull()
                        ? obj.get("opportunityWage").getAsString()
                        : "";

                String homeOffice = obj.has("opportunityHomeOffice") && !obj.get("opportunityHomeOffice").isJsonNull()
                        ? obj.get("opportunityHomeOffice").getAsString()
                        : "";

                String benefits = obj.has("opportunityBenefit") && !obj.get("opportunityBenefit").isJsonNull()
                        ? obj.get("opportunityBenefit").getAsString()
                        : "";

                String formReq = obj.has("opportunityFormReq") && !obj.get("opportunityFormReq").isJsonNull()
                        ? obj.get("opportunityFormReq").getAsString()
                        : "";

                String techReq = obj.has("opportunityTechReq") && !obj.get("opportunityTechReq").isJsonNull()
                        ? obj.get("opportunityTechReq").getAsString()
                        : "";

                // Contact person (optional)
                String contact = "";
                if (obj.has("expertPreviews")) {
                    JsonArray contacts = obj.getAsJsonArray("expertPreviews");
                    if (contacts.size() > 0) {
                        JsonObject c = contacts.get(0).getAsJsonObject();
                        if (c.has("name") && !c.get("name").isJsonNull()) {
                            contact = c.get("name").getAsString();
                        }
                    }
                }

                // Add the parsed opportunity to the list
                results.add(new Opportunity(id, name, company, jobType, deadline, description, extLink,
                        wage, homeOffice, benefits, formReq, techReq, contact));
            }

            return results;
        }
    }

    /**
     * Data class representing a single opportunity.
     */
    public static class Opportunity {
        public final String id, title, company, type, deadline, description, url;
        public final String wage, homeOffice, benefits, formReq, techReq, contactPerson;

        public Opportunity(String id, String title, String company, String type, String deadline,
                           String description, String url,
                           String wage, String homeOffice, String benefits,
                           String formReq, String techReq, String contactPerson) {
            this.id = id;
            this.title = title;
            this.company = company;
            this.type = type;
            this.deadline = deadline;
            this.description = description;
            this.url = url;
            this.wage = wage;
            this.homeOffice = homeOffice;
            this.benefits = benefits;
            this.formReq = formReq;
            this.techReq = techReq;
            this.contactPerson = contactPerson;
        }

        /**
         * Converts the opportunity into a Discord MessageEmbed for rich formatting.
         */
        public MessageEmbed toEmbed() {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📌 " + title, url);
            embed.setDescription(description.length() > 500 ? description.substring(0, 500) + "..." : description);
            embed.addField("🏢 Company", company, true);
            embed.addField("💼 Type", type, true);
            embed.addField("📅 Deadline", deadline, true);

            if (!wage.isBlank()) embed.addField("💰 Salary", wage, true);
            if (!homeOffice.isBlank()) embed.addField("🏠 Home Office", homeOffice, true);
            if (!formReq.isBlank()) embed.addField("📚 Formal Req.", formReq, true);
            if (!techReq.isBlank()) embed.addField("🛠 Tech Req.", techReq, true);
            if (!benefits.isBlank()) embed.addField("🎁 Benefits", benefits.length() > 500 ? benefits.substring(0, 500) + "..." : benefits, false);
            if (!contactPerson.isBlank()) embed.addField("📞 Contact", contactPerson, true);

            embed.setColor(0x00AEEF); // CVUT blue
            embed.setFooter("Powered by EXPERTS.AI");

            return embed.build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Opportunity other)) return false;
            return Objects.equals(id, other.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
