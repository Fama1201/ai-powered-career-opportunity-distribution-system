# events_module.py
import discord
from persistence import save_profiles

async def on_message_handler(message):
    # Bot mesajlarını veya guild (sunucu) ortamındaki mesajları işlemiyoruz.
    if message.guild is not None or message.author.bot:
        return

    user_id = str(message.author.id)
    user_profiles = message.client.user_profiles  # Bot'un global verisine ulaşım
    if user_id not in user_profiles:
        return

    profile = user_profiles[user_id]
    step = profile.get("step", 1)

    # Kullanıcının DM'deki adımlarına göre yanıtlar:
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
            f"Awesome, thanks {profile.get('name', '')}! 🙌\n\n"
            f"I’ve saved your profile — {profile.get('interests', '')}, {profile.get('skills', '')}… got it! ✅\n"
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
            "📝 Description: You’ll join a team working on a real-world SaaS platform, building REST APIs and automating backend workflows.\n\n"
            "Would you like to:\n⿡ Bookmark this opportunity\n⿢ See more options\n⿣ Decline and stop here\n\nJust reply with `bookmark`, `more`, or `stop` — your call! 🙌"
        )
    elif step == 5:
        response = message.content.lower().strip()
        if response == "bookmark":
            await message.channel.send("✅ Awesome! You’ve bookmarked the opportunity:\nJunior Backend Developer @ ByteWave Technologies 🎉")
            await message.channel.send(
                "📬 Good news!\nYour profile has been sent to the company, and they’d like to move forward with an interview!\n\n"
                "Would you like to:\n⿡ confirm the interview\n⿢ reschedule\n⿣ decline this opportunity\n\nJust type your choice."
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
                "📝 Description: Collaborate with senior developers on backend features.\n\n"
                "What would you like to do?\n⿡ bookmark this one\n⿢ more to see another option\n⿣ stop if nothing feels right"
            )
        elif response == "stop":
            await message.channel.send("👋 No worries! You can always type `!start` again whenever you’re ready.")
            profile["step"] = -1
    elif step == 6:
        response = message.content.lower().strip()
        if response == "confirm":
            await message.channel.send(
                "🙌 Awesome — your interview is confirmed!\n\n"
                "If you'd like some interview tips or want to review your profile, just type `prep tips` or `profile`."
            )
            profile["step"] = 7
        elif response == "reschedule":
            await message.channel.send("📅 Okay! Let me know what new time works for you, and I’ll notify the company.")
        elif response == "decline":
            await message.channel.send("❌ Got it — we’ll skip this one and keep looking for other matches!")
            profile["step"] = 5
    elif step == 7:
        response = message.content.lower().strip()
        if response in ["not relevant", "somewhat relevant", "pretty relevant", "very relevant", "perfect match"]:
            await message.channel.send("🎉 Thanks for the feedback! I’ve updated your profile to prioritize better matches in the future.")
            profile["step"] = -1

    # Her adımın sonunda değişiklikleri kalıcı hale getiriyoruz.
    save_profiles(user_profiles)

    # Bot komutlarının da tetiklenmesi için:
    await message.client.process_commands(message)
