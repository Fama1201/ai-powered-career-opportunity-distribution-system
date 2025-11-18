package bot;

import bot.ai.GPTClient;
import bot.storage.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel; // Important Import
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.PdfUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Component
public class CommandHandler extends ListenerAdapter {

    private final GPTClient gpt;

    @Autowired private OpportunityDAO opportunityDAO;
    @Autowired private StudentDAO studentDAO;
    @Autowired private FeedbackDAO feedbackDAO;

    // Made static so InteractionHandler can access registration state
    private static final Map<String, Integer> userSteps = new HashMap<>();

    // Helper method for InteractionHandler
    public static void startRegistrationFor(String userId) {
        userSteps.put(userId, 1);
    }

    @Autowired
    public CommandHandler(@Autowired(required = false) GPTClient gptClient) {
        this.gpt = gptClient;
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

        if (content.startsWith("!clean ")) {
            try {
                String[] parts = content.split("\\s+");
                if (parts.length == 2) {
                    int count = Integer.parseInt(parts[1]);
                    if (count > 0 && count <= 100) {
                        event.getChannel().getHistory().retrievePast(count + 1).queue(messages -> {
                            event.getChannel().purgeMessages(messages);
                            event.getChannel().sendMessage("✅ Deleted " + count + " messages.")
                                    .queue(msg -> msg.delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS));
                        });
                    }
                }
            } catch (Exception e) { /* Ignore */ }
            return;
        }

        if (event.isFromType(ChannelType.PRIVATE)) {
            if (!event.getMessage().getAttachments().isEmpty()) {
                handlePdfUploadStep(event, userId);
                return;
            }

            if (content.equalsIgnoreCase("!fetch")) {
                handleFetchCommand(event, userId);
                return;
            }

            if (content.startsWith("!ask ") && gpt != null) {
                handleAskCommand(event, userId, content);
                return;
            }

            // Registration Steps
            int step = userSteps.getOrDefault(userId, -1);
            switch (step) {
                case 1 -> {
                    handleEmailStep(event.getChannel(), userId, content);
                    userSteps.put(userId, 2);
                }
                case 2 -> {
                    handleNameStep(event.getChannel(), userId, content);
                    userSteps.remove(userId);
                }
            }
        }
    }

    // --- Extracted Logic Methods ---

    private void handleFetchCommand(MessageReceivedEvent event, String userId) {
        try {
            Optional<Student> studentOpt = studentDAO.findByDiscordId(userId);
            if (studentOpt.isEmpty() || studentOpt.get().getSkills() == null) {
                event.getChannel().sendMessage("❗ You need to complete your profile first.").queue();
                return;
            }

            Student student = studentOpt.get();
            String skills = student.getSkills();
            String interest = student.getCareerInterest() != null ? student.getCareerInterest() : "";

            Set<bot.api.OpportunityClient.Opportunity> results =
                    bot.api.OpportunityClient.searchMultipleKeywords(skills + " " + interest);

            if (results.isEmpty()) {
                event.getChannel().sendMessage("😢 No opportunities found.").queue();
            } else {
                event.getChannel().sendMessage("🎯 Found " + results.size() + " opportunities:").queue();
                for (var opp : results) {
                    event.getChannel().sendMessageEmbeds(opp.toEmbed())
                            .setActionRow(Button.link(opp.url, "📩 Apply")).queue();

                    try {
                        if (!opportunityDAO.existsByOpportunityIdAndDiscordId(opp.id, userId)) {
                            Opportunity dbOpp = new Opportunity();
                            dbOpp.setOpportunityId(opp.id);
                            dbOpp.setDiscordId(userId);
                            dbOpp.setTitle(opp.title);
                            dbOpp.setDescription(opp.description);
                            dbOpp.setJobType(opp.type);
                            if (opp.deadline != null && !opp.deadline.isBlank()) {
                                try { dbOpp.setApplicationDeadline(LocalDate.parse(opp.deadline)); } catch (Exception ignored) {}
                            }
                            dbOpp.setUrl(opp.url);
                            dbOpp.setWage(opp.wage);
                            dbOpp.setHomeOffice(opp.homeOffice);
                            dbOpp.setBenefits(opp.benefits);
                            dbOpp.setFormalRequirements(opp.formReq);
                            dbOpp.setTechnicalRequirements(opp.techReq);
                            dbOpp.setContactPerson(opp.contactPerson);
                            dbOpp.setCompany(opp.company);
                            opportunityDAO.save(dbOpp);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage("❌ Error: " + e.getMessage()).queue();
        }
    }

    private void handleAskCommand(MessageReceivedEvent event, String userId, String content) {
        String question = content.substring(5).trim();
        event.getChannel().sendTyping().queue();
        StringBuilder profileInfo = new StringBuilder();
        StringBuilder opportunitiesInfo = new StringBuilder();

        try {
            studentDAO.findByDiscordId(userId).ifPresent(s -> {
                profileInfo.append("📄 Profile:\n");
                if (s.getName() != null) profileInfo.append("- Name: ").append(s.getName()).append("\n");
                if (s.getSkills() != null) profileInfo.append("- Skills: ").append(s.getSkills()).append("\n");
            });

            List<Opportunity> opps = opportunityDAO.findByDiscordId(userId);
            if (!opps.isEmpty()) {
                opportunitiesInfo.append("📌 Opportunities:\n");
                for (Opportunity o : opps) {
                    opportunitiesInfo.append("- ").append(o.getTitle()).append(" at ").append(o.getCompany()).append("\n");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "You are a career assistant."));
        String fullPrompt = profileInfo + "\n" + opportunitiesInfo + "\nQuestion: " + question;
        messages.add(Map.of("role", "user", "content", fullPrompt));

        try {
            String aiReply = gpt.ask(messages, "gpt-3.5-turbo");
            event.getChannel().sendMessage(aiReply.length() > 2000 ? aiReply.substring(0, 2000) : aiReply).queue();
        } catch (IOException e) {
            event.getChannel().sendMessage("⚠️ AI Error.").queue();
        }
    }

    // Updated to use MessageChannel so it works for both Chat and Buttons
    public void handleEmailStep(MessageChannel channel, String userId, String email) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            channel.sendMessage("❗ Invalid email format, please retry.").queue();
            return;
        }
        try {
            Student s = studentDAO.findByDiscordId(userId).orElse(new Student());
            s.setDiscordId(userId);
            s.setEmail(email);
            studentDAO.save(s);
        } catch (Exception e) { e.printStackTrace(); }
        channel.sendMessage("👤 Please enter your full name.").queue();
    }

    public void handleNameStep(MessageChannel channel, String userId, String name) {
        try {
            Student s = studentDAO.findByDiscordId(userId).orElse(new Student());
            s.setDiscordId(userId);
            s.setName(name);
            studentDAO.save(s);
        } catch (Exception e) { e.printStackTrace(); }
        promptSkillsSelection(channel);
    }

    public void handlePdfUploadStep(MessageReceivedEvent event, String userId) {
        var attachment = event.getMessage().getAttachments().get(0);
        File dir = new File("resumes");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, userId + ".pdf");

        try {
            attachment.getProxy().downloadToFile(out).join();
            String extractedText = PdfUtils.extractText(out);

            Student s = studentDAO.findByDiscordId(userId).orElse(new Student());
            s.setDiscordId(userId);
            s.setCvText(extractedText);
            studentDAO.save(s);

            if (gpt != null) {
                String prompt = "Extract JSON {name, email, skills, positions} from: " + extractedText;
                List<Map<String, String>> msgs = List.of(Map.of("role", "user", "content", prompt));
                String jsonStr = gpt.ask(msgs, "gpt-3.5-turbo");

                try {
                    JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                    if (json.has("name")) s.setName(json.get("name").getAsString());
                    if (json.has("email")) s.setEmail(json.get("email").getAsString());
                    if (json.has("skills")) s.setSkills(json.get("skills").toString());
                    if (json.has("positions")) s.setCareerInterest(json.get("positions").toString());
                    studentDAO.save(s);
                } catch (Exception ignored) {}
            }
            event.getChannel().sendMessage("✅ Processed.").queue(msg -> showMainMenu(event.getAuthor()));

        } catch (Exception e) {
            event.getChannel().sendMessage("⚠️ Error.").queue();
        }
    }

    // Helper methods updated to accept MessageChannel
    public static void promptSkillsSelection(MessageChannel channel) {
        StringSelectMenu skillsMenu = StringSelectMenu.create("select_skills")
                .setPlaceholder("Select Skills")
                .addOption("Java", "java").addOption("Python", "python").build();
        channel.sendMessage("Select your skills:").setActionRow(skillsMenu).queue();
    }

    public static void promptPositionSelection(MessageChannel channel) {
        StringSelectMenu posMenu = StringSelectMenu.create("select_position")
                .setPlaceholder("Select Position")
                .addOption("Backend", "backend").addOption("Frontend", "frontend").build();
        channel.sendMessage("Select position:").setActionRow(posMenu).queue();
    }

    public static void showMainMenu(User user) {
        user.openPrivateChannel().queue(dm -> {
            dm.sendMessage("Menu:")
                    .setActionRow(
                            Button.primary("view_profile", "View Profile"),
                            Button.success("create_profile", "Create Profile"),
                            Button.danger("delete_profile", "Delete")
                    ).queue();
        });
    }

    private String formatOpportunityEntity(bot.storage.Opportunity opp) {
        return String.format("🔹 **Title**: %s\n🏢 **Company**: %s\n", opp.getTitle(), opp.getCompany());
    }

    private static List<String> toList(JsonArray array) {
        List<String> list = new ArrayList<>();
        for (JsonElement el : array) list.add(el.getAsString());
        return list;
    }
}