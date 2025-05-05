package bot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import javax.security.auth.login.LoginException;

public class BotMain {
    public static void main(String[] args) throws LoginException {
        // Get the Discord bot token from environment variables
        String token = System.getenv("DISCORD_TOKEN");

        // If the token is not set, print an error and stop execution
        if (token == null || token.isEmpty()) {
            System.out.println("❌ DISCORD_TOKEN is not set.");
            return;
        }

        // Create the JDA builder instance using the bot token
        JDABuilder builder = JDABuilder.createDefault(token);

        // Enable necessary Gateway Intents for receiving messages
        builder.enableIntents(
                GatewayIntent.GUILD_MESSAGES,       // Needed to receive messages from guild (server) text channels
                GatewayIntent.DIRECT_MESSAGES,      // Needed to receive private messages (DMs)
                GatewayIntent.MESSAGE_CONTENT       // Needed to read the actual content of messages (e.g., "!start")
        );

        // Set the bot's activity (shows under the bot's name in Discord)
        builder.setActivity(Activity.listening("!start"))

                // Add your event listener (command handler logic)
                .addEventListeners(new CommandHandler())

                // Build and start the bot
                .build();
    }
}
