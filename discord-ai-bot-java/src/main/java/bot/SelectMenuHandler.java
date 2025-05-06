package bot;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectMenuHandler extends ListenerAdapter {


    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        String userId = event.getUser().getId();
        Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());

        switch (event.getComponentId()) {
            case "select_skills" -> {
                List<String> values = event.getValues();
                if (values.contains("other")) {
                    profile.put("step", 3); // Corregido: ahora espera texto manual
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✍️ Please type your skills manually.").setEphemeral(true).queue();
                } else {
                    profile.put("skills", String.join(", ", values));
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✅ Skills saved.").setEphemeral(true).queue();

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
                String selected = event.getValues().get(0);
                if (selected.equals("other")) {
                    profile.put("step", 5); // Corregido: espera texto manual
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✍️ Please type your preferred position.").setEphemeral(true).queue();
                } else {
                    profile.put("interests", selected);
                    profile.put("step", 6); // Siguiente paso: describir CV
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.reply("✅ Position saved. Please describe your resume or paste a link.").setEphemeral(true).queue();
                }
            }
        }
    }
}
