package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.StudentDAO;
import util.PdfUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler extends ListenerAdapter {

    private final GPTClient gpt;

    private static final Map<String, Integer> userSteps = new HashMap<>();

    public static void startRegistrationFor(String userId) {
        userSteps.put(userId, 1);
    }

    public CommandHandler(GPTClient gpt) {
        this.gpt = gpt;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());
        for (var guild : event.getJDA().getGuilds()) {
            if (guild.getDefaultChannel() instanceof TextChannel channel && channel.canTalk()) {
                channel.sendMessage("👋 **JOBIFY CVUT Bot is now online and ready to help!**")
                        .setActionRow(Button.primary("start", "🚀 Get Started"))
                        .queue();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().trim();

        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ Bot is operational.").queue();
            return;
        }

        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            event.getChannel()
                    .sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!** Check your DMs to begin registration.")
                    .queue();

            event.getAuthor().openPrivateChannel().queue(dm -> {
                dm.sendMessage("👋 Welcome! Choose an option:")
                        .setActionRow(
                                Button.primary("gpt_ask", "🤖 Ask GPT"),
                                Button.primary("view_profile", "👤 View Profile"),
                                Button.success("create_profile", "📝 Create Profile")
                        )
                        .queue();
            });
            return;
        }

        if (event.isFromType(ChannelType.PRIVATE)) {

            // ✅ Si el usuario sube un archivo (PDF esperado)
            if (!event.getMessage().getAttachments().isEmpty()) {
                handlePdfUploadStep(event, userId);
                return;
            }

            // ✅ GPT pregunta
            if (content.startsWith("!ask ") && gpt != null) {
                String question = content.substring(5).trim();
                event.getChannel().sendTyping().queue();
                List<Map<String, String>> messages = List.of(
                        Map.of("role", "system", "content", "You are a helpful career advisor."),
                        Map.of("role", "user", "content", question)
                );
                try {
                    String aiReply = gpt.ask(messages, "gpt-3.5-turbo");
                    event.getChannel().sendMessage(aiReply).queue();
                } catch (IOException e) {
                    event.getChannel().sendMessage("⚠️ OpenAI error: " + e.getMessage()).queue();
                }
                return;
            }

            // ✅ Flujo paso a paso
            int step = userSteps.getOrDefault(userId, -1);
            switch (step) {
                case 1 -> {
                    handleEmailStep(event, userId, content);
                    userSteps.put(userId, 2);
                }
                case 2 -> {
                    handleNameStep(event, userId, content);
                    userSteps.remove(userId);
                }
            }
        }
    }

    public static void handleEmailStep(MessageReceivedEvent event, String userId, String email) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            event.getChannel().sendMessage("❗ Invalid email format, please retry.").queue();
            return;
        }
        try {
            StudentDAO.upsertStudent(null, email, null, null, userId, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.getChannel().sendMessage("👤 Please enter your full name.").queue();
    }

    public static void handleNameStep(MessageReceivedEvent event, String userId, String name) {
        try {
            StudentDAO.upsertStudent(name, null, null, null, userId, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        promptSkillsSelection(event);
    }

    public static void promptSkillsSelection(MessageReceivedEvent event) {
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

    public static void promptPositionSelection(MessageReceivedEvent event) {
        StringSelectMenu positionMenu = StringSelectMenu.create("select_position")
                .setPlaceholder("📌 Select up to 5 positions")
                .setMaxValues(5)
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

    public static void showMainMenu(User user) {
        user.openPrivateChannel().queue(dm -> {
            dm.sendMessage("💼 What would you like to do next?")
                    .setActionRow(
                            Button.primary("gpt_ask", "🤖 Ask GPT"),
                            Button.primary("view_profile", "👤 View Profile"),
                            Button.success("create_profile", "📝 Create Profile")
                    ).queue();
        });
    }

    public static void handleResumeDescriptionStep(MessageReceivedEvent event, String userId, String description) {
        try {
            StudentDAO.upsertStudent(null, null, null, null, userId, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        event.getChannel().sendMessage("📄 Please upload your resume as a PDF file.").queue();
    }

    public static void handlePdfUploadStep(MessageReceivedEvent event, String userId) {
        if (event.getMessage().getAttachments().isEmpty()) {
            event.getChannel().sendMessage("❗ Attach a PDF file please.").queue();
            return;
        }
        var attachment = event.getMessage().getAttachments().get(0);
        if (!attachment.getFileName().toLowerCase().endsWith(".pdf")) {
            event.getChannel().sendMessage("❌ Only PDF files are accepted.").queue();
            return;
        }
        File dir = new File("resumes");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, userId + ".pdf");

        attachment.downloadToFile(out)
                .thenRun(() -> {
                    try {
                        String extractedText = PdfUtils.extractText(out);
                        StudentDAO.updateCvTextByDiscordId(userId, extractedText);
                        System.out.println("✅ Text saved in DB for " + userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    event.getChannel().sendMessage("✅ PDF resume received and processed.").queue();
                    showMainMenu(event.getAuthor());

                })
                .exceptionally(ex -> {
                    event.getChannel().sendMessage("❌ Error uploading PDF. Please try again.").queue();
                    return null;
                });
    }
}
