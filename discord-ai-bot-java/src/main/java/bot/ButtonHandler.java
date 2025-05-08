package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the user's response to the "Do you have a resume?" prompt.
 *
 * Two flows:
 *   1. User has a resume → await PDF upload in step 7.
 *   2. User does not have a resume → start manual registration by asking for email (step 1).
 */
public class ButtonHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Load existing profiles from storage
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "cv_yes" -> {
                // User will upload a PDF resume
                profile.put("step", 7);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                // Instruct user to upload PDF resume
                event.reply("📄 Great! Please upload your resume as a PDF in this DM.")
                        .setEphemeral(true)
                        .queue();
            }

            case "cv_no" -> {
                // No resume available; proceed to email collection
                profile.put("resume", "No CV provided");
                profile.put("step", 1);  // Next: ask for email
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                // Prompt user for email address
                event.reply("📧 Perfect. What is your email address?")
                        .setEphemeral(true)
                        .queue();
            }

            default -> {
                // Unrecognized button ID
                event.reply("⚠️ Sorry, I didn't recognize that option.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Ensure profile changes are saved
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
    }
}
