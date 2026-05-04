import sys
import os
from dotenv import load_dotenv
from huggingface_hub import InferenceClient

env_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path=env_path)

def ask_ai(query, context, schedule):
    api_key = os.environ.get("HF_API_KEY") 
    
    if not api_key:
        return "API Key is missing. Please check your .env file."
        
    try:
        # Use HuggingFace InferenceClient
        client = InferenceClient(api_key=api_key)
        
        messages = [
            {
                "role": "system",
                "content": f"You are DeepWorkAI, a helpful, encouraging productivity assistant. Keep answers very concise (under 3 sentences) and practical.\nUser's Recent Focus Stats: {context}\nUser's Daily Schedule: {schedule}"
            },
            {
                "role": "user",
                "content": query
            }
        ]
        
        response = client.chat.completions.create(
            model="Qwen/Qwen2.5-72B-Instruct", 
            messages=messages, 
            max_tokens=100,
            temperature=0.7
        )
        
        return response.choices[0].message.content.strip()
    except Exception as e:
        if "403" in str(e) or "Forbidden" in str(e):
            return "Your HuggingFace API key does not have Inference permissions. Please create a new token and check 'Make calls to the Serverless Inference API'."
        return f"Failed to connect to the AI service. Details: {str(e)}"

if __name__ == "__main__":
    if len(sys.argv) > 3:
        query_arg = sys.argv[1]
        context_arg = sys.argv[2]
        schedule_arg = sys.argv[3]
        print(ask_ai(query_arg, context_arg, schedule_arg))
    else:
        print("Error: Missing arguments. Expected query, context, and schedule.")
