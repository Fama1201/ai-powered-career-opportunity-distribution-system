package bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.Map;

public class ButtonHandler extends ListenerAdapter {


    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "cv_yes" -> {
                profile.put("resume", "Has CV ✅");
                profile.put("step", 5); // saltar a resultado
                event.reply("✅ Great! We'll use your resume to find opportunities...").setEphemeral(true).queue();
                event.getChannel().sendMessage(
                        "🎯 **Found an Opportunity That Matches You!**\n\n" +
                                "🔹 **Role:** Backend Developer Intern\n" +
                                "🏢 **Company:** NovaTech Solutions\n" +
                                "📍 **Location:** Remote\n" +
                                "💼 **Stack:** Java, Spring Boot, PostgreSQL"
                ).queue();
            }
            case "cv_no" -> {
                profile.put("resume", "No CV ❌");
                profile.put("step", 1); // comenzar flujo normal
                event.reply("👤 No problem! What’s your full name?").setEphemeral(true).queue();
            }
            default -> {
                event.reply("⚠️ Unknown button.").setEphemeral(true).queue();
                return;
            }
        }

        userProfiles.put(userId, profile);
        ProfileStorage.saveProfiles(userProfiles);
    }
}
