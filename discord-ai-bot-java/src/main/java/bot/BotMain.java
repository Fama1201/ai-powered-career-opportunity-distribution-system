package bot;

import bot.ai.GPTClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; // Make sure this import is here
import org.springframework.web.bind.annotation.RestController;

// These annotations should be on the ONE class definition
@EnableJpaRepositories("bot.storage")
@SpringBootApplication
@RestController
public class BotMain {

    // This is the main method that starts Spring
    public static void main(String[] args) {
        SpringApplication.run(BotMain.class, args);
    }

    /**
     * Creates a GPTClient bean.
     * This method reads the API key and returns a client,
     * or null if the key is missing.
     */
    @Bean
    public GPTClient gptClient() {
        String openAIApiKey = System.getenv("OPENAI_API_KEY");
        if (openAIApiKey == null || openAIApiKey.isEmpty()) {
            System.out.println("OPENAI_API_KEY is not set. GPT functionality will be disabled.");
            return null; // Return null if no key
        }
        System.out.println("GPT Client Initialized.");
        return new GPTClient(openAIApiKey);
    }

    /**
     * This @Bean will be run automatically after Spring starts.
     * We ask for the handlers in the parameters, and Spring gives them to us.
     */
    @Bean
    public CommandLineRunner runBot(
            @Autowired CommandHandler commandHandler,
            @Autowired InteractionHandler interactionHandler
    ) {
        return args -> {
            // --- 1. Load your Environment Variables ---
            String discordToken = System.getenv("DISCORD_TOKEN");
            if (discordToken == null || discordToken.isEmpty()) {
                System.err.println("DISCORD_TOKEN is not set in environment variables.");
                System.err.println("Aborting bot login.");
                return; // Don't try to log in if the token is missing
            }

            // --- 2. Handlers are now Injected ---
            // Spring has already built them and injected their dependencies.

            // --- 3. Build the JDA Bot ---
            try {
                JDA jda = JDABuilder.createDefault(discordToken)
                        .addEventListeners(commandHandler, interactionHandler) // Register your handlers
                        .enableIntents(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.MESSAGE_CONTENT,
                                GatewayIntent.GUILD_MEMBERS
                        )
                        .build();

                jda.awaitReady(); // Wait for the bot to be fully online
                System.out.println("JDA Bot is online and ready!");

            } catch (InterruptedException e) {
                System.err.println("Failed to log in to Discord.");
                e.printStackTrace();
            }
        };
    }
}