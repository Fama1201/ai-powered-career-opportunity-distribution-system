# commands_module.py
import discord
from discord.ext import commands
from persistence import save_profiles

class BotCommands(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def start(self, ctx):
        """Sends a welcome message to the user and initiates onboarding via DM."""
        welcome_message = (
            "👋 **Welcome to the EXPERTS.AI Career Hub!**\n\n"
            "We’re excited to help you connect with internships, job opportunities, and thesis topics that match your skills and interests — powered by intelligent AI matching.\n\n"
            "🚀 To get started, simply type `start` in any public channel, and our bot will guide you through a quick onboarding in your DMs.\n\n"
            "🧠 Once you're set up, you'll receive personalized opportunities directly through Discord.\n\n"
            "💬 Need help? Type `help` at any time, or reach out to the team in the #support channel.\n\n"
            "📌 Stay tuned in **#announcements** for company events, new openings, and system updates!\n\n"
            "Let’s find your next big opportunity together!"
        )
        await ctx.send(welcome_message)
        try:
            await ctx.author.send(
                "👋 Hey there! I’m your personal assistant from **EXPERTS.AI** — here to help you discover opportunities that match your profile.\n\n"
                "Before we begin, please tell me: **What’s your full name?**"
            )
            # Add the user's data to the bot instance's global dictionary.
            self.bot.user_profiles[str(ctx.author.id)] = {"step": 1}
            save_profiles(self.bot.user_profiles)
        except discord.Forbidden:
            await ctx.send("❌ I couldn’t send you a DM. Please enable direct messages in your privacy settings and try again!")

    @commands.command()
    async def reset(self, ctx):
        """Deletes the user's data."""
        self.bot.user_profiles.pop(str(ctx.author.id), None)
        save_profiles(self.bot.user_profiles)
        await ctx.send("✅ Your profile has been deleted. You can start over by using `!start`.")

    @commands.command()
    @commands.has_permissions(manage_messages=True)
    async def clean(self, ctx, amount: int = 10):
        """Deletes the last X messages from the channel."""
        deleted = await ctx.channel.purge(limit=amount)
        await ctx.send(f"🧹 {len(deleted)} messages deleted.", delete_after=3)

def setup(bot):
    bot.add_cog(BotCommands(bot))
