import discord
from discord.ext import commands

intents = discord.Intents.default()
intents.message_content = True
intents.members = True

bot = commands.Bot(command_prefix='!', intents=intents)
tree = bot.tree  # para slash commands

user_profiles = {}

@bot.event
async def on_ready():
    await tree.sync()
    print(f'✅ Slash commands sincronizados. Jobify CVUT está online como {bot.user}')

@bot.command()
async def start(ctx):
    await ctx.send("""👋 **Welcome to the EXPERTS.AI Career Hub!**

We’re excited to help you connect with internships, job opportunities, and thesis topics that match your skills and interests — powered by intelligent AI matching.

🚀 To get started, simply type `start` in any public channel, and our bot will guide you through a quick onboarding in your DMs.

🧠 Once you're set up, you'll receive personalized opportunities directly through Discord.

💬 Need help? Type `help` at any time, or reach out to the team in the #support channel.

📌 Stay tuned in ⁠**#announcements** for company events, new openings, and system updates!

Let’s find your next big opportunity together!
""")
    try:
        await ctx.author.send(
            "👋 Hey there! I’m your personal assistant from **EXPERTS.AI** — here to help you discover job, internship, and thesis opportunities that fit you.\n\n"
            "Before we begin, I just need a few quick details to tailor your experience:\n\n"
            "🧑 **1. What’s your full name?**\n💡 This helps us personalize your profile."
        )
        user_profiles[ctx.author.id] = {"step": 1}
    except discord.Forbidden:
        await ctx.send("❌ I couldn’t send you a DM. Please enable direct messages in your privacy settings and try again!")

@tree.command(name="start", description="Empieza tu camino en Jobify CVUT")
async def start_command(interaction: discord.Interaction):
    await interaction.response.send_message(
        "👋 **Welcome to the EXPERTS.AI Career Hub!**\n\n"
        "We’re excited to help you connect with internships, job opportunities, and thesis topics that match your skills and interests — powered by intelligent AI matching.\n\n"
        "🚀 To get started, simply type `start` in any public channel, and our bot will guide you through a quick onboarding in your DMs.\n\n"
        "🧠 Once you're set up, you'll receive personalized opportunities directly through Discord.\n\n"
        "💬 Need help? Type `help` at any time, or reach out to the team in the support channel.\n\n"
        "📌 Stay tuned in ⁠**#announcements** for company events, new openings, and system updates!\n\n"
        "Let’s find your next big opportunity together!"
    )

@bot.event
async def on_message(message):
    await bot.process_commands(message)

    if message.guild is not None or message.author.bot:
        return

    user_id = message.author.id
    if user_id not in user_profiles:
        return

    profile = user_profiles[user_id]
    step = profile.get("step", 1)

    if step == 1:
        profile["name"] = message.content.strip()
        profile["step"] = 2
        await message.channel.send("💻 **2. What are your top skills or technologies?**\n_(e.g., Python, React, SQL, UI/UX...)_")

    elif step == 2:
        profile["skills"] = message.content.strip()
        profile["step"] = 3
        await message.channel.send("🧾 **3. What type of opportunities are you interested in?**\n_(e.g., Backend Developer Internship, AI Research Thesis, QA position...)_")

    elif step == 3:
        profile["interests"] = message.content.strip()
        profile["step"] = 4
        await message.channel.send("📄 **4. Do you have a resume you'd like to share?**\nYou can upload it here or share a link (Google Drive, PDF, etc.)")

    elif step == 4:
        profile["resume"] = message.content.strip()
        profile["step"] = 5
        await message.channel.send(
            f"Awesome, thanks {profile['name']}! 🙌\n\n"
            f"I’ve saved your profile — {profile['interests']}, {profile['skills']}… got it! ✅\n"
            "Your resume is safely received and will help me fine-tune the opportunities I send your way.\n\n"
            "Give me a moment to scan through what’s available… 🔍"
        )

        await message.channel.send(
            "🎯 **Found an Opportunity That Matches You!**\n\n"
            "🔹 Role: Backend Developer Intern\n"
            "🏢 Company: NovaTech Solutions\n"
            "📍 Location: Remote\n"
            "🕒 Duration: 3 months\n"
            "💼 Stack: Python, Django, PostgreSQL\n"
            "📝 Description: You’ll join a team working on a real-world SaaS platform, building REST APIs and automating backend workflows. Weekly mentoring sessions included.\n\n"
            "Would you like to:\n⿡ Bookmark this opportunity\n⿢ See more options\n⿣ Decline and stop here\n\nJust reply with `bookmark`, `more`, or `stop` — your call! 🙌"
        )

    elif step == 5:
        response = message.content.lower().strip()
        if response == "bookmark":
            await message.channel.send("✅ Awesome! You’ve bookmarked the opportunity:\nJunior Backend Developer @ ByteWave Technologies 🎉")
            await message.channel.send(
                "📬 Good news!\nYour profile has been sent to the company, and based on your match, they’d like to move forward with an interview! 💼🎯\n\n"
                "🗓 Interview Details:\n📅 Date: Tuesday, April 9\n🕓 Time: 14:00 CET\n📍 Format: Online (Zoom link will be sent shortly)\n👤 Interviewer: Sarah, Backend Team Lead at ByteWave\n\n"
                "Would you like to:\n⿡ confirm the interview\n⿢ reschedule\n⿣ decline this opportunity\n\nJust type your choice — and congrats! 🚀"
            )
            profile["step"] = 6

        elif response == "more":
            await message.channel.send(
                "🔍 Scanning...\n🎯 Here’s another one you might like:\n\n"
                "🔹 Role: Junior Backend Developer\n"
                "🏢 Company: ByteWave Technologies\n"
                "📍 Location: Prague (Hybrid)\n"
                "🕒 Duration: 6 months (starting July)\n"
                "💼 Stack: Python, Flask, PostgreSQL, Git\n"
                "📝 Description: Collaborate with senior developers on backend features, API integrations, and CI/CD improvements. Ideal for someone with strong fundamentals and a hunger to grow!\n\n"
                "What would you like to do?\n⿡ bookmark this one\n⿢ more to see another option\n⿣ stop if nothing feels right"
            )
        elif response == "stop":
            await message.channel.send("👋 No worries! You can always type `start` again whenever you’re ready.")
            profile["step"] = -1

    elif step == 6:
        response = message.content.lower().strip()
        if response == "confirm":
            await message.channel.send(
                "🙌 Awesome — your interview is officially confirmed!\n\n"
                "✨ In the meantime, if you'd like some interview tips or want to review your profile, just type `prep tips` or `profile`.\n"
                "You've got this — I’m rooting for you! 💪🧠"
            )
            profile["step"] = 7
        elif response == "reschedule":
            await message.channel.send("📅 Okay! Let me know what new time works for you, and I’ll notify the company.")
        elif response == "decline":
            await message.channel.send("❌ Got it — we’ll skip this one. I’ll keep looking for other matches for you!")
            profile["step"] = 5

    elif step == 7:
        response = message.content.strip().lower()
        if response in ["not relevant", "somewhat relevant", "pretty relevant", "very relevant", "perfect match"]:
            await message.channel.send("🎉 Thanks for the feedback! I’ve updated your profile to prioritize better matches in the future. 🙌")
            profile["step"] = -1

@bot.command()
async def reset(ctx):
    user_profiles.pop(ctx.author.id, None)
    await ctx.send("✅ Tu perfil fue eliminado. Puedes empezar desde cero con `!start`.")

@bot.command()
@commands.has_permissions(manage_messages=True)
async def clean(ctx, cantidad: int = 10):
    """Elimina los últimos X mensajes del canal"""
    deleted = await ctx.channel.purge(limit=cantidad)
    await ctx.send(f"🧹 {len(deleted)} mensajes eliminados.", delete_after=3)


bot.run('MTM1NzA0MTIxNjE4MDQ1NzY5Mw.G4YBKw.c4aOjJPbejJrfK974fK7r4rDtjP_jhxugyDb9g')
