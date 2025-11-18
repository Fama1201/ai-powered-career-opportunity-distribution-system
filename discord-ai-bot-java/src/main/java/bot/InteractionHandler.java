package bot;

import bot.storage.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class InteractionHandler extends ListenerAdapter {

    @Autowired private OpportunityDAO opportunityDAO;
    @Autowired private StudentDAO studentDAO;
    @Autowired private FeedbackDAO feedbackDAO;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String userId = event.getUser().getId();
        String buttonId = event.getComponentId();

        switch (buttonId) {
            case "start":
                CommandHandler.showMainMenu(event.getUser());
                event.deferEdit().queue();
                break;

            case "view_profile":
                Optional<Student> sOpt = studentDAO.findByDiscordId(userId);
                if (sOpt.isEmpty()) {
                    event.reply("Profile not found.").setEphemeral(true).queue();
                } else {
                    Student s = sOpt.get();
                    String msg = "Name: " + s.getName() + "\nEmail: " + s.getEmail() + "\nSkills: " + s.getSkills();
                    event.reply(msg).setEphemeral(true).queue();
                }
                break;

            case "delete_profile":
                try {
                    if(studentDAO.findByDiscordId(userId).isPresent()) {
                        studentDAO.deleteByDiscordId(userId);
                        event.reply("Deleted.").setEphemeral(true).queue();
                    } else {
                        event.reply("No profile to delete.").setEphemeral(true).queue();
                    }
                } catch (Exception e) {
                    event.reply("Error deleting.").setEphemeral(true).queue();
                }
                break;

            case "create_profile":
                // Use the static helper to start the process
                CommandHandler.startRegistrationFor(userId);
                event.reply("Please enter your email:").setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String userId = event.getUser().getId();
        List<String> values = event.getValues();
        String joined = String.join(", ", values);

        Student s = studentDAO.findByDiscordId(userId).orElse(new Student());
        s.setDiscordId(userId);

        if (event.getComponentId().equals("select_skills")) {
            s.setSkills(joined);
            studentDAO.save(s);
            // Use the new helper with event.getChannel()
            CommandHandler.promptPositionSelection(event.getChannel());
            event.reply("Skills saved. Next, select positions.").setEphemeral(true).queue();
        } else if (event.getComponentId().equals("select_position")) {
            s.setCareerInterest(joined);
            studentDAO.save(s);
            event.reply("Profile Complete!").setEphemeral(true).queue();
        }
    }
}