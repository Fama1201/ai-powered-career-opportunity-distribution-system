package bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler extends ListenerAdapter {

    private final Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();

        if (event.getAuthor().isBot()) return;

        if (content.equalsIgnoreCase("!start")) {
            event.getChannel().sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!**\n\nType `!start` in any public channel and I'll DM you the onboarding form.").queue();
            event.getAuthor().openPrivateChannel().queue(dm -> {
                dm.sendMessage("👋 Hi there! What’s your full name?").queue();
            });
            return;
        }

        if (!event.isFromGuild()) {
            String userId = event.getAuthor().getId();
            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            String contentTrimmed = content.trim();
            Object stepObj = profile.getOrDefault("step", 1);
            int step = (stepObj instanceof Number) ? ((Number) stepObj).intValue() : 1;

            switch (step) {
                case 1 -> {
                    profile.put("name", contentTrimmed);
                    profile.put("step", 2);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("💻 **2. What are your top skills or technologies?**\n_(Write up to 5, comma-separated)_").queue();
                }
                case 2 -> {
                    profile.put("skills", contentTrimmed);
                    profile.put("step", 3);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("🧾 **3. What type of opportunities are you interested in?**\n_(e.g., Backend, Mobile, QA, etc.)_").queue();
                }
                case 3 -> {
                    profile.put("interests", contentTrimmed);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("📄 **4. Do you have a resume you'd like to share?** (Paste link or describe)").queue();
                }
                case 4 -> {
                    profile.put("resume", contentTrimmed);
                    profile.put("step", 5);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    event.getChannel().sendMessage("🔍 Searching for opportunities that match you...").queue();
                    event.getChannel().sendMessage(
                            "🎯 **Found an Opportunity That Matches You!**\n\n" +
                                    "🔹 **Role:** Backend Developer Intern\n" +
                                    "🏢 **Company:** NovaTech Solutions\n" +
                                    "📍 **Location:** Remote\n" +
                                    "💼 **Stack:** Java, Spring Boot, PostgreSQL"
                    ).queue();
                }
                case 5 -> {
                    event.getChannel().sendMessage("🎉 **Your profile is complete!** You will receive personalized opportunities here. Type `!start` to begin again.").queue();
                    profile.put("step", -1);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                }
            }
        }
    }
}
