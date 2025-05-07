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

/**
 * CommandHandler handles incoming messages both in guild channels (to start the flow)
 * and in private DMs (to collect profile data, accept PDF uploads, or process GPT queries).
 */
public class CommandHandler extends ListenerAdapter {

    // GPT client for handling free-form AI queries
    private final GPTClient gpt;

    // Constructor injection of the GPT client (may be null if AI is disabled)
    public CommandHandler(GPTClient gpt) {
        this.gpt = gpt;
    }

    /**
     * Called once when the bot comes online.
     * Logs the bot tag and notifies each guild's default channel.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());
        for (var guild : event.getJDA().getGuilds()) {
            if (guild.getDefaultChannel() instanceof TextChannel channel && channel.canTalk()) {
                channel.sendMessage("👋 **EXPERTS.AI Bot is now online and ready to help!**").queue();
            }
        }
    }

    /**
     * Called for every message received.
     * - In guilds: listens for "!status" and "!start".
     * - In DMs: handles "!ask", profile steps 1–6, and PDF upload step (7).
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Reload profiles on each message to keep state in sync
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();

        // Ignore bot messages
        if (event.getAuthor().isBot()) return;

        String userId  = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().trim();

        // --- GLOBAL COMMANDS ---

        // !status: quick health check
        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ I'm alive and ready!").queue();
            return;
        }

        // !start in a guild channel: initialize DM flow
        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            event.getChannel()
                    .sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!** Check your DMs to get started!")
                    .queue();

            // Open DM and begin at step 0
            event.getAuthor().openPrivateChannel().queue(dm -> {
                Map<String, Object> profile = new HashMap<>();
                profile.put("step", 0);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                dm.sendMessage("📄 Do you have a resume (CV)?")
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger ("cv_no",  "❌ No")
                        )
                        .queue();
            });
            return;
        }

        // --- DM-SPECIFIC HANDLING ---
        if (event.isFromType(ChannelType.PRIVATE)) {

            // 1) Free-form AI command: "!ask <question>"
            if (content.startsWith("!ask ") && gpt != null) {
                String question = content.substring(5).trim();
                event.getChannel().sendTyping().queue();

                // Build messages for the AI model
                List<Map<String, String>> msgs = List.of(
                        Map.of("role",    "system", "content", "You are a helpful career advisor."),
                        Map.of("role",    "user",   "content", question)
                );

                try {
                    String aiReply = gpt.ask(msgs, "gpt-3.5-turbo");
                    event.getChannel().sendMessage(aiReply).queue();
                } catch (IOException e) {
                    event.getChannel()
                            .sendMessage("⚠️ Error calling OpenAI: " + e.getMessage())
                            .queue();
                }
                return;
            }

            // 2) Profile and PDF upload flow based on 'step'
            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            int step = ((Number) profile.getOrDefault("step", 0)).intValue();
            String input = content;

            switch (step) {
                case 1 -> { // User provided full name
                    profile.put("name", input);
                    profile.put("step", 2);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask for skills via select menu
                    StringSelectMenu menu = StringSelectMenu.create("select_skills")
                            .setPlaceholder("💻 Select up to 5 skills")
                            .setMaxValues(5)
                            .addOption("Java",       "java")
                            .addOption("Python",     "python")
                            .addOption("JavaScript", "javascript")
                            .addOption("React",      "react")
                            .addOption("Spring Boot","spring")
                            .addOption("Node.js",    "node")
                            .addOption("C++",        "cpp")
                            .addOption("SQL",        "sql")
                            .addOption("Git",        "git")
                            .addOption("Docker",     "docker")
                            .addOption("Other",      "other")
                            .build();

                    event.getChannel()
                            .sendMessage("💻 **2. What are your top skills or technologies?**")
                            .setActionRow(menu)
                            .queue();
                }

                case 2 -> { // Manual skills input (fallback for “Other”)
                    profile.put("skills", input);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Move to position selection
                    StringSelectMenu menu = StringSelectMenu.create("select_position")
                            .setPlaceholder("📌 Choose your preferred position")
                            .setMaxValues(1)
                            .addOption("Backend",      "backend")
                            .addOption("Frontend",     "frontend")
                            .addOption("Full Stack",   "fullstack")
                            .addOption("Mobile",       "mobile")
                            .addOption("QA",           "qa")
                            .addOption("DevOps",       "devops")
                            .addOption("Data Science", "data")
                            .addOption("Other",        "other")
                            .build();

                    event.getChannel()
                            .sendMessage("🧾 **3. What type of position are you looking for?**")
                            .setActionRow(menu)
                            .queue();
                }

                case 3 -> { // Selected skills via menu: same as case 2
                    profile.put("skills", input);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask for position as in case 2
                    onMessageReceived(event); // re-enter case 2 logic
                }

                case 4 -> { // Position chosen
                    profile.put("interests", input);
                    profile.put("step", 6);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask for resume details or link
                    event.getChannel()
                            .sendMessage("📄 **4. Optionally, describe your resume or paste a link**")
                            .queue();
                }

                case 6 -> { // Resume description or link provided
                    profile.put("resume", input);
                    profile.put("step", -1); // flow complete (AI summary can follow)
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    event.getChannel()
                            .sendMessage("🎉 **Your profile is complete!** You’ll receive opportunities soon.")
                            .queue();
                }

                case 7 -> { // Awaiting PDF upload
                    // Ensure an attachment is present
                    if (event.getMessage().getAttachments().isEmpty()) {
                        event.getChannel()
                                .sendMessage("❗ Please attach a PDF file when uploading your resume.")
                                .queue();
                        break;
                    }
                    var attachment = event.getMessage().getAttachments().get(0);

                    // Validate PDF extension
                    if (!attachment.getFileName().toLowerCase().endsWith(".pdf")) {
                        event.getChannel()
                                .sendMessage("❌ The file must have a **.pdf** extension.")
                                .queue();
                        break;
                    }

                    // Create 'resumes' directory if needed
                    java.io.File dir = new java.io.File("resumes");
                    if (!dir.exists()) dir.mkdirs();

                    // Download the PDF to local storage
                    java.io.File outFile = new java.io.File(dir, userId + ".pdf");
                    attachment.downloadToFile(outFile)
                            .thenRun(() -> {
                                // Save the file path and finish the flow
                                profile.put("resumePath", outFile.getAbsolutePath());
                                profile.put("step", -1);
                                userProfiles.put(userId, profile);
                                ProfileStorage.saveProfiles(userProfiles);

                                event.getChannel()
                                        .sendMessage("✅ Your resume PDF has been received! We’ll DM you matching opportunities soon.")
                                        .queue();
                            })
                            .exceptionally(ex -> {
                                event.getChannel()
                                        .sendMessage("❌ Error uploading your resume. Please try again.")
                                        .queue();
                                return null;
                            });
                    break;
                }

                default -> {
                    // No action for other steps
                }
            }
        }
    }
}
