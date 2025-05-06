package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class BotMain {
    public static void main(String[] args) throws LoginException {
        // 1. Carga tu token de Discord
        String discordToken = System.getenv("DISCORD_TOKEN");
        if (discordToken == null || discordToken.isEmpty()) {
            System.err.println("❌ DISCORD_TOKEN is not set.");
            return;
        }

        // 2. Carga tu API key de OpenAI
        String openAiKey = System.getenv("OPENAI_API_KEY");
        if (openAiKey == null || openAiKey.isEmpty()) {
            System.err.println("❌ OPENAI_API_KEY is not set.");
            return;
        }

        // 3. Crea el cliente GPT
        GPTClient gptClient = new GPTClient(openAiKey);

        // 4. Construye el bot de Discord
        JDABuilder builder = JDABuilder.createDefault(discordToken)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setActivity(Activity.listening("!start"))
                // 5. Registra tus handlers, pasando el GPTClient al handler que lo necesite
                .addEventListeners(
                        new CommandHandler(gptClient),   // ahora CommandHandler recibe el cliente GPT
                        new ButtonHandler(),
                        new SelectMenuHandler()
                );

        // 6. Arranca el bot
        builder.build();
    }
}
