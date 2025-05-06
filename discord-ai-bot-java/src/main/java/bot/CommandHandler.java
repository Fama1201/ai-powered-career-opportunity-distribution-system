package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler extends ListenerAdapter {

    // GPTClient instance used to query OpenAI
    private final GPTClient gpt;

    // Constructor injects the GPT client
    public CommandHandler(GPTClient gpt) {
        this.gpt = gpt;
    }

    /**
     * Fired when the bot becomes ready.
     * Logs the bot's tag and notifies each guild default channel.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());
        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.getDefaultChannel() instanceof TextChannel channel && channel.canTalk()) {
                channel.sendMessage("👋 **EXPERTS.AI Bot is now online and ready to help!**").queue();
            }
        });
    }

    /**
     * Called on every message received.
     * Handles status, start command, GPT questions, and the profile collection flow.
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Always reload profiles from storage to get the latest data
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();

        // Ignore messages from other bots
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().trim();

        // Handle simple status check command
        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ I'm alive and ready!").queue();
            return;
        }

        // Handle start command in a guild channel
        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            event.getChannel()
                    .sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!** Check your DMs to get started!")
                    .queue();

            // Open a private DM channel and initialize the user's profile
            event.getAuthor().openPrivateChannel().queue(dm -> {
                Map<String, Object> profile = new HashMap<>();
                profile.put("step", 0);                // Start at step 0
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                // Ask the first question about having a resume
                dm.sendMessage("📄 Do you have a resume (CV)?")
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no",  "❌ No")
                        )
                        .queue();
            });
            return;
        }

        // Only handle further input in private (DM) channels
        if (event.isFromType(ChannelType.PRIVATE)) {
            // 1) Free-form GPT command: "!ask <question>"
            if (content.startsWith("!ask ")) {
                String question = content.substring(5).trim();
                event.getChannel().sendTyping().queue();

                // Build the chat messages payload
                List<Map<String, String>> msgs = List.of(
                        Map.of("role",    "system", "content", "You are a helpful career advisor."),
                        Map.of("role",    "user",   "content", question)
                );

                try {
                    // Query the AI model (using GPT-3.5 Turbo by default)
                    String aiResponse = gpt.ask(msgs, "gpt-3.5-turbo");
                    event.getChannel().sendMessage(aiResponse).queue();
                } catch (IOException e) {
                    // Inform the user if the API call fails
                    event.getChannel()
                            .sendMessage("⚠️ Error calling OpenAI: " + e.getMessage())
                            .queue();
                }
                return;
            }

            // 2) Profile collection flow based on "step"
            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            int step = ((Number) profile.getOrDefault("step", 0)).intValue();
            String input = content;

            switch (step) {
                case 1 -> {
                    // User provided name
                    profile.put("name", input);
                    profile.put("step", 2);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask for skills via a select menu
                    StringSelectMenu menu = StringSelectMenu.create("select_skills")
                            .setPlaceholder("💻 Select up to 5 skills")
                            .setMaxValues(5)
                            .addOption("Java", "java")
                            .addOption("Python", "python")
                            .addOption("JavaScript", "javascript")
                            .addOption("React", "react")
                            .addOption("Spring Boot", "spring")
                            .addOption("Node.js", "node")
                            .addOption("C++", "cpp")
                            .addOption("SQL", "sql")
                            .addOption("Git", "git")
                            .addOption("Docker", "docker")
                            .addOption("Other", "other")
                            .build();

                    event.getChannel()
                            .sendMessage("💻 **2. What are your top skills or technologies?**")
                            .setActionRow(menu)
                            .queue();
                }
                case 2 -> {
                    // User typed skills manually (fallback for "Other")
                    profile.put("skills", input);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Prompt for position type
                    StringSelectMenu menu = StringSelectMenu.create("select_position")
                            .setPlaceholder("📌 Choose your preferred position")
                            .setMaxValues(1)
                            .addOption("Backend", "backend")
                            .addOption("Frontend", "frontend")
                            .addOption("Full Stack", "fullstack")
                            .addOption("Mobile", "mobile")
                            .addOption("QA", "qa")
                            .addOption("DevOps", "devops")
                            .addOption("Data Science", "data")
                            .addOption("Other", "other")
                            .build();

                    event.getChannel()
                            .sendMessage("🧾 **3. What type of position are you looking for?**")
                            .setActionRow(menu)
                            .queue();
                }
                case 4 -> {
                    // User selected a position
                    profile.put("interests", input);
                    profile.put("step", 6);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask for resume details or link
                    event.getChannel()
                            .sendMessage("📄 **4. Optionally, describe your resume or paste a link**")
                            .queue();
                }
                case 6 -> {
                    // Final step: user provided resume info
                    profile.put("resume", input);
                    profile.put("step", -1);  // Mark flow as complete
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Notify user of completion
                    event.getChannel()
                            .sendMessage("🎉 **Your profile is complete!** You’ll receive opportunities soon.")
                            .queue();
                }
            }
        }
    }
}
