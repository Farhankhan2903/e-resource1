# E-Resource: Appliance Repair & Shared Tool Pool (India 🇮🇳)

A modern, production-ready Appliance Service Marketplace built with **Jetpack Compose (Android)** and **Next.js (Backend)**. This project is localized for the Indian market, supporting INR (₹) and Indian address formats.

---

## 🚀 Quick Start Guide

### 1. Start the Backend Server
The Android app requires the server to be running to handle authentication and data.

1.  Open your terminal.
2.  Navigate to the `backend` directory:
    ```bash
    cd backend
    ```
3.  Install dependencies:
    ```bash
    npm install
    ```
4.  Start the development server:
    ```bash
    npm run dev
    ```
    *The server starts on `http://localhost:4000`. Keep this window open.*

---

### 2. Run the Android App
1.  Open **Android Studio**.
2.  Select **Open** and choose the root folder of this project.
3.  Select an **Emulator** (Recommended: API 34+).
4.  Click the green **Run** button (Shift + F10).

---

### 3. Sign In Credentials
Use the **Quick Login** buttons on the app screen, or enter these manually:

| Role | Email Address | Password |
| :--- | :--- | :--- |
| **Administrator** | `admin@eresource.com` | `admin123` |
| **Customer** | `jane@gmail.com` | `password123` |
| **Technician** | `elite@gmail.com` | `password123` |

---

## 🗄️ Database Setup & Management

### MySQL Setup
By default, the app uses an in-memory fallback for zero-config runs. To use **MySQL**:
1.  Ensure your MySQL server is running.
2.  Create a database named `e_resource_solution`.
3.  Import the schema: `mysql -u root -p e_resource_solution < schema.sql`.
4.  Verify `.env` in the `backend` folder matches your credentials:
    ```env
    DB_HOST=127.0.0.1
    DB_USER=root
    DB_PASS=Root@123456
    DB_NAME=e_resource_solution
    ```

### 📊 Connecting to TablePlus
To view and manage your data visually in TablePlus, use these settings:
*   **Driver**: MySQL
*   **Host**: `127.0.0.1`
*   **Port**: `3306`
*   **User**: `root`
*   **Password**: `Root@123456`
*   **Database**: `e_resource_solution`

---

## 🛠️ Indian Localization Features
*   **Currency**: All pricing (Invoices, Tool Pool, AI Cost Estimates) is in **₹ INR**.
*   **Address Formats**: Supports House No, Street/Area, Landmark, City, State, and 6-digit PIN Codes.
*   **AI Diagnosis**: Provides localized repair cost estimates for the Indian market.
*   **Technician KYC**: 4-step verification process designed for Indian ID standards.

---

*Built with Jetpack Compose, Material 3, Next.js, and Leaflet Maps.*

---

## 🔍 How Everything Works (Technical Architecture)

### 1. The Core Ecosystem
The E-Resource app is a tripartite marketplace connecting **Customers**, **Technicians (Workers)**, and **Shop Owners**.
*   **Customers** use AI to diagnose problems and book verified technicians.
*   **Technicians** receive job bookings and can rent high-end industrial tools from the shared pool to complete specialized repairs.
*   **Shop Owners** list their specialized industrial equipment for rent, generating revenue from technicians and other small shops.

### 2. Smart AI Fault Diagnosis
When a user describes an appliance problem (e.g., "Washing machine making noise"), the app sends this text to a backend AI module. 
*   **Mechanism**: The AI analyzes the symptoms, suggests possible causes, and estimates the repair cost in ₹ INR.
*   **Pre-filling**: Selecting an appliance card on the Home screen pre-fills the category, allowing the user to focus solely on describing the problem symptoms.

### 3. Shared Tool Pool & Marketplace
This is the "Uber for Tools" component.
*   **Role-Based Access**: Both **Technicians** and **Shop Owners** have the privilege to rent equipment from the pool.
*   **Workflow**: Renting a tool decreases the `avail` count in the database. Returning it calculates the total bill based on the `alloc_hour` and the tool's hourly rate.

### 4. Admin Control & KYC
To ensure safety, technicians must undergo a 4-step KYC process.
*   **Admin Dashboard**: Administrators review Aadhaar, PAN, and Shop details. Only after Admin approval can a Technician appear in search results or accept customer bookings.

### 5. Multi-Mode Database
The backend is designed for high availability:
*   **MySQL Mode**: For production, it connects to a structured relational database.
*   **Memory Fallback**: If MySQL is unavailable, the `lib/db.js` interceptor automatically switches to a persistent in-memory state, ensuring the app remains "workable" for testing and demo purposes without complex setup.
