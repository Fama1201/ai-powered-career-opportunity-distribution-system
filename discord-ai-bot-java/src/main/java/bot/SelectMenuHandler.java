package bot;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles select‐menu interactions for skills and position choices.
 */
public class SelectMenuHandler extends ListenerAdapter {

    /**
     * Called when a user selects an option from a StringSelectMenu.
     * Updates the user profile and advances the conversation flow.
     */
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        // Reload the latest profiles from storage
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();

        // Get or initialize this user's profile map
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "select_skills" -> {
                // User has selected one or more skills
                List<String> values = event.getValues();
                if (values.contains("other")) {
                    // If "Other" was selected, switch to manual input step
                    profile.put("step", 3);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Prompt the user to type in skills manually
                    event.reply("✍️ Please type your skills manually.")
                            .setEphemeral(true)
                            .queue();
                } else {
                    // Save the chosen skills and advance to the position selection step
                    profile.put("skills", String.join(", ", values));
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Acknowledge and present the next select menu for position
                    event.reply("✅ Skills saved.")
                            .setEphemeral(true)
                            .queue();

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

                    event.getChannel().sendMessage("🧾 **3. What type of position are you looking for?**")
                            .setActionRow(menu)
                            .queue();
                }
            }

            case "select_position" -> {
                // User has selected a preferred position
                String selected = event.getValues().get(0);
                if (selected.equals("other")) {
                    // If "Other", prompt for manual input of position
                    profile.put("step", 5);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    event.reply("✍️ Please type your preferred position.")
                            .setEphemeral(true)
                            .queue();
                } else {
                    // Save the selected position and move to the final resume description step
                    profile.put("interests", selected);
                    profile.put("step", 6);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Acknowledge and prompt for resume details or link
                    event.reply("✅ Position saved. Please describe your resume or paste a link.")
                            .setEphemeral(true)
                            .queue();
                }
            }

            default -> {
                // Unknown menu interaction id
                event.reply("⚠️ Unknown select menu.").setEphemeral(true).queue();
            }
        }
    }
}
