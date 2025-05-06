package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

public class ButtonHandler extends ListenerAdapter {

    /**
     * This method is called whenever a button interaction event occurs.
     * It handles clicks on the "Yes" and "No" CV buttons and updates the user profile accordingly.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        // Reload the latest user profiles from storage
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        // Get the profile for this user, or create a new one if it doesn't exist yet
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "cv_yes" -> {
                // User has a CV: mark resume status and skip to result step
                profile.put("resume", "Has CV ✅");
                profile.put("step", 5);

                // Send an ephemeral acknowledgement to the user
                event.reply("✅ Great! We'll use your resume to find opportunities...")
                        .setEphemeral(true)
                        .queue();

                // Send a follow-up message with a matched opportunity example
                event.getChannel().sendMessage(
                        "🎯 **Found an Opportunity That Matches You!**\n\n" +
                                "🔹 **Role:** Backend Developer Intern\n" +
                                "🏢 **Company:** NovaTech Solutions\n" +
                                "📍 **Location:** Remote\n" +
                                "💼 **Stack:** Java, Spring Boot, PostgreSQL"
                ).queue();
            }
            case "cv_no" -> {
                // User does not have a CV: mark resume status and set step to start the normal flow
                profile.put("resume", "No CV ❌");
                profile.put("step", 1);

                // Ask the user for their full name next
                event.reply("👤 No problem! What’s your full name?")
                        .setEphemeral(true)
                        .queue();
            }
            default -> {
                // Unknown button ID: inform the user
                event.reply("⚠️ Unknown button.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // Save the updated profile back into storage
        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
    }
}
