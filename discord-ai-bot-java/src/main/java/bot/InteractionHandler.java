package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.StudentDAO;

import java.util.List;

public class InteractionHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        String userId = event.getUser().getId();

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
                event.reply("📄 Do you have a resume (CV)?")
                        .setEphemeral(true)
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no", "❌ No")
                        )
                        .queue();
            }

            case "cv_yes" -> {
                event.reply("📄 Please upload your resume as a PDF.")
                        .setEphemeral(true)
                        .queue();
            }

            case "cv_no" -> {
                try {
                    StudentDAO.upsertStudent(null, null, null, null, userId, "No CV provided", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Iniciar flujo paso a paso
                CommandHandler.startRegistrationFor(userId);

                event.reply("📧 Please enter your email address.")
                        .setEphemeral(true)
                        .queue();
            }

            default -> event.reply("⚠️ Unrecognized button.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String userId = event.getUser().getId();

        switch (event.getComponentId()) {
            case "select_skills" -> {
                List<String> values = event.getValues();
                String skills = String.join(", ", values);
                try {
                    StudentDAO.upsertStudent(null, null, skills, null, userId, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                        .build();

                event.getChannel().sendMessage("🧾 What type of position are you looking for?")
                        .setActionRow(posMenu)
                        .queue();
            }

            case "select_position" -> {
                String selected = event.getValues().get(0);
                try {
                    StudentDAO.upsertStudent(null, null, null, selected, userId, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                event.reply("✅ Position saved.")
                        .setEphemeral(true)
                        .queue();

                event.getUser().openPrivateChannel().queue(dm -> {
                    dm.sendMessage("✅ Your profile has been saved! What would you like to do next?")
                            .setActionRow(
                                    Button.primary("gpt_ask", "🤖 Ask GPT"),
                                    Button.primary("view_profile", "👤 View Profile"),
                                    Button.success("create_profile", "📝 Create Profile")
                            )
                            .queue();
                });
            }

            default -> event.reply("⚠️ Unknown select menu.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
