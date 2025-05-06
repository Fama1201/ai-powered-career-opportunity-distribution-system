
package bot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import storage.ProfileStorage;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler extends ListenerAdapter {


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("✅ Bot is online as " + event.getJDA().getSelfUser().getAsTag());

        event.getJDA().getGuilds().forEach(guild -> {
            if (guild.getDefaultChannel() instanceof TextChannel channel) {
                if (channel.canTalk()) {
                    channel.sendMessage("👋 **EXPERTS.AI Bot is now online and ready to help!**").queue();
                }
            }
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Map<String, Map<String, Object>> userProfiles = ProfileStorage.loadProfiles();
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        String content = event.getMessage().getContentRaw().trim();

        if (content.equalsIgnoreCase("!status")) {
            event.getChannel().sendMessage("✅ I'm alive and ready!").queue();
            return;
        }

        if (event.isFromGuild() && content.equalsIgnoreCase("!start")) {
            event.getChannel().sendMessage("👋 **Welcome to the EXPERTS.AI Career Hub!** Check your DMs to get started!").queue();

            event.getAuthor().openPrivateChannel().queue(dm -> {
                Map<String, Object> profile = new HashMap<>();
                profile.put("step", 0);
                userProfiles.put(userId, profile);
                ProfileStorage.saveProfiles(userProfiles);

                dm.sendMessage("📄 Do you have a resume (CV)?")
                        .setActionRow(
                                Button.success("cv_yes", "✅ Yes"),
                                Button.danger("cv_no", "❌ No")
                        ).queue();
            });

            return;
        }

        if (!event.isFromGuild()) {
            Map<String, Object> profile = userProfiles.getOrDefault(userId, new HashMap<>());
            int step = ((Number) profile.getOrDefault("step", 0)).intValue();
            String input = content.trim();

            switch (step) {
                case 1 -> {
                    profile.put("name", input);
                    profile.put("step", 2);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

                    StringSelectMenu menu = StringSelectMenu.create("select_skills")
                            .setPlaceholder("💻 Select up to 5 skills")
                            .setMaxValues(5)
                            .addOption("Java", "java")
                            .addOption("Python", "python")
                            .addOption("JavaScript", "javascript")
                            .addOption("React", "react")
                            .addOption("Spring Boot", "spring")
                            .addOption("Node.js", "node")
                            .addOption("C++", "cpp")
                            .addOption("SQL", "sql")
                            .addOption("Git", "git")
                            .addOption("Docker", "docker")
                            .addOption("Other", "other")
                            .build();

                    event.getChannel().sendMessage("💻 **2. What are your top skills or technologies?**")
                            .setActionRow(menu)
                            .queue();
                }
                case 3 -> {
                    profile.put("skills", input);
                    profile.put("step", 4);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);

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
                case 4 -> {
                    profile.put("interests", input);
                    profile.put("step", 6);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("📄 **4. Optionally, describe your resume or paste a link**").queue();
                }
                case 6 -> {
                    profile.put("resume", input);
                    profile.put("step", -1);
                    userProfiles.put(userId, profile);
                    ProfileStorage.saveProfiles(userProfiles);
                    event.getChannel().sendMessage("🎉 **Your profile is complete!** You’ll receive opportunities soon.").queue();
                }
            }
        }
    }
}
