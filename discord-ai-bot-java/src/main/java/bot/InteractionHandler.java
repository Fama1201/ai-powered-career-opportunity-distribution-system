package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;
import storage.StudentDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all user interactions:
 * - Button clicks: start, gpt_ask, view_profile, create_profile, cv_yes, cv_no
 * - Select menus: select_skills, select_position
 */
public class InteractionHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (id) {
            case "start" -> {
                event.reply("📩 Check your DMs to continue.")
                        .setEphemeral(true)
                        .queue();

                event.getUser().openPrivateChannel().queue(dm -> {
                    dm.sendMessage("👋 Welcome! Choose an option:")
                            .setActionRow(
                                    Button.primary("gpt_ask", "🤖 Ask GPT"),
                                    Button.primary("view_profile", "👤 View Profile"),
                                    Button.success("create_profile", "📝 Create Profile")
                            )
                            .queue();
                });
            }

            case "gpt_ask" -> {
                event.reply("✍️ You can ask the AI by typing `!ask <your question>` here.")
                        .setEphemeral(true)
                        .queue();
            }

            case "view_profile" -> {
                event.deferReply(true).queue();
                try {
                    var data = StudentDAO.getStudentProfile(userId);
                    if (data == null || data.isEmpty()) {
                        event.getHook().sendMessage("⚠️ You don't have a profile yet. Select 'Create Profile' to start.").queue();
                    } else {
                        StringBuilder sb = new StringBuilder("**Your Profile**\n");
                        data.forEach((k, v) -> {
                            if (v != null && !v.isBlank()) {
                                sb.append("**").append(k).append(":** ").append(v).append("\n");
                            }
                        });
                        event.getHook().sendMessage(sb.toString()).queue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Error retrieving profile.").queue();
                }
            }

            case "create_profile" -> {
                profile.put("step", 0);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                event.reply("📄 Do you have a resume (CV)?")
                        .setEphemeral(true)
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no", "❌ No")
                        )
                        .queue();
            }

            case "cv_yes" -> {
                profile.put("step", 7);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                event.reply("📄 Please upload your resume as a PDF.")
                        .setEphemeral(true)
                        .queue();
            }

            case "cv_no" -> {
                profile.put("resume", "No CV provided");
                profile.put("step", 1);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                event.reply("📧 Please enter your email address.")
                        .setEphemeral(true)
                        .queue();
            }

            default -> event.reply("⚠️ Unrecognized button.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "select_skills" -> {
                List<String> values = event.getValues();
                if (values.contains("other")) {
                    profile.put("step", 3);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✍️ Please type your skills manually.")
                            .setEphemeral(true)
                            .queue();
                } else {
                    String skills = String.join(", ", values);
                    profile.put("skills", skills);
                    try {
                        StudentDAO.upsertStudent(null, null, skills, null, userId, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    event.reply("✅ Skills saved.")
                            .setEphemeral(true)
                            .queue();

                    StringSelectMenu posMenu = StringSelectMenu.create("select_position")
                            .setPlaceholder("📌 Choose your preferred position")
                            .setMaxValues(5)
                            .addOption("Backend", "backend")
                            .addOption("Frontend", "frontend")
                            .addOption("Full Stack", "fullstack")
                            .addOption("Mobile", "mobile")
                            .addOption("QA", "qa")
                            .addOption("DevOps", "devops")
                            .addOption("Data Science", "data")
                            .addOption("Other", "other")
                            .build();

                    event.getChannel().sendMessage("🧾 What type of position are you looking for?")
                            .setActionRow(posMenu)
                            .queue();
                }
            }

            case "select_position" -> {
                String selected = event.getValues().get(0);
                if (selected.equals("other")) {
                    profile.put("step", 5);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✍️ Please type your preferred position.")
                            .setEphemeral(true)
                            .queue();
                } else {
                    profile.put("career_interest", selected);
                    try {
                        StudentDAO.upsertStudent(null, null, null, selected, userId, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    profile.put("step", 6);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✅ Position saved. Please describe your resume or paste a link.")
                            .setEphemeral(true)
                            .queue();
                }
            }

            default -> event.reply("⚠️ Unknown select menu.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
