import pandas as pd
import sys
import json

def generate_insights(json_data):
    try:
        # Load the data passed from Ktor
        df = pd.DataFrame(json_data)

        if df.empty:
            return {
                "peak": "Not enough data yet",
                "consistency": "Gathering data...",
                "switches_advice": "Keep focused!"
            }
        # 1. Calculate Cognitive Peak
        df['start_time'] = pd.to_datetime(df['start_time'])
        df['hour'] = df['start_time'].dt.hour

        # Find the hour with the highest average focus score
        peak_hour = df.groupby('hour')['focus_score'].mean().idxmax()
        peak_range = f"{peak_hour}:00 and {peak_hour + 2}:30"

        # 2. Calculate Consistency (Simple Week-over-Week improvement)
        # For demo purposes, we compare the last 3 sessions to previous 3
        last_avg = df.tail(3)['focus_score'].mean()
        prev_avg = df.iloc[:-3].tail(3)['focus_score'].mean()

        improvement = 0
        if prev_avg > 0:
            improvement = int(((last_avg - prev_avg) / prev_avg) * 100)

        return{
            "peak": f"Your brain enters flow state fastest between {peak_range}.",
            "consistency": f"Focus consistency improved by {improvement}% compared to last week.",
            "switches": f"You switched apps {int(df['distractions'].iloc[-1])} times during your last session."
        }

    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    # Ktor will pass the entire session history as a JSON string
    data = json.loads(sys.argv[1])
    print(json.dumps(generate_insights(data)))        