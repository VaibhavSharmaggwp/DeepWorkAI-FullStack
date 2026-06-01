# DeepWorkAI: The Ultimate Privacy-First Productivity Ecosystem 🚀

DeepWorkAI is a sophisticated, full-stack productivity suite designed for knowledge workers who want to master their focus. By combining real-time app tracking, machine learning-driven insights, and a sleek, futuristic interface, DeepWorkAI helps you enter and maintain the "Flow State" while protecting your privacy.

---

## ✨ Core Philosophy & Mathematical Foundation

DeepWorkAI isn't just a timer; it computes real-time neurological and performance metrics. 

### Mathematical Calculations
*   **Cognitive Resilience ($R_c$)**: Measures your neurological resistance to digital distraction algorithms. 
    $$R_c = \max(0, 100 - (D_{total} \times 2))$$
    *(Where $D_{total}$ is the total minutes spent on distracting "Leak" apps during a session)*.
*   **Focus Stability Score ($S_f$)**: The primary metric for your session. It evaluates your total time focused versus interruptions.
    $$S_f = \text{Base Focus Duration} - \text{Distraction Penalty} + \text{Vitality Bonus}$$
*   **Neural Burnout Predictor ($B_p$)**: Alerts you when you approach cognitive overload. The optimal threshold is dynamically calculated, but defaults to a critical limit of 300 minutes (5 hours) of intense focus per day.
    $$B_p = \left( \frac{\sum \text{Focus Minutes Today}}{300} \right) \times 100\%$$

---

## 🛠️ Key Features

### 🧪 Flow State Lab (Futuristic HUD)
*   **Cybernetic Rotating HUD**: A dynamic, glowing visualizer for your Cognitive Resilience score. 
*   **Glowing Cyber Trend Graph**: Replaces boring charts with a neon cyan/purple glowing trend line over a cybernetic grid, plotting your last 7 neural stability scores.
*   **Top Focus Leaks Analysis**: Automatically detects when your attention drifts to known distracting apps and calculates the exact penalty in minutes, displayed in a sleek, "liquid" UI card.
*   **Calibration Mode**: Beautiful empty-state animations ("Awaiting Neural Sync") prevent the app from looking broken when you have no data.

### 🧠 Cognitive Challenges & Brain Training
*   **Dynamic Arcade Menu**: Train your focus with a suite of mini-games categorized by difficulty.
*   **5 Brain-Training Games**: 
    *   **Pattern Sequence (Easy)**: Recreate the flashing memory blocks.
    *   **Color Match (Easy)**: Overcome the Stroop effect by matching color names to text colors under pressure.
    *   **Quick Math (Medium)**: Solve rapid arithmetic equations.
    *   **Reaction Speed (Medium)**: Test your pure reflexes and aim for sub-300ms reaction times.
    *   **Memory Matrix (Hard)**: Memorize and recall complex 4x4 illuminated grid patterns with 3 lives.

### ⚡ Vitality & Focus Sync
*   **Focus-Fitness Correlation**: A dedicated dashboard that links physical wellness (Sleep, Hydration, Exercise) to your focus performance.
*   **AI Vitality Insights**: Personalized recommendations on how to boost focus through lifestyle changes (e.g., "Increase hydration by 2 glasses for 12% better stability").

### 📅 Smart Task Planner & Detailed Insights
*   **Deep vs. Shallow Categorization**: AI-driven task sorting based on cognitive complexity.
*   **Dynamic Session Linking**: When you start a focus session for a specific task, DeepWorkAI automatically imports the task metadata (Title, Category) into the database.
*   **Actionable History Logs**: Instead of generic "Focus Blocks," your history clearly displays exactly what you worked on, along with summarized lifetime analytics (Total Focus Time, Avg Score, Top Category).

### 🤖 AI Productivity Assistant (LLM Integration)
*   **Context-Aware Cortex AI**: Integrated **Qwen-2.5-72B-Instruct** model via HuggingFace Inference API.
*   **Burnout Risk Prediction**: Machine learning algorithms warn you before you overwork based on session history.

---

## 🏗️ Project Architecture

The ecosystem is divided into three specialized environments:

```text
DeepWorkAI-FullStack/
├── DeepWorkAI_UI/      # Android Frontend (Kotlin, Jetpack Compose)
├── DeepWorkBackend/    # Ktor REST API & Database Layer (PostgreSQL)
└── deepwork_ml/        # Python ML Models & LLM Integration (AI Layer)
```

### 💻 Technology Stack
*   **Frontend (Android)**: Kotlin, Jetpack Compose, Retrofit, Coroutines, Vico Charts, Canvas Animations.
*   **Backend (REST API)**: Ktor (Kotlin Server), Exposed ORM, PostgreSQL, JWT Authentication.
*   **ML/AI Server**: Python, Scikit-learn, HuggingFace Hub (InferenceClient), Pandas, FPDF.

---

## 🚀 Getting Started

### Prerequisites
*   **Android Studio** (Ladybug or newer)
*   **IntelliJ IDEA** (For the Ktor backend)
*   **Python 3.10+** (For the ML microservice)
*   **PostgreSQL 14+**

### Local Setup

1.  **Database**: Start your PostgreSQL service and create a database named `deepworkdb`.
2.  **Backend**: 
    *   Navigate to `DeepWorkBackend`.
    *   Rename `.env.example` to `.env` and fill in your DB credentials.
    *   Run `Application.kt` in IntelliJ. The Exposed ORM will automatically create the necessary schema!
3.  **Frontend**:
    *   Open `DeepWorkAI_UI` in Android Studio.
    *   Sync Gradle and run the app on an emulator or physical device.
4.  **AI Layer**:
    *   Navigate to `deepwork_ml` and run `pip install -r requirements.txt`.
    *   Create a token on HuggingFace (with Inference API permissions) and add it to `deepwork_ml/.env`.
    *   Start the Python Flask/FastAPI server.

---

## 🤝 Feel Free to Contribute!

DeepWorkAI is open to contributions from developers, UI designers, and data scientists! Whether you want to improve the AI models, add a new Jetpack Compose animation, or optimize the Ktor database queries, your help is welcome.

**How to Contribute:**
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request!

---

*Developed with ❤️ by [VaibhavSharmaggwp](https://github.com/VaibhavSharmaggwp)*
