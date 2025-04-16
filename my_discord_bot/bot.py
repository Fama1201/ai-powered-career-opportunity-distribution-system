# bot.py
print("🟢 bot.py script is running…")
import logging
logging.basicConfig(level=logging.INFO)
import discord
from discord.ext import commands
from config import DISCORD_TOKEN
from persistence import load_profiles
from commands_module import BotCommands
from events_module import on_message_handler

# Configure necessary intents.
intents = discord.Intents.default()
intents.message_content = True
intents.members = True

# Create the bot instance with a command prefix and intents.
bot = commands.Bot(command_prefix='!', intents=intents)
#bot.tree = bot.tree  # For slash commands (optional).

# Global database: we work with the user_profiles dictionary for the lifetime of the bot.
bot.user_profiles = load_profiles()

@bot.event
async def on_ready():
    await bot.tree.sync()
    print(f'✅ Slash commands synchronized. Bot is online as {bot.user}!')

# Register the event listener.
bot.event(on_message_handler)

# Load the commands cog.
bot.add_cog(BotCommands(bot))

bot.run(DISCORD_TOKEN)
