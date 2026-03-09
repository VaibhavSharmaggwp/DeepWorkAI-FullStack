import pandas as pd
import psycopg2
import numpy as np
from dotenv import load_dotenv
import os
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
import joblib

load_dotenv()

def fetch_data():
    try:
        conn = psycopg2.connect(
            dbname=os.getenv('DB_NAME'),
            user=os.getenv('DB_USER'),
            password=os.getenv('DB_PASS'),
            host=os.getenv('DB_HOST'),
            port=os.getenv("DB_PORT")
        )
        query = "SELECT * FROM focus_sessions"
        df = pd.read_sql_query(query, conn)
        conn.close()
        return df
    except Exception as e:
        print(f"⚠️ Database connection skipped: {e}")
        return pd.DataFrame()

def generate_synthetic_data(num_rows=500):
    print(f"Generating {num_rows} rows of synthetic data...")
    data = {
        'duration_min': np.random.randint(10, 180, num_rows),
        'hour_of_day': np.random.randint(0, 24, num_rows),
        'distractions': np.random.randint(0, 30, num_rows),
        'focus_score': np.random.randint(20, 100, num_rows)
    }
    return pd.DataFrame(data)

def preprocess_data(df):
    # If using real DB data, extract features from timestamps
    if 'start_time' in df.columns:
        df['start_time'] = pd.to_datetime(df['start_time'])
        df['end_time'] = pd.to_datetime(df['end_time'])
        df['duration_min'] = (df['end_time'] - df['start_time']).dt.total_seconds() / 60
        df['hour_of_day'] = df['start_time'].dt.hour
    
    # CALCULATE THIS FIRST to avoid KeyError
    df['distraction_rate'] = df['distractions'] / (df['duration_min'] + 1)
    return df

def label_burnout_risk(row):
    # Logic to classify the data for the ML model
    if row['duration_min'] > 120 and row['focus_score'] < 60:
        return 2  # High Risk
    elif row['duration_min'] > 60 and row['distraction_rate'] > 0.5:
        return 1  # Moderate Risk
    else:
        return 0  # Low Risk

def train_burnout_model(df):
    # Select our features
    features = df[['duration_min', 'hour_of_day', 'distraction_rate', 'focus_score']]
    labels = df.apply(label_burnout_risk, axis=1)

    X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.2, random_state=42)
    
    model = RandomForestClassifier(n_estimators=100, random_state=42)
    model.fit(X_train, y_train)
    
    predictions = model.predict(X_test)
    print(f"✅ Model Accuracy: {accuracy_score(y_test, predictions) * 100:.2f}%")
    
    joblib.dump(model, 'burnout_model.pkl')
    print("💾 Model saved as burnout_model.pkl")

if __name__ == "__main__":
    raw_df = fetch_data()
    
    # If DB is empty, use synthetic data for training
    if raw_df.empty or len(raw_df) < 5:
        print("Using synthetic data for initial training...")
        final_df = generate_synthetic_data(500)
    else:
        print(f"Using {len(raw_df)} real sessions.")
        final_df = raw_df

    # Process and Train
    final_df = preprocess_data(final_df)
    train_burnout_model(final_df)