# DeepWorkAI: The Ultimate Privacy-First Productivity Ecosystem 🚀

DeepWorkAI is a sophisticated, full-stack productivity suite designed for knowledge workers who want to master their focus. By combining real-time app tracking, machine learning-driven insights, and a sleek, modern interface, DeepWorkAI helps you enter and maintain the "Flow State" while protecting your privacy.

---

## ✨ Key Features

### 🧪 Flow State Lab (New!)
*   **Focus Stability Trend**: Visualize how your focus quality changes across your last 7 sessions.
*   **Cognitive Resilience Metric**: An AI-driven score that measures your ability to resist digital distractions and maintain deep work.
*   **Focus Leaks Analysis**: Rebranding distracting apps as "Leaks" to help you identify where you lose momentum.
*   **Motivational Empty States**: Beautiful, premium UI that reinforces positive focus habits when no distractions are detected.

### ⚡ Vitality & Focus Sync
*   **Focus-Fitness Correlation**: A dedicated dashboard that links physical wellness (Sleep, Hydration, Exercise) to your focus performance.
*   **Daily Vitality Score**: A holistic metric representing your physical readiness for deep work.
*   **AI Vitality Insights**: Personalized recommendations on how to boost focus through lifestyle changes (e.g., "Increase hydration by 2 glasses for 12% better stability").

### 🧩 Daily Cognitive Challenge
*   **Focus Warmup**: A LinkedIn-style daily puzzle (Pattern Memory) to train your working memory before a session.
*   **Gamified Cognitive Training**: Level up your memory capacity and sustained attention through interactive challenges.

### 📅 Smart Task Planner
*   **Deep vs. Shallow Categorization**: AI-driven task sorting based on complexity.
*   **Session-Task Linking**: Associate focus sessions with specific planned tasks for granular productivity insights.
*   **Priority Management**: Organize your day to align with your cognitive peaks.

### 📱 Android Application (Jetpack Compose)
*   **Dynamic Analytics Dashboard**: Visualize your productivity with beautiful charts, heatmaps, and AI-driven reports.
*   **Gamified Consistency**: Stay motivated with the **DeepWork Streak** fire system.
*   **Cognitive Load Monitoring**: Real-time tracking of mental stamina based on focus duration and intensity.
*   **Smart Distraction Whitelisting**: Select only the apps you need; DeepWorkAI monitors the rest without invading privacy.

### 🤖 AI Productivity Assistant (LLM Integration)
*   **Context-Aware Chatbot**: Integrated **Qwen-2.5-72B-Instruct** model via HuggingFace Inference API.
*   **Data-Driven Insights**: The AI analyzes focus scores and distraction patterns to give personalized advice.

### 🧠 Machine Learning Service (Python)
*   **Burnout Risk Prediction**: Warns you before you overwork based on session history.
*   **Professional PDF Reporting**: Generates premium focus reports with trend analysis and AI recommendations.

---

## 🏗️ Project Structure

The project is divided into three specialized subfolders:

```
DeepWorkAI-FullStack/
├── DeepWorkAI_UI/      # Android Frontend (Kotlin/Compose)
├── DeepWorkBackend/    # Ktor REST API & Database Layer (PostgreSQL)
└── deepwork_ml/        # Python ML Models & LLM Integration (AI Layer)
```

---

## 🛠️ Technology Stack

*   **Frontend**: Kotlin, Jetpack Compose, Retrofit, Coroutines, Vico Charts, Coil, Lottie.
*   **Backend**: Ktor (Kotlin Server), Exposed ORM, PostgreSQL, JWT Authentication.
*   **ML/AI**: Python, Scikit-learn, HuggingFace Hub (InferenceClient), Pandas, FPDF.

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Ladybug or newer)
*   IntelliJ IDEA
*   Python 3.10+
*   PostgreSQL 14+

### Setup

1.  **UI**: Open `DeepWorkAI_UI` in Android Studio, sync Gradle, and run.
2.  **Backend**: Configure your environment variables in `DeepWorkBackend` (see `.env.example`) and run `Application.kt`.
3.  **ML**: Install requirements in `deepwork_ml` using `pip install -r requirements.txt`.
4.  **AI Key**: Create a token on HuggingFace with `Inference API` permissions and add it to `deepwork_ml/.env`.

---

Developed with ❤️ by [VaibhavSharmaggwp](https://github.com/VaibhavSharmaggwp)
