# bot.py
import logging
from functools import partial

import discord
from discord.ext import commands

from config import DISCORD_TOKEN
from persistence import load_profiles
from commands_module import BotCommands
from events_module import on_message_handler  # Import our DM handler

# Print startup banner and configure logging
print("🟢 bot.py script is running…")
logging.basicConfig(level=logging.INFO)

# Configure necessary intents
intents = discord.Intents.default()
intents.message_content = True
intents.members = True

class MyBot(commands.Bot):
    def __init__(self):
        # Initialize with '!' prefix and configured intents
        super().__init__(command_prefix='!', intents=intents)
        # Load or initialize the user profiles dictionary
        self.user_profiles = load_profiles()

    async def setup_hook(self):
        # Add our command Cog
        await self.add_cog(BotCommands(self))
        # Sync slash commands if you have any (remove if unused)
        await self.tree.sync()

# Create the bot instance
bot = MyBot()

# Register the on_message DM handler
# Bind the bot instance to the handler via functools.partial
bot.add_listener(partial(on_message_handler, bot), 'on_message')

@bot.event
async def on_ready():
    # Confirm that slash commands are synced and bot is online
    print(f'✅ Slash commands synchronized. Bot is online as {bot.user}!')

# Run the bot with basic error handling
try:
    bot.run(DISCORD_TOKEN)
except Exception as e:
    print("❌ bot.run failed with error:", e)
    logging.error("bot.run failed", exc_info=e)
