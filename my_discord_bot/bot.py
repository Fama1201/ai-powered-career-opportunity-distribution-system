# bot.py
import logging
from functools import partial

print("🟢 bot.py script is running…")
logging.basicConfig(level=logging.INFO)

import discord
from discord.ext import commands
from config import DISCORD_TOKEN
from persistence import load_profiles
from commands_module import BotCommands
from events_module import on_message_handler

# Configure necessary intents
intents = discord.Intents.default()
intents.message_content = True
intents.members = True

class MyBot(commands.Bot):
    def __init__(self):
        super().__init__(command_prefix='!', intents=intents)
        # The user profiles dictionary we'll keep for the bot's lifetime
        self.user_profiles = load_profiles()

    async def setup_hook(self):
        # Adding the Cog (awaited!)
        await self.add_cog(BotCommands(self))
        # Register and synchronize slash commands
        await self.tree.sync()

# Create the bot instance
bot = MyBot()

# on_ready event
@bot.event
async def on_ready():
    print(f'✅ Slash commands synchronized. Bot is online as {bot.user}!')

# Register the handler for processing messages from DMs to the correct event
# Pass the bot instance to on_message_handler using functools.partial
bot.add_listener(partial(on_message_handler, bot), 'on_message')

# Run the bot
try:
    bot.run(DISCORD_TOKEN)
except Exception as e:
    print("❌ bot.run failed with error:", e)
    logging.error("bot.run failed", exc_info=e)

