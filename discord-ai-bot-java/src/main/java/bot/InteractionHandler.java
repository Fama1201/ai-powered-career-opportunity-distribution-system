package bot;

import bot.api.OpportunityClient;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.StudentDAO;
import net.dv8tion.jda.api.EmbedBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all interactions from Discord UI components such as buttons and select menus.
 * This includes profile actions, matching jobs, and chatbot prompts.
 */
public class InteractionHandler extends ListenerAdapter {

    /**
     * Responds to button clicks based on their component ID.
     * Each button triggers a different workflow depending on its ID.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();              // Unique identifier of the button clicked
        String userId = event.getUser().getId();         // Discord user ID

        switch (id) {
            case "start" -> {
                // Show main menu after the user clicks the start button
                event.reply("📬 Check your DMs to continue.")
                        .setEphemeral(true)
                        .queue(success -> CommandHandler.showMainMenu(event.getUser()));
            }

            case "delete_profile" -> {
                // Delete user profile from the database
                event.deferReply(true).queue();
                try {
                    boolean deleted = StudentDAO.deleteProfileByDiscordId(userId);
                    if (deleted) {
                        event.getHook().sendMessage("✅ Your profile has been successfully deleted.")
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                    } else {
                        event.getHook().sendMessage("⚠️ No profile was found to delete.")
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ An error occurred while trying to delete your profile.")
                            .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                }
            }

            case "gpt_ask" -> {
                // Prompt user to type a GPT question
                event.reply("✍️ You can ask the AI by typing `!ask <your question>` here.")
                        .setEphemeral(true)
                        .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
            }

            case "view_profile" -> {
                event.deferReply(true).queue();
                try {
                    var data = StudentDAO.getStudentProfile(userId);
                    if (data == null || data.isEmpty()) {
                        event.getHook().sendMessage("⚠️ You don't have a profile yet. Select 'Create Profile' to start.")
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                    } else {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("👤 Your Profile");
                        embed.setColor(0x5865F2); // Discord blurple

                        if (data.get("Name") != null)
                            embed.addField("🧑 Name", data.get("Name"), false);
                        if (data.get("Email") != null)
                            embed.addField("📧 Email", data.get("Email"), false);
                        if (data.get("Skills") != null)
                            embed.addField("🛠️ Skills", data.get("Skills"), false);
                        if (data.get("Career Interest") != null)
                            embed.addField("🎯 Career Interests", data.get("Career Interest"), false);

                        event.getHook().sendMessageEmbeds(embed.build())
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Error retrieving profile.")
                            .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                }
            }

            case "create_profile" -> {
                // Ask if the user has a resume (CV)
                event.reply("\uD83D\uDCC4 Do you have a resume (CV)?")
                        .setEphemeral(true)
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no", "❌ No")
                        ).queue();
            }

            case "cv_yes" -> {
                // Ask the user to upload a PDF
                event.reply("\uD83D\uDCC4 Please upload your resume as a PDF.")
                        .setEphemeral(true).queue();
            }

            case "cv_no" -> {
                // Initialize profile and start registration (email step)
                event.deferReply(true).queue();
                try {
                    StudentDAO.upsertStudent(null, null, null, null, userId);
                    CommandHandler.startRegistrationFor(userId);
                    event.getHook().sendMessage("\uD83D\uDCE7 Please enter your email address.").queue();
                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Failed to initialize profile setup.").queue();
                }
            }

            case "match_jobs" -> {
                // Match job opportunities based on profile data
                event.deferReply(true).queue();
                try {
                    var profile = StudentDAO.getStudentProfile(userId);

                    if (profile == null || profile.get("Skills") == null || profile.get("Career Interest") == null) {
                        event.getHook().sendMessage("❗ You need to complete your profile first.")
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                        return;
                    }

                    String skills = profile.get("Skills");
                    String interest = profile.get("Career Interest");

                    Set<OpportunityClient.Opportunity> results =
                            OpportunityClient.searchMultipleKeywords(skills + " " + interest);

                    if (results.isEmpty()) {
                        event.getHook().sendMessage("😢 No opportunities found for your profile.")
                                .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                    } else {
                        event.getHook().sendMessage("🎯 Found " + results.size() + " opportunities for you:")
                                .queue(msg -> {
                                    for (var opp : results) {
                                        try {
                                            if (!storage.OpportunityDAO.existsForUser(opp, userId)) {
                                                storage.OpportunityDAO.insertForUser(opp, userId);
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                        if (!opp.url.isBlank()) {
                                            event.getChannel().sendMessageEmbeds(opp.toEmbed())
                                                    .addActionRow(Button.link(opp.url, "📩 Apply"))
                                                    .queue();
                                        } else {
                                            event.getChannel().sendMessageEmbeds(opp.toEmbed()).queue();
                                        }
                                    }
                                    // Show menu after listing jobs
                                    CommandHandler.showMainMenu(event.getUser());
                                });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("❌ Error matching opportunities: " + e.getMessage())
                            .queue(msg -> CommandHandler.showMainMenu(event.getUser()));
                }
            }

            // Default case for unknown buttons
            default -> event.reply("\u26A0\uFE0F Unrecognized button.")
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Handles dropdown (select menu) interaction events from the user.
     */
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String userId = event.getUser().getId();

        switch (event.getComponentId()) {
            case "select_skills" -> {
                // Store selected skills to user profile
                List<String> values = event.getValues();
                String skills = String.join(", ", values);
                try {
                    StudentDAO.upsertStudent(null, null, skills, null, userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                event.reply("✅ Skills saved.").setEphemeral(true).queue();

                // Prompt for position selection
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
                // Store selected positions to user profile in career_interest column
                List<String> selectedPositions = event.getValues(); // Now supports multiple positions
                try {
                    String joined = String.join(", ", selectedPositions);
                    StudentDAO.upsertStudent(null, null, null, joined, userId); // <-- save to career_interest
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String joined = String.join(", ", selectedPositions);
                event.reply("✅ Positions saved: " + joined).setEphemeral(true).queue();

                // Show next step menu
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


            // Default case for unknown select menus
            default -> event.reply("⚠️ Unknown select menu.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
