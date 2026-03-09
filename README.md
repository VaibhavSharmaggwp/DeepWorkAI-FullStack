# DeepWorkAI-FullStack

This repository contains the full-stack code for DeepWorkAI, separated into three main components:
1. **[UI] DeepWorkAI_UI**: The Android/Kotlin Jetpack Compose frontend application.
2. **[Backend] DeepWorkBackend**: The Ktor-based Kotlin backend managing the API, database connectivity, and authentication.
3. **[ML] deepwork_ml**: The Python machine-learning service with its models and scripts.

## Setup Instructions

### 1. UI (Android App)
- Open `DeepWorkAI_UI` in Android Studio.
- Create a `local.properties` file in `DeepWorkAI_UI/local.properties` (if it does not exist) and add the following lines to run locally:
  ```properties
  BACKEND_IP=10.0.2.2
  GOOGLE_CLIENT_ID=your_google_client_id_here
  ```
- Sync Project with Gradle Files and Run on an Emulator or Physical Device.

### 2. Backend (Ktor)
- Open `DeepWorkBackend` in IntelliJ IDEA or Android Studio.
- Create a `.env` file in the root of `DeepWorkBackend/` and provide the JWT Secret:
  ```env
  JWT_SECRET=your_secret_key_here
  ```
- Configure your local PostgreSQL database credentials inside the project as needed, or provide them via additional `.env` variables.
- Run the `Application.kt` `main` file or run `.\gradlew run`.

### 3. Machine Learning Service
- Navigate to the `deepwork_ml` folder in your terminal.
- Create a virtual environment and install the required dependencies:
  ```bash
  python -m venv venv
  .\venv\Scripts\activate
  pip install -r requirements.txt
  ```
- Make sure to add any requisite `.env` files internally for ML-specific secrets.
