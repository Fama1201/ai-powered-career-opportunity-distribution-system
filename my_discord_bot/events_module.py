# events_module.py
from persistence import save_profiles

async def on_message_handler(bot, message):
    """
    Listens for onboarding steps in DMs only.
    Prefix commands (e.g. !start) are handled by commands.Bot automatically.
    """
    # 1) Ignore any message sent in a guild (public channel)
    if message.guild is not None:
        return

    # 2) Ignore bot messages
    if message.author.bot:
        return

    user_id = str(message.author.id)
    user_profiles = bot.user_profiles

    # 3) If the user hasn't run !start yet, skip
    if user_id not in user_profiles:
        return

    profile = user_profiles[user_id]
    step = profile.get("step", 1)
    content = message.content.strip()

    # ----- Onboarding steps in DMs -----
    if step == 1:
        # Step 1: Save full name, ask for skills
        profile["name"] = content
        profile["step"] = 2
        await message.channel.send(
            "💻 **2. What are your top skills or technologies?**\n"
            "_(e.g., Python, React, SQL, UI/UX...)_"
        )

    elif step == 2:
        # Step 2: Save skills, ask for interests
        profile["skills"] = content
        profile["step"] = 3
        await message.channel.send(
            "🧾 **3. What type of opportunities are you interested in?**\n"
            "_(e.g., Backend Developer Internship, AI Research Thesis, QA position...)_"
        )

    elif step == 3:
        # Step 3: Save interests, ask for resume
        profile["interests"] = content
        profile["step"] = 4
        await message.channel.send(
            "📄 **4. Do you have a resume you'd like to share?**\n"
            "You can upload it here or share a link (Google Drive, PDF, etc.)"
        )

    elif step == 4:
        # Step 4: Save resume info and show first opportunity
        profile["resume"] = content
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
            "📝 Description: You’ll join a team building REST APIs…\n\n"
            "Would you like to: bookmark / more / stop?"
        )

    elif step == 5:
        # Step 5: Handle user's choice
        response = content.lower()
        if response == "bookmark":
            await message.channel.send("✅ Bookmarked! 🎉")
            profile["step"] = 6
        elif response == "more":
            await message.channel.send("🔍 Here’s another one...")
            # stay at step 5 to keep showing matches
        elif response == "stop":
            await message.channel.send("👋 Stopping. You can start again with `!start`.")
            profile["step"] = -1

    elif step == 6:
        # Step 6: Final completion message
        await message.channel.send(
            "🎉 **Your profile is complete!** You will receive personalized opportunities here. "
            "Feel free to restart anytime with `!start`."
        )
        profile["step"] = -1

    # 4) Persist any changes
    save_profiles(user_profiles)
