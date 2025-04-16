# events_module.py
from persistence import save_profiles

async def on_message_handler(bot, message):
    # Prefix commands are handled here 
    await bot.process_commands(message)

    # DM messages are handled here
    if message.guild is not None or message.author.bot:
        return

    user_id = str(message.author.id)
    user_profiles = bot.user_profiles
    if user_id not in user_profiles:
        return

    profile = user_profiles[user_id]
    step = profile.get("step", 1)

    if step == 1:
        profile["name"] = message.content.strip()
        profile["step"] = 2
        await message.channel.send(
            "💻 **2. What are your top skills or technologies?**\n_(e.g., Python, React, SQL, UI/UX...)_"
        )

    elif step == 2:
        profile["skills"] = message.content.strip()
        profile["step"] = 3
        await message.channel.send(
            "🧾 **3. What type of opportunities are you interested in?**\n_(e.g., Backend Developer Internship, AI Research Thesis, QA position...)_"
        )

    elif step == 3:
        profile["interests"] = message.content.strip()
        profile["step"] = 4
        await message.channel.send(
            "📄 **4. Do you have a resume you'd like to share?**\nYou can upload it here or share a link (Google Drive, PDF, etc.)"
        )

    elif step == 4:
        profile["resume"] = message.content.strip()
        profile["step"] = 5
        # Özet ve örnek fırsat mesajı
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
            "📝 Description: You’ll join a team building REST APIs…\n\n"
            "Would you like to: bookmark / more / stop?"
        )

    elif step == 5:
        response = message.content.lower().strip()
        if response == "bookmark":
            await message.channel.send("✅ Bookmarked! 🎉")
            profile["step"] = 6
        elif response == "more":
            await message.channel.send("🔍 Here’s another one...")
        elif response == "stop":
            await message.channel.send("👋 Stopping. You can start again with !start.")
            profile["step"] = -1

    # Save the updated user profiles
    save_profiles(user_profiles)
