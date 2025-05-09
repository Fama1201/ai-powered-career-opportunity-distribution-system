package bot;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;
import storage.StudentDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SelectMenuHandler manages user interactions with select menus for skills and position preferences.
 *
 * When the user selects options in the menus, it updates the in-memory profile and persists
 * the relevant fields to the database via StudentDAO.upsertStudent(...).
 */
public class SelectMenuHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        // Load existing profiles from storage
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "select_skills" -> handleSkillsSelection(event, profile, userProfiles, userId);
            case "select_position" -> handlePositionSelection(event, profile, userProfiles, userId);
            default -> event.reply("⚠️ Unrecognized select menu interaction.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Handles the skills select menu (componentId = "select_skills").
     * If "Other" is selected, prompts for manual entry, otherwise saves the chosen skills.
     */
    private void handleSkillsSelection(StringSelectInteractionEvent event,
                                       Map<String,Object> profile,
                                       Map<String,Map<String,Object>> userProfiles,
                                       String userId) {
        List<String> selectedValues = event.getValues();
        if (selectedValues.contains("other")) {
            // Switch to manual skills entry
            profile.put("step", 3);
            userProfiles.put(userId, profile);
            ProfileStorage.saveProfiles(userProfiles);
            event.reply("✍️ Please type in your skills manually.")
                    .setEphemeral(true)
                    .queue();
        } else {
            // Save selected skills list
            String skillsCsv = String.join(", ", selectedValues);
            profile.put("skills", skillsCsv);
            try {
                StudentDAO.upsertStudent(
                        null,        // no change to name
                        null,        // no change to email
                        skillsCsv,   // update skills
                        null,        // no change to career interest
                        userId,
                        null,        // no change to cvUrl
                        null         // no change to jobType
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Advance to next step
            profile.put("step", 4);
            userProfiles.put(userId, profile);
            ProfileStorage.saveProfiles(userProfiles);

            // Acknowledge and prompt for position
            event.reply("✅ Skills saved.")
                    .setEphemeral(true)
                    .queue();

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
                    .addOption("Other", "other")
                    .build();

            event.getChannel().sendMessage("🧾 Which type of position are you seeking?")
                    .setActionRow(positionMenu)
                    .queue();
        }
    }

    /**
     * Handles the position select menu (componentId = "select_position").
     * If "Other" is selected, prompts for manual entry, otherwise saves the chosen position.
     */
    private void handlePositionSelection(StringSelectInteractionEvent event,
                                         Map<String,Object> profile,
                                         Map<String,Map<String,Object>> userProfiles,
                                         String userId) {
        String selected = event.getValues().get(0);
        if ("other".equals(selected)) {
            // Manual entry for position
            profile.put("step", 5);
            userProfiles.put(userId, profile);
            ProfileStorage.saveProfiles(userProfiles);
            event.reply("✍️ Please type your preferred position.")
                    .setEphemeral(true)
                    .queue();
        } else {
            // Save selected position
            profile.put("career_interest", selected);
            try {
                StudentDAO.upsertStudent(
                        null,         // no change to name
                        null,         // no change to email
                        null,         // no change to skills
                        selected,     // update career interest
                        userId,
                        null,         // no change to cvUrl
                        null          // no change to jobType
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Advance to next step
            profile.put("step", 6);
            userProfiles.put(userId, profile);
            ProfileStorage.saveProfiles(userProfiles);

            // Acknowledge and prompt for resume description/link
            event.reply("✅ Position saved. Please describe your resume or paste a link.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
