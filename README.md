# SkillExchange 🤝

A peer-to-peer skill-sharing Android application that connects people within local communities to exchange services and expertise — no money required, just skills.

> **"Trade your skills. Build your community."**

---

## 📱 Screenshots

<table>
  <tr>
    <td align="center"><b>Registration</b></td>
    <td align="center"><b>Login</b></td>
    <td align="center"><b>Reset Password</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/Registration.jpeg" width="220"/></td>
    <td><img src="screenshots/Log_in.jpeg" width="220"/></td>
    <td><img src="screenshots/Reset_Password.jpeg" width="220"/></td>
  </tr>
  <tr>
    <td align="center"><b>Home / Skill Board</b></td>
    <td align="center"><b>Post a Need</b></td>
    <td align="center"><b>My Swaps</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/Home.jpeg" width="220"/></td>
    <td><img src="screenshots/Post.jpeg" width="220"/></td>
    <td><img src="screenshots/Swap.jpeg" width="220"/></td>
  </tr>
</table>

---

## ✨ Features

- **Skill Board (Feed)** — Browse community posts filtered by skill category with real-time search
- **Post a Need** — Request help from your community by selecting the skill you need
- **Skill Swaps** — Propose, accept, and manage skill exchange agreements between users
- **In-App Chat** — Real-time messaging within each swap for coordination
- **AI Skill Suggestions** — Get personalized recommendations on skills to learn next
- **Leaderboard** — Points and trust score system that rewards completed swaps
- **Village Profiles** — Community-level view of active skill sharers in your area
- **Dual Authentication** — Sign in via Email/Password or Phone OTP (Firebase Auth)
- **Dark Mode** — Clean dark UI throughout the app

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Backend | Firebase Firestore (real-time) |
| Authentication | Firebase Auth (Email + Phone OTP) |
| Navigation | Jetpack Navigation Compose |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle |

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** clean architecture:

```
app/
├── data/
│   ├── model/          # Data classes: User, Post, Swap, Message
│   └── repository/     # FirestoreRepository — all Firestore operations
├── navigation/         # NavGraph — type-safe navigation routes
├── ui/
│   ├── screens/        # Composable screens (Feed, Chat, Profile, etc.)
│   └── theme/          # Material 3 theme config
└── viewmodel/          # AppViewModel — single source of truth for UI state
```

- **AppViewModel** manages all UI state using `StateFlow` and exposes actions to the UI
- **FirestoreRepository** abstracts all database operations and returns `Flow<T>` for real-time updates
- Screens observe state reactively — no direct database access from UI layer

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24+
- A Firebase project with Firestore and Authentication enabled

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/abhishekmanasali1606/SkillExchange.git
   cd SkillExchange
   ```

2. **Create a Firebase project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project and register an Android app with package name `com.skillexchange`
   - Enable **Firestore Database** and **Authentication** (Email/Password + Phone)
   - Download `google-services.json` and place it in the `app/` directory

3. **Open in Android Studio**
   - Open the project and let Gradle sync complete
   - Run on an emulator or physical device (API 24+)

---

## 📊 Key Metrics (Test Results)

- **90%+ skill matching accuracy** across 50+ test users
- **Real-time sync** — Firestore listeners update UI within milliseconds
- Supports **dual authentication** flows: Email/Password and Phone OTP

---

## 🔄 How It Works

1. **Sign up** using email + phone OTP verification
2. **Set up your profile** — enter the skill you offer and the skill you want to learn
3. **Browse the Skill Board** — see posts from others in your community, filter by skill
4. **Propose a Swap** — send a swap request to someone whose skill you need
5. **Chat** — coordinate via in-app messaging once a swap is accepted
6. **Confirm completion** — both parties confirm to earn points and trust score
7. **Climb the Leaderboard** — top skill sharers gain community recognition

---

## 🧠 AI Skill Suggestions

The app includes an AI-powered suggestion engine that analyses your current skill profile and recommends complementary skills to learn — helping users grow and increase their swap opportunities within the community.

---

## 📄 License

This project is licensed under the MIT License.

---

## 👤 Author

**Abhishek Manasali**
- GitHub: [@abhishekmanasali1606](https://github.com/abhishekmanasali1606)
- LinkedIn: [abhishekmanasali2004](https://linkedin.com/in/abhishekmanasali2004)
- Email: abhishekmanasali2004@gmail.com
