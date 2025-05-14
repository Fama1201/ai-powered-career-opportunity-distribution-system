package bot;

import bot.api.OpportunityClient;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.StudentDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InteractionHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        String userId = event.getUser().getId();

        switch (id) {
            case "start" -> {
                event.reply("\uD83D\uDCEC Check your DMs to continue.")
                        .setEphemeral(true)
                        .queue(success -> event.getUser().openPrivateChannel().queue(dm -> {
                            dm.sendMessage("\uD83D\uDC4B Welcome! Choose an option:")
                                    .setActionRow(
                                            Button.primary("gpt_ask", "\uD83E\uDD16 Ask GPT"),
                                            Button.primary("view_profile", "\uD83D\uDC64 View Profile"),
                                            Button.success("create_profile", "\uD83D\uDCDD Create Profile"),
                                            Button.secondary("match_jobs", "\uD83C\uDFAF Match Me")
                                    )
                                    .queue();
                        }));
            }

            case "gpt_ask" -> event.reply("\u270D\uFE0F You can ask the AI by typing `!ask <your question>` here.")
                    .setEphemeral(true).queue();

            case "view_profile" -> {
                event.deferReply(true).queue();
                try {
                    var data = StudentDAO.getStudentProfile(userId);
                    if (data == null || data.isEmpty()) {
                        event.getHook().sendMessage("\u26A0\uFE0F You don't have a profile yet. Select 'Create Profile' to start.").queue();
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

            case "create_profile" -> event.reply("\uD83D\uDCC4 Do you have a resume (CV)?")
                    .setEphemeral(true)
                    .setActionRow(
                            Button.success("cv_yes", "✅ Yes"),
                            Button.danger("cv_no", "❌ No")
                    ).queue();

            case "cv_yes" -> event.reply("\uD83D\uDCC4 Please upload your resume as a PDF.")
                    .setEphemeral(true).queue();

            case "cv_no" -> {
                event.deferReply(true).queue();
                try {
                    StudentDAO.upsertStudent(null, null, null, null, userId, null);
                    CommandHandler.startRegistrationFor(userId);
                    event.getHook().sendMessage("\uD83D\uDCE7 Please enter your email address.").queue();
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Failed to initialize profile setup.").queue();
                }
            }

            case "match_jobs" -> {
                event.deferReply(true).queue();
                try {
                    var profile = StudentDAO.getStudentProfile(userId);

                    if (profile == null || profile.get("Skills") == null || profile.get("Career Interest") == null) {
                        event.getHook().sendMessage("❗ You need to complete your profile first.").queue();
                        return;
                    }

                    String skills = profile.get("Skills");
                    String interest = profile.get("Career Interest");

                    Set<OpportunityClient.Opportunity> results =
                            OpportunityClient.searchMultipleKeywords(skills + " " + interest);

                    if (results.isEmpty()) {
                        event.getHook().sendMessage("😢 No opportunities found for your profile.").queue();
                    } else {
                        event.getHook().sendMessage("🎯 Found " + results.size() + " opportunities for you:").queue();
                        for (var opp : results) {
                            if (!storage.OpportunityDAO.existsForUser(opp, userId)) {
                                storage.OpportunityDAO.insertForUser(opp, userId);
                            }
                            if (!opp.url.isBlank()) {
                                event.getChannel().sendMessageEmbeds(opp.toEmbed())
                                        .addActionRow(Button.link(opp.url, "📩 Apply"))
                                        .queue();
                            } else {
                                event.getChannel().sendMessageEmbeds(opp.toEmbed()).queue();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Error matching opportunities: " + e.getMessage()).queue();
                }
            }

            default -> event.reply("\u26A0\uFE0F Unrecognized button.")
                    .setEphemeral(true).queue();
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
                    StudentDAO.upsertStudent(null, null, skills, null, userId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                event.reply("✅ Skills saved.").setEphemeral(true).queue();

                StringSelectMenu posMenu = StringSelectMenu.create("select_position")
                        .setPlaceholder("\uD83D\uDCCC Choose your preferred position")
                        .setMaxValues(5)
                        .addOption("Backend", "backend")
                        .addOption("Frontend", "frontend")
                        .addOption("Full Stack", "fullstack")
                        .addOption("Mobile", "mobile")
                        .addOption("QA", "qa")
                        .addOption("DevOps", "devops")
                        .addOption("Data Science", "data")
                        .build();

                event.getChannel().sendMessage("\uD83D\uDCDD What type of position are you looking for?")
                        .setActionRow(posMenu)
                        .queue();
            }

            case "select_position" -> {
                String selected = event.getValues().get(0);
                try {
                    StudentDAO.upsertStudent(null, null, null, selected, userId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                event.reply("✅ Position saved.").setEphemeral(true).queue();

                event.getUser().openPrivateChannel().queue(dm -> {
                    dm.sendMessage("✅ Your profile has been saved! What would you like to do next?")
                            .setActionRow(
                                    Button.primary("gpt_ask", "🤖 Ask GPT"),
                                    Button.primary("view_profile", "👤 View Profile"),
                                    Button.success("create_profile", "📝 Create Profile"),
                                    Button.secondary("match_jobs", "🎯 Match Me")
                            ).queue();
                });
            }

            default -> event.reply("⚠️ Unknown select menu.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
