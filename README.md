# CyberShield – Personal Cyber Safety Management System

Browser-based conversion of the original Java Swing microproject. The Swing sources in `src/` are unchanged and still compile.

**This is an educational, rule-based tool.** It does not detect every real-world scam and does not provide real-time protection.

---

## Architecture

- **Backend:** Java 21, Spring Boot 3.2, REST APIs, session authentication, JSON files under `backend/data/`
- **Frontend:** React, TypeScript, Vite (`npm run dev`), proxied to the backend
- **Original desktop app:** Java Swing under `src/`

---

## Prerequisites (Windows)

- Node.js 24+ (`node -v`, `npm -v`)
- Eclipse Temurin JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`
- Maven is **not** required. Use the Maven Wrapper in `backend/` (`mvnw.cmd`)

---

## Start the website

Open **two** PowerShell windows.

### 1. Backend

```powershell
cd C:\Users\HP\Desktop\CyberShield\backend
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\mvnw.cmd spring-boot:run
```

Wait until the log shows Tomcat started on port **8080**.

Optional tests:

```powershell
cd C:\Users\HP\Desktop\CyberShield\backend
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\mvnw.cmd test
```

### 2. Frontend

```powershell
cd C:\Users\HP\Desktop\CyberShield\frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Vite proxies `/api` to `http://localhost:8080`.

You can also run `.\run.ps1` from the project root. It opens both processes in new windows.

---

## First-time use

1. Register an account
2. Report an incident and read the risk explanation
3. Add a safety task
4. Open Reports — counts come from stored JSON files, not placeholders
5. Restart the backend; the same user data is still there (`backend/data/*.json`)

Password strength checking runs **only in the browser**. Candidate passwords are not sent to the API.

---

## Original Swing app

```powershell
cd C:\Users\HP\Desktop\CyberShield
.\run-desktop.ps1
```

Or use `compile_and_run.bat` / `src/Main.java` in an IDE.

---

## Project layout

```
CyberShield/
├── src/                 Original Swing application (do not delete)
├── backend/             Spring Boot API + Maven Wrapper
├── frontend/            React + TypeScript + Vite
├── run.ps1              Starts website backend and frontend
└── run-desktop.ps1      Starts original Swing UI
```

Java OOP used in the backend (adapted from the Swing design):

- Encapsulation on models and DTOs
- Inheritance: `Incident` → `PhishingIncident`, `SuspiciousLinkIncident`, `OTPScamIncident`
- Runtime polymorphism: `getTypeSpecificDetails()`
- Abstraction: `RiskAssessor` interface and `RiskAssessmentService`
- Collections and checked validation errors mapped to HTTP status codes

---

## Data

Website data: `backend/data/users.json`, `incidents.json`, `tasks.json`  
Swing data: project-root `data/*.dat`

These stores are separate.
