package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;
import storage.StudentDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommandHandler listens for guild and private messages.
 *
 * Supports:
 *   - !status health check
 *   - !start to initiate DM profile flow
 *   - !ask to query GPT advisor
 *   - Profile registration steps (1–7):
 *       1. Email
 *       2. Full Name
 *       3. Skills selection or manual entry
 *       4. Position preference
 *       5. Resume description or link
 *       7. PDF upload
 */
public class CommandHandler extends ListenerAdapter {

    private final GPTClient gpt;

    /**
     * @param gpt GPTClient instance for AI interactions
     */
    public CommandHandler(GPTClient gpt) {
        this.gpt = gpt;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());
        for (var guild : event.getJDA().getGuilds()) {
            if (guild.getDefaultChannel() instanceof TextChannel channel && channel.canTalk()) {
                channel.sendMessage("👋 **EXPERTS.AI Bot is now online and ready to help!**").queue();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().trim();
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();

        // === Status Command ===
        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ Bot is operational.").queue();
            return;
        }

        // === Start Command in Guild ===
        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            event.getChannel()
                    .sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!** Check your DMs to begin registration.")
                    .queue();
            event.getAuthor().openPrivateChannel().queue(dm -> {
                Map<String, Object> profile = new HashMap<>();
                profile.put("step", 0);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                dm.sendMessage("📄 Do you have a resume (CV)?")
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no",  "❌ No")
                        ).queue();
            });
            return;
        }

        // === Private Channel Flow ===
        if (event.isFromType(ChannelType.PRIVATE)) {
            // GPT Ask Command
            if (content.startsWith("!ask ") && gpt != null) {
                String question = content.substring(5).trim();
                event.getChannel().sendTyping().queue();
                List<Map<String,String>> messages = List.of(
                        Map.of("role","system", "content","You are a helpful career advisor."),
                        Map.of("role","user",   "content", question)
                );
                try {
                    String aiReply = gpt.ask(messages, "gpt-3.5-turbo");
                    event.getChannel().sendMessage(aiReply).queue();
                } catch (IOException e) {
                    event.getChannel().sendMessage("⚠️ OpenAI error: " + e.getMessage()).queue();
                }
                return;
            }

            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            int step = ((Number) profile.getOrDefault("step", 0)).intValue();

            switch (step) {
                case 1 -> handleEmailStep(event, userId, content, profile, userProfiles);
                case 2 -> handleNameStep(event, userId, content, profile, userProfiles);
                case 3 -> promptSkillsSelection(event);
                case 4 -> promptPositionSelection(event);
                case 5 -> handleResumeDescriptionStep(event, userId, content, profile, userProfiles);
                case 7 -> handlePdfUploadStep(event, userId, profile, userProfiles);
                default -> {
                    // No active step; ignore or log
                }
            }
        }
    }

    private void handleEmailStep(MessageReceivedEvent event, String userId, String email,
                                 Map<String,Object> profile, Map<String,Map<String,Object>> userProfiles) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            event.getChannel().sendMessage("❗ Invalid email format, please retry.").queue();
            return;
        }
        profile.put("email", email);
        try {
            StudentDAO.upsertStudent(null, email, null, null, userId, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        profile.put("step", 2);
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
        event.getChannel().sendMessage("👤 Please enter your full name.").queue();
    }

    private void handleNameStep(MessageReceivedEvent event, String userId, String name,
                                Map<String,Object> profile, Map<String,Map<String,Object>> userProfiles) {
        profile.put("name", name);
        try {
            StudentDAO.upsertStudent(name, null, null, null, userId, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        profile.put("step", 3);
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
        promptSkillsSelection(event);
    }

    private void promptSkillsSelection(MessageReceivedEvent event) {
        StringSelectMenu skillsMenu = StringSelectMenu.create("select_skills")
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
                .build();
        event.getChannel()
                .sendMessage("💻 What are your primary skills or technologies?")
                .setActionRow(skillsMenu)
                .queue();
    }

    private void promptPositionSelection(MessageReceivedEvent event) {
        StringSelectMenu positionMenu = StringSelectMenu.create("select_position")
                .setPlaceholder("📌 Choose your desired position")
                .setMaxValues(1)
                .addOption("Backend", "backend")
                .addOption("Frontend", "frontend")
                .addOption("Full Stack", "fullstack")
                .addOption("Mobile", "mobile")
                .addOption("QA", "qa")
                .addOption("DevOps", "devops")
                .addOption("Data Science", "data")
                .build();
        event.getChannel()
                .sendMessage("🧾 Which type of position are you seeking?")
                .setActionRow(positionMenu)
                .queue();
    }

    private void handleResumeDescriptionStep(MessageReceivedEvent event, String userId, String description,
                                             Map<String,Object> profile, Map<String,Map<String,Object>> userProfiles) {
        profile.put("resume", description);
        try {
            StudentDAO.upsertStudent(null, null, null, null, userId, description, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        profile.put("step", 7);
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
        event.getChannel().sendMessage("📄 Please upload your resume as a PDF file.").queue();
    }

    private void handlePdfUploadStep(MessageReceivedEvent event, String userId,
                                     Map<String,Object> profile, Map<String,Map<String,Object>> userProfiles) {
        if (event.getMessage().getAttachments().isEmpty()) {
            event.getChannel().sendMessage("❗ Attach a PDF file please.").queue();
            return;
        }
        var attachment = event.getMessage().getAttachments().get(0);
        if (!attachment.getFileName().toLowerCase().endsWith(".pdf")) {
            event.getChannel().sendMessage("❌ Only PDF files are accepted.").queue();
            return;
        }
        java.io.File dir = new java.io.File("resumes");
        if (!dir.exists()) dir.mkdirs();
        java.io.File out = new java.io.File(dir, userId + ".pdf");
        attachment.downloadToFile(out)
                .thenRun(() -> {
                    profile.put("resumePath", out.getAbsolutePath());
                    try {
                        StudentDAO.upsertStudent(null, null, null, null, userId, out.getAbsolutePath(), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    profile.put("step", -1);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("✅ PDF resume received. Thank you!").queue();
                })
                .exceptionally(ex -> {
                    event.getChannel().sendMessage("❌ Error uploading PDF. Please try again.").queue();
                    return null;
                });
    }
}
