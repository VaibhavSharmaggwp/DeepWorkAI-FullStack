import sys
import json
import requests

def get_ai_recommendation(data):
    # Free tier API for Hugging Face
    API_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2"
    # Placeholder or mock key. 
    # Usually it's better to pass it as an environment variable or store it in config.
    # If the request fails due to 401 Unauthorized, we'll return a fallback message.
    headers = {"Authorization": "Bearer YOUR_API_KEY"} 

    prompt = f"Suggest productivity advice based on this distraction data: {data}. Give a single sentence."
    payload = {
        "inputs": prompt,
        "parameters": {"max_new_tokens": 50, "return_full_text": False}
    }
    
    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=5)
        if response.status_code == 200:
            result = response.json()
            if isinstance(result, list) and len(result) > 0 and "generated_text" in result[0]:
                return result[0]["generated_text"].strip().replace('\n', ' ')
    except Exception as e:
        pass
    
    # Fallback response if API fails
    try:
        sessions = json.loads(data)
        if sessions and isinstance(sessions, list) and "apps" in sessions[0] and len(sessions[0]["apps"]) > 0:
            top_app = sessions[0]["apps"][0]["appName"]
        else:
            top_app = "certain apps"
        return f"You were distracted by {top_app}. Did you know that it takes an average of 23 minutes to regain deep focus after an interrution? Try to use less {top_app} during study hours."
    except Exception as e:
        return "Consider limiting your usage of distracting apps to stay more focused."

if __name__ == "__main__":
    if len(sys.argv) > 1:
        data_arg = sys.argv[1]
        print(get_ai_recommendation(data_arg))
    else:
        print("No data provided.")
