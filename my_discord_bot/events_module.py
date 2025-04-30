# events_module.py
import discord
from discord.ui import View, Select, Modal, TextInput, Button
from persistence import save_profiles

# --- UI COMPONENTS INLINE ---

class SkillSelect(Select):
    """Multi-select dropdown for common skills + an “Other” option."""
    def __init__(self, bot):
        options = [
            discord.SelectOption(label="Python"),
            discord.SelectOption(label="Java"),
            discord.SelectOption(label="JavaScript"),
            discord.SelectOption(label="React"),
            discord.SelectOption(label="SQL"),
            discord.SelectOption(label="UI/UX"),
            discord.SelectOption(label="Other"),
        ]
        super().__init__(
            placeholder="Select your top skills (max 5)…",
            min_values=1,
            max_values=5,
            options=options
        )
        self.bot = bot

    async def callback(self, interaction: discord.Interaction):
        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        selected = self.values

        if "Other" in selected:
            # show modal for custom skills
            await interaction.response.send_modal(OtherSkillModal(self.bot, selected))
        else:
            # record and advance
            profile["skills"] = selected
            profile["step"] = 3
            save_profiles(self.bot.user_profiles)

            # immediate response with next dropdown
            await interaction.response.send_message(
                "🧾 **3. What type of opportunities are you interested in?**\n"
                "_(Select one or more, or choose Other to type your own.)_",
                view=InterestSelectView(self.bot),
                ephemeral=True
            )

class SkillSelectView(View):
    """Wraps SkillSelect for easy sending."""
    def __init__(self, bot, *, timeout=180):
        super().__init__(timeout=timeout)
        self.add_item(SkillSelect(bot))

class OtherSkillModal(Modal):
    """Modal to collect custom skills when “Other” is selected."""
    other_skills = TextInput(
        label="Enter your custom skills (comma-separated)",
        style=discord.TextStyle.long,
        placeholder="e.g. Go, Rust, Swift"
    )

    def __init__(self, bot, selected):
        super().__init__(title="Custom Skills")
        self.bot = bot
        self.selected = [s for s in selected if s != "Other"]

    async def on_submit(self, interaction: discord.Interaction):
        extras = [s.strip() for s in self.other_skills.value.split(",") if s.strip()]
        skills = self.selected + extras

        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        profile["skills"] = skills
        profile["step"] = 3
        save_profiles(self.bot.user_profiles)

        await interaction.response.send_message(
            "🧾 **3. What type of opportunities are you interested in?**\n"
            "_(Select one or more, or choose Other to type your own.)_",
            view=InterestSelectView(self.bot),
            ephemeral=True
        )

class InterestSelect(Select):
    """Multi-select dropdown for common domains + an “Other” option."""
    def __init__(self, bot):
        options = [
            discord.SelectOption(label="Backend Development"),
            discord.SelectOption(label="Frontend Development"),
            discord.SelectOption(label="Full-Stack Development"),
            discord.SelectOption(label="Data Science"),
            discord.SelectOption(label="DevOps / SRE"),
            discord.SelectOption(label="Mobile Development"),
            discord.SelectOption(label="QA / Testing"),
            discord.SelectOption(label="Game Development"),
            discord.SelectOption(label="Other"),
        ]
        super().__init__(
            placeholder="Select your interest areas…",
            min_values=1,
            max_values=3,
            options=options
        )
        self.bot = bot

    async def callback(self, interaction: discord.Interaction):
        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        selected = self.values

        if "Other" in selected:
            await interaction.response.send_modal(OtherInterestModal(self.bot, selected))
        else:
            profile["interests"] = selected
            profile["step"] = 4
            save_profiles(self.bot.user_profiles)

            await interaction.response.send_message(
                "📄 **4. Do you have a resume you'd like to share?**\n"
                "You can upload it here or share a link (Google Drive, PDF, etc.)",
                ephemeral=True
            )

class InterestSelectView(View):
    """Wraps InterestSelect for easy sending."""
    def __init__(self, bot, *, timeout=180):
        super().__init__(timeout=timeout)
        self.add_item(InterestSelect(bot))

class OtherInterestModal(Modal):
    """Modal to collect custom interests when “Other” is selected."""
    other_interests = TextInput(
        label="Enter your custom interests (comma-separated)",
        style=discord.TextStyle.long,
        placeholder="e.g. Blockchain, AR/VR, Embedded Systems"
    )

    def __init__(self, bot, selected):
        super().__init__(title="Custom Interests")
        self.bot = bot
        self.selected = [s for s in selected if s != "Other"]

    async def on_submit(self, interaction: discord.Interaction):
        extras = [s.strip() for s in self.other_interests.value.split(",") if s.strip()]
        interests = self.selected + extras

        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        profile["interests"] = interests
        profile["step"] = 4
        save_profiles(self.bot.user_profiles)

        await interaction.response.send_message(
            "📄 **4. Do you have a resume you'd like to share?**\n"
            "You can upload it here or share a link (Google Drive, PDF, etc.)",
            ephemeral=True
        )

class OpportunityView(View):
    """Buttons for bookmarking, showing more, or stopping."""
    def __init__(self, bot, *, timeout=180):
        super().__init__(timeout=timeout)
        self.bot = bot

    @discord.ui.button(label="Bookmark", style=discord.ButtonStyle.green)
    async def bookmark(self, interaction: discord.Interaction, button: Button):
        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        if profile:
            profile["step"] = 6
            save_profiles(self.bot.user_profiles)
        await interaction.response.send_message("✅ Bookmarked! 🎉", ephemeral=True)

    @discord.ui.button(label="More", style=discord.ButtonStyle.blurple)
    async def more(self, interaction: discord.Interaction, button: Button):
        await interaction.response.send_message("🔍 Here’s another one...", ephemeral=True)

    @discord.ui.button(label="Stop", style=discord.ButtonStyle.red)
    async def stop(self, interaction: discord.Interaction, button: Button):
        user_id = str(interaction.user.id)
        profile = self.bot.user_profiles.get(user_id)
        if profile:
            profile["step"] = -1
            save_profiles(self.bot.user_profiles)
        await interaction.response.send_message(
            "👋 Stopping. You can start again with `!start`.", ephemeral=True
        )

# --- EVENT HANDLER ---

async def on_message_handler(bot, message):
    """
    Manages onboarding steps in DMs only.
    Public-channel commands (e.g. !start) are handled by commands.Bot.
    """
    if message.guild or message.author.bot:
        return

    user_id = str(message.author.id)
    profiles = bot.user_profiles
    if user_id not in profiles:
        return

    profile = profiles[user_id]
    step = profile.get("step", 1)
    content = message.content.strip()

    if step == 1:
        # Step 1: Save name → Step 2
        profile["name"] = content
        profile["step"] = 2
        save_profiles(profiles)
        await message.channel.send(
            "💻 **2. What are your top skills or technologies?**\n"
            "_(Select up to 5 from dropdown or choose Other to type your own.)_",
            view=SkillSelectView(bot)
        )

    elif step == 4:
        # Step 4: Save resume → Step 5
        profile["resume"] = content
        profile["step"] = 5
        save_profiles(profiles)
        await message.channel.send(
            f"Awesome, thanks {profile['name']}! 🙌\n\n"
            f"I’ve saved your profile — {profile['interests']}, {profile['skills']}… got it! ✅\n"
            "Give me a moment to scan through what’s available… 🔍"
        )
        await message.channel.send(
            "🎯 **Found an Opportunity That Matches You!**\n\n"
            "🔹 **Role:** Backend Developer Intern\n"
            "🏢 **Company:** NovaTech Solutions\n"
            "📍 **Location:** Remote\n"
            "🕒 **Duration:** 3 months\n"
            "💼 **Stack:** Python, Django, PostgreSQL\n"
            "📝 **Description:** You’ll join a team building REST APIs and work on scalable backend services…\n\n"
            "What would you like to do?",
            view=OpportunityView(bot)
        )

    elif step == 6:
        # Step 6: Complete
        await message.channel.send(
            "🎉 **Your profile is complete!** You will receive personalized opportunities here. "
            "Type `!start` to begin again."
        )
        profile["step"] = -1
        save_profiles(profiles)
