import joblib
import pandas as pd

# Load the trained model
model = joblib.load('burnout_model.pkl')

def predict_my_burnout(duration, hour, distractions, score):
    # Calculate the distraction rate just like we did in training
    dist_rate = distractions / (duration + 1)
    dist_rate = distractions / (duration + 1)

    # Create a small dataframe for the model
    input_data = pd.DataFrame([[duration, hour, dist_rate, score]], 
                              columns=['duration_min', 'hour_of_day', 'distraction_rate', 'focus_score'])
    
    # Make predictions
    prediction = model.predict(input_data)[0]

    # Map the number back to a human-readable string
    risk_map = {0: "Low Risk ✅", 1: "Moderate Risk ⚠️", 2: "High Risk 🔥"}
    return risk_map[prediction]


# --- Let's test two scenarios ---
if __name__ == "__main__":
    # Scenario A: Short, high-quality session
    print(f"Scenario A (45 min, 2 distractions): {predict_my_burnout(45, 14, 2, 90)}")
    
    # Scenario B: Long, messy session late at night
    print(f"Scenario B (150 min, 25 distractions): {predict_my_burnout(150, 23, 25, 45)}")

