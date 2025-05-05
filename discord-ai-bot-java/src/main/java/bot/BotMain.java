package bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;

public class BotMain {
    public static void main(String[] args) throws LoginException {
        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            System.out.println("❌ DISCORD_TOKEN is not set.");
            return;
        }

        JDABuilder.createDefault(token)
                .setActivity(Activity.listening("!start"))
                .addEventListeners(new CommandHandler())
                .build();
    }
}
