package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles button clicks for the initial CV question.
 *
 * There are two possible flows:
 *  1. User has a CV → we ask them to upload their PDF (step 7).
 *  2. User does not have a CV → we start the manual profile flow (step 1).
 */
public class ButtonHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Reload the current profiles map from storage
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();

        // Get or create this user's profile
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "cv_yes" -> {
                // User said they have a CV:
                //   → advance to a new step where we wait for a PDF upload.
                profile.put("step", 7);  // 7: awaiting file upload

                // Save immediately so the next DM handler sees the updated step
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                // Prompt user to upload their resume PDF
                event.reply("📄 Great – please upload your resume **PDF** here in this DM.")
                        .setEphemeral(true)
                        .queue();
            }

            case "cv_no" -> {
                // User said they do NOT have a CV:
                //   → mark resume status and start the manual profile flow.
                profile.put("resume", "No CV ❌");
                profile.put("step", 1);  // 1: next we ask for full name

                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                // Ask for their full name
                event.reply("👤 No worries! What’s your full name?")
                        .setEphemeral(true)
                        .queue();
            }

            default -> {
                // Any other button ID is unexpected
                event.reply("⚠️ Sorry, I didn't recognize that button.")
                        .setEphemeral(true)
                        .queue();
                return;  // do not save or modify anything further
            }
        }

        // Ensure the profile is saved after handling
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
    }
}
