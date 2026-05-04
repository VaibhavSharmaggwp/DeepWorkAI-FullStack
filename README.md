# DeepWorkAI: The Ultimate Privacy-First Productivity Ecosystem 🚀

DeepWorkAI is a sophisticated, full-stack productivity suite designed for knowledge workers who want to master their focus. By combining real-time app tracking, machine learning-driven insights, and a sleek, modern interface, DeepWorkAI helps you enter and maintain the "Flow State" while protecting your privacy.

## ✨ Key Features

### 📱 Android Application (Jetpack Compose)
*   **Intelligent Session Tracking**: Start and stop deep work sessions with a single tap.
*   **Dynamic Analytics Dashboard**: Visualize your productivity with beautiful charts for Weekly and Monthly performance.
*   **Cognitive Load Monitoring**: Real-time tracking of your mental stamina based on focus duration and intensity.
*   **Smart Distraction Whitelisting**: Select only the apps you need for work; DeepWorkAI monitors the rest without invading your privacy.
*   **Session History**: A detailed log of every deep work session, complete with stability scores and burnout risk assessments.
*   **Animated AI Entry**: A pulsating, beautiful chatbot banner for a premium user experience.

### 🤖 AI Productivity Assistant (LLM Integration)
*   **Context-Aware Chatbot**: Integrated **Qwen-2.5-72B-Instruct** model via HuggingFace Inference API.
*   **Data-Driven Insights**: The AI analyzes your real focus scores and distraction patterns to give personalized advice.
*   **Schedule Optimization**: Share your daily routine with the AI to get a tailored productivity plan.
*   **Conversational UI**: A sleek Modal Bottom Sheet interface for seamless interaction.

### ⚙️ Robust Backend (Ktor)
*   **Scalable Architecture**: Built with Kotlin and Ktor for high performance and low latency.
*   **Secure Authentication**: JWT-based security and Google OAuth integration for seamless, safe login.
*   **PostgreSQL Persistence**: Reliable data storage for all your focus history and analytics.
*   **Python ML Bridge**: Seamlessly communicates with the machine learning service using ProcessBuilder.

### 🧠 Machine Learning Service (Python)
*   **Burnout Risk Prediction**: Analyzes session duration and distraction frequency to warn you before you overwork.
*   **Distraction Recommendations**: Identifies which apps are your biggest "focus killers" and suggests interventions.
*   **Flow State Analysis**: Uses data-driven models to predict your "Cognitive Peak" hours.
*   **Automated PDF Reporting**: Generates comprehensive focus reports to track your long-term progress.

## 🏗️ Project Structure

The project is divided into three specialized subfolders:

```
DeepWorkAI-FullStack/
├── DeepWorkAI_UI/      # Android Frontend (Kotlin/Compose)
├── DeepWorkBackend/    # Ktor REST API & Database Layer
└── deepwork_ml/        # Python ML Models & LLM Integration
```

## 🛠️ Technology Stack

*   **Frontend**: Kotlin, Jetpack Compose, Retrofit, Coroutines, Vico Charts, Coil.
*   **Backend**: Ktor, Exposed ORM, PostgreSQL, JWT, BCrypt.
*   **ML/AI**: Python, Scikit-learn, HuggingFace Hub (InferenceClient), Pandas, FPDF.

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Ladybug or newer)
*   IntelliJ IDEA
*   Python 3.10+
*   PostgreSQL 14+

### Setup

1.  **UI**: Open `DeepWorkAI_UI` in Android Studio, sync Gradle, and run.
2.  **Backend**: Configure your `.env` in `DeepWorkBackend` (see `.env.example`) and run `Application.kt`.
3.  **ML**: Install requirements in `deepwork_ml` using `pip install -r requirements.txt`.
4.  **AI Key**: Create a fine-grained token on HuggingFace with `Inference API` permissions and add it to `deepwork_ml/.env`.

---

Developed with ❤️ by [VaibhavSharmaggwp](https://github.com/VaibhavSharmaggwp)
