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
        apps = json.loads(data)
        top_app = apps[0]["appName"] if apps else "certain apps"
        return f"You spent a significant amount of time on {top_app}. Studies show the average youth spends ~2 hours daily on such content. Consider limiting usage during focus hours to improve productivity."
    except:
        return "Consider limiting your usage of these apps to improve focus."

if __name__ == "__main__":
    if len(sys.argv) > 1:
        data_arg = sys.argv[1]
        print(get_ai_recommendation(data_arg))
    else:
        print("No data provided.")
