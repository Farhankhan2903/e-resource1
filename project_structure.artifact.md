# E-Resource Solution: Enterprise Folder Structure

## 📱 Android (Kotlin + Jetpack Compose)
```text
app/
├── src/main/java/com/eresource/solution/
│   ├── data/
│   │   ├── local/              # Room DB, DataStore, SharedPreferences
│   │   ├── remote/             # Retrofit API, WebSockets
│   │   ├── repository/         # Implementation of Repository Pattern
│   │   └── models/             # DTOs, Entities, Domain Models
│   ├── di/                     # Hilt Modules
│   ├── domain/                 # UseCases & Business Logic
│   ├── ui/
│   │   ├── components/         # Reusable Premium UI Elements
│   │   ├── theme/              # Material 3 Design Tokens
│   │   └── screens/            # Feature-specific screens (MVVM)
│   │       ├── auth/
│   │       ├── marketplace/    # New Tool Rental Module
│   │       ├── worker/
│   │       └── admin/
│   └── utils/                  # Formatters, Extensions, Constants
```

## 🌐 Backend (Next.js + Node.js)
```text
backend/
├── lib/                        # Database Connection, Auth Middleware
├── pages/api/
│   ├── auth/                   # Login, Signup, Refresh Token
│   ├── marketplace/            # Shop & Tool Management
│   ├── booking/                # Rental Workflow
│   ├── payment/                # Razorpay Integration
│   └── notifications/          # FCM Push logic
├── public/                     # Static Assets, Uploads
└── schema.sql                  # Enterprise MySQL Schema
```
