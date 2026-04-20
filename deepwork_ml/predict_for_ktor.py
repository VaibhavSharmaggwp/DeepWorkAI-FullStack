import sys

def predict():
    # Read arguments passed from Ktor
    duration = float(sys.argv[1])
    hour = int(sys.argv[2])
    distractions = int(sys.argv[3])
    score = int(sys.argv[4])

    # Rule-based logic requested by the user
    # 0 = Low, 1 = Medium, 2 = High
    risk = 1 

    if duration < 20: 
        risk = 0  # low burnout if not enough time
    elif duration > 60 and distractions <= 2:
        risk = 2  # high burnout if hyper-focusing without breaks
    elif score < 50 and duration > 30:
        risk = 2  # high burnout if struggling
    elif duration > 90:
        risk = 2  # high burnout if overworking

    # Print only the result so Ktor can read it
    print(risk)

if __name__ == "__main__":
    predict()