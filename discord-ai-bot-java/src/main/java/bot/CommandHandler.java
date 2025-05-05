package bot;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles Discord events like when the bot becomes ready
 * or when a message is received in a server or DM.
 */
public class CommandHandler extends ListenerAdapter {

    // Load all user profiles from file into memory (persistent onboarding data)
    private final Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();

    /**
     * This method is triggered once the bot has fully connected to Discord.
     * It sends a welcome message in each server's default text channel.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());

        event.getJDA().getGuilds().forEach(guild -> {
            // Check if the default channel exists and is a text channel
            if (guild.getDefaultChannel() instanceof TextChannel channel) {
                // Only send the welcome message if the bot has permission to talk
                if (channel.canTalk()) {
                    channel.sendMessage("👋 **EXPERTS.AI Bot is now online and ready to help!**").queue();
                }
            }
        });
    }

    /**
     * This method is triggered whenever a message is received
     * either in a guild (server) or in a private message (DM).
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Get the raw message content and trim leading/trailing whitespace
        String content = event.getMessage().getContentRaw().trim();

        // Ignore messages from bots (including itself)
        if (event.getAuthor().isBot()) return;

        // ✅ Command: "!status" — replies publicly with bot's availability
        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ I'm alive and ready!").queue();
            return;
        }

        // ✅ Command: "!start" — public message triggers private onboarding flow
        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            // Public confirmation
            event.getChannel().sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!**\n\nCheck your DMs to get started!").queue();

            // Open a DM channel with the user and start the conversation
            event.getAuthor().openPrivateChannel().queue(dm -> {
                dm.sendMessage("👋 Hi there! What’s your full name?").queue();

                // Reset user profile (start onboarding from step 1)
                Map<String, Object> newProfile = new HashMap<>();
                newProfile.put("step", 1);
                userProfiles.put(event.getAuthor().getId(), newProfile);
                ProfileStorage.saveProfiles(userProfiles);
            });

            return;
        }

        // ✅ Private message flow (user continues onboarding via DM)
        if (!event.isFromGuild()) {
            String userId = event.getAuthor().getId();

            // Load existing profile or create new one
            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            String contentTrimmed = content.trim();

            // Determine the current onboarding step (default to 1)
            Object stepObj = profile.getOrDefault("step", 1);
            int step = (stepObj instanceof Number) ? ((Number) stepObj).intValue() : 1;

            // Onboarding logic: ask questions step-by-step
            switch (step) {
                case 1 -> {
                    // Save user's name and ask about their skills
                    profile.put("name", contentTrimmed);
                    profile.put("step", 2);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("💻 **2. What are your top skills or technologies?**\n_(Write up to 5, comma-separated)_").queue();
                }
                case 2 -> {
                    // Save skills and ask about interests
                    profile.put("skills", contentTrimmed);
                    profile.put("step", 3);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("🧾 **3. What type of opportunities are you interested in?**\n_(e.g., Backend, Mobile, QA, etc.)_").queue();
                }
                case 3 -> {
                    // Save interests and ask about resume
                    profile.put("interests", contentTrimmed);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("📄 **4. Do you have a resume you'd like to share?** (Paste link or describe)").queue();
                }
                case 4 -> {
                    // Save resume and show a mock matching opportunity
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
                    // Completion message and mark profile as completed
                    event.getChannel().sendMessage("🎉 **Your profile is complete!** You will receive personalized opportunities here. Type `!start` to begin again.").queue();
                    profile.put("step", -1); // mark as done
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                }
            }
        }
    }
}
