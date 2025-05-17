package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class BotMain {
    public static void main(String[] args) throws LoginException {
        // 1. Load Discord bot token from environment variable
        String discordToken = System.getenv("DISCORD_TOKEN");
        if (discordToken == null || discordToken.isEmpty()) {
            System.err.println("❌ DISCORD_TOKEN is not set.");
            return;
        }

        // 2. Load OpenAI API key (optional)
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey == null || openAiKey.isEmpty()) {
            System.err.println("⚠️ OPENAI_API_KEY is not set. GPT features will be disabled.");
        }

        // 3. Initialize GPTClient if API key is provided
        GPTClient gptClient = null;
        if (openAiKey != null && !openAiKey.isEmpty()) {
            gptClient = new GPTClient(openAiKey);
        }

        // 4. Initialize JDA bot
        JDABuilder builder = JDABuilder.createDefault(discordToken)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setActivity(Activity.listening("!start"));

        // 5. Register event listeners
        builder.addEventListeners(
                new CommandHandler(gptClient),
                new InteractionHandler()
        );

        // 6. Start the bot
        builder.build();
    }
}
