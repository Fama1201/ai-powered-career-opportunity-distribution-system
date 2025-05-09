package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles button clicks for:
 *   - Starting the registration flow ("start" button)
 *   - Responding to the "Do you have a resume?" prompt ("cv_yes" / "cv_no")
 */
public class ButtonHandler extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Load in-memory profiles
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "start" -> {
                // Kick off the DM-based registration flow
                event.reply("👋 Check your DMs to begin registration!")
                        .setEphemeral(true)
                        .queue();

                event.getUser().openPrivateChannel().queue(dm -> {
                    // Initialize step 0
                    profile.put("step", 0);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    // Ask the resume question
                    dm.sendMessage("📄 Do you have a resume (CV)?")
                            .setActionRow(
                                    Button.success("cv_yes", "✅ Yes"),
                                    Button.danger("cv_no",  "❌ No")
                            )
                            .queue();
                });
            }

            case "cv_yes" -> {
                // User will upload a PDF resume
                profile.put("step", 7);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                event.reply("📄 Great! Please upload your resume as a PDF in this DM.")
                        .setEphemeral(true)
                        .queue();
            }

            case "cv_no" -> {
                // No resume; proceed to email collection
                profile.put("resume", "No CV provided");
                profile.put("step", 1);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                event.reply("📧 Perfect. What is your email address?")
                        .setEphemeral(true)
                        .queue();
            }

            default -> {
                // Unknown button
                event.reply("⚠️ Sorry, I didn't recognize that option.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Persist any profile changes
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
    }
}
