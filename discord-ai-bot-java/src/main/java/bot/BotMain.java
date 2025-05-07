package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class BotMain {
    public static void main(String[] args) throws LoginException {
        // 1. Load your Discord bot token from the environment
        String discordToken = System.getenv("DISCORD_TOKEN");
        if (discordToken == null || discordToken.isEmpty()) {
            System.err.println("❌ DISCORD_TOKEN is not set.");
            return;
        }

        // 2. Load your OpenAI API key from the environment
        //    If you don’t need GPT features, you can skip this or leave it blank.
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey == null || openAiKey.isEmpty()) {
            System.err.println("⚠️ OPENAI_API_KEY is not set. GPT features will be disabled.");
        }

        // 3. Create GPTClient only if we have a valid key
        GPTClient gptClient = null;
        if (openAiKey != null && !openAiKey.isEmpty()) {
            gptClient = new GPTClient(openAiKey);
        }

        // 4. Build the JDA Discord client
        JDABuilder builder = JDABuilder.createDefault(discordToken)
                // Enable intents to read both guild and DM messages
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                // Show "!start" as the bot’s activity status
                .setActivity(Activity.listening("!start"));


            builder.addEventListeners(
                    new CommandHandler(gptClient),
                    new ButtonHandler(),
                    new SelectMenuHandler()
            );


        // 6. Login and start the bot
        builder.build();
    }
}
