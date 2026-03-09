import sys
import joblib
import pandas as pd

# Load the brain i trained
model = joblib.load('burnout_model.pkl')

def predict():
    # Read arguments passed from Ktor
    duration = float(sys.argv[1])
    hour = int(sys.argv[2])
    distractions = int(sys.argv[3])
    score = int(sys.argv[4])


    dist_rate = distractions / (duration + 1)

    input_data = pd.DataFrame([[duration, hour, dist_rate, score]], 
                              columns=['duration_min', 'hour_of_day', 'distraction_rate', 'focus_score'])
    
    # Get prediction (0, 1, or 2)
    prediction = model.predict(input_data)[0]

    # Print only the result so Ktor can read it
    print(prediction)


if __name__ == "__main__":
    predict()