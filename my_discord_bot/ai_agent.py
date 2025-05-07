# ai_agent.py
from openai import OpenAI
from config import OPENAI_API_KEY

client = OpenAI(api_key=OPENAI_API_KEY)

def generate_opportunity_suggestion(profile):
    name = profile.get("name", "Anonymous")
    skills = ", ".join(profile.get("skills", []))
    interests = ", ".join(profile.get("interests", []))
    resume = profile.get("resume", "Not provided.")

    prompt = f"""
You are an AI assistant that helps students find opportunities.
Here is the user's profile:

Name: {name}
Skills: {skills}
Interests: {interests}
Resume: {resume}

Based on this, suggest a specific opportunity. Respond like a Discord bot with:

- Role title
- Company
- Stack/technologies
- Type of opportunity
- Location
- Short, friendly description
"""

    response = client.chat.completions.create(
     model="gpt-3.5-turbo",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.7
    )

    return response.choices[0].message.content
