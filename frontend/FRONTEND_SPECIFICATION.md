# Frontend Specification: JwtAuth React Client

> **Document Purpose**: Technical specification, component hierarchy, state architecture, and API integration guide for the React frontend of the `JwtAuth` application. This document serves as the permanent reference for all React-related development and future enhancements.

---

## 1. Executive Summary & Tech Stack

The `JwtAuth` Frontend is a single-page React application built with Vite and Axios. It interfaces with the Spring Boot backend (`http://localhost:8080`) implementing multi-tier JWT authentication, TOTP 2FA, bidirectional friend request management, and direct messaging.

### Technology Stack
| Component | Library / Framework | Version / Details |
| :--- | :--- | :--- |
| **UI Library** | React | 18.x |
| **Build Tool** | Vite | 6.x |
| **HTTP Client** | Axios | 1.x (Configured with `withCredentials: true`) |
| **Icons** | Lucide React | Clean, minimalist SVG icons |
| **Styling** | Vanilla CSS | Plain-type, minimal aesthetic, neutral palette |

---

## 2. Security Architecture & Axios Interceptor

### A. Token Management
- **`TOKEN_LOGIN` (Access Token)**: Short-lived token stored in memory (`localStorage` for state persistence across browser reloads). Sent in HTTP headers: `Authorization: Bearer <token>`.
- **`TOKEN_REFRESH` (Refresh Token)**: Long-lived 7-day token stored in HTTP-Only cookie (`refreshToken`). Automatically attached by browser on all requests because `axios.defaults.withCredentials = true`.

### B. Automatic Session Auto-Recovery on Mount
When the application mounts (`App.jsx` `useEffect`), it performs a silent session check:
1. Calls `POST /auth/refresh` with `withCredentials: true`.
2. If successful (valid `refreshToken` cookie exists):
   - Receives new `TOKEN_LOGIN` access token and `userId`.
   - Populates user state and immediately renders the WhatsApp Chat view.
3. If failed (cookie missing or expired):
   - Clears user state and presents the Login / Register screen (`AuthPage`).

### C. 401 Unauthorized Interceptor Logic
Axios is configured with a response interceptor:
```
[ API Request Fails with 401 Unauthorized ]
                    │
                    ▼
       Is this already a retry or /auth/refresh?
                   /         \
              Yes /           \ No
                 ▼             ▼
       Clear Auth State   Call POST /auth/refresh
       Redirect to Login       /           \
                          Success /         \ Failure
                                 ▼           ▼
                      Save new TOKEN_LOGIN  Clear Auth State
                      Retry original request Redirect to Login
```

---

## 3. UI/UX Architecture & Layout Specifications

Design Aesthetic: Minimalist, clean, plain-type typography, neutral color palette (grays, subtle borders, high contrast readability).

```
┌────────────────────────────────────────────────────────────────────────┐
│  HEADER: [App Name] | User: mohit | MFA: Active [2FA] | [Logout]       │
├───────────────────┬───────────────────────────────────┬────────────────┤
│ SIDEBAR           │ MAIN CHAT WINDOW                  │ REQUEST DRAWER │
│                   │                                   │ (Slidable)     │
│ [Conversations]   │ Header: Chatting with mohit10     │                │
│ [All Users]       │ ───────────────────────────────── │ Incoming (2)   │
│                   │ mohit10: Hey bro                  │  - mohit9      │
│ ┌───────────────┐ │ mohit8:  Working on project       │    [Accept][X] │
│ │ Conversation 1│ │                                   │ Sent (1)       │
│ │ Conversation 2│ │ ───────────────────────────────── │  - mohit12     │
│ └───────────────┘ │ Message Input: [ Type... ] [Send] │    [Cancel]    │
└───────────────────┴───────────────────────────────────┴────────────────┘
```

### Components Matrix

#### 1. Authentication Component (`src/components/Auth/`)
- `AuthPage.jsx`: Toggleable tabs for **Login** and **Register**.
  - Form fields: `username`, `password`, `email` (for register).
  - Handles response token types:
    - If `tokenType === 'TOKEN_TEMPORARY'`: Triggers `MfaModal` for 6-digit TOTP code.
    - If `tokenType === 'TOKEN_LOGIN'`: Saves token, transitions to Main Chat view.
- `MfaModal.jsx`: Modal popup accepting 6-digit TOTP code. Submits to `/mfa/verify`.

#### 2. Layout & Header (`src/components/Layout/`)
- `Header.jsx`: Minimal top navigation bar. Displays current username, MFA status badge, toggle button for Friend Requests drawer, TOTP Setup modal trigger, and Logout button.
- `RequestDrawer.jsx`: Slide-out panel for friend request management:
  - Send Request section (input target user ID or select user).
  - Pending Incoming Requests (`ASKED`) with **Accept**, **Reject**, and **Block** actions.
  - Pending Outgoing Requests (`sent`) with **Cancel** action.
- `TotpSetupModal.jsx`: Modal displaying a scannable QR Code SVG (via `qrcode.react`) generated from the `otpauth://` URI, along with optional manual secret key toggle and 6-digit code verification to activate MFA (`/mfa/enable`).

#### 3. Chat Interface (`src/components/Chat/`)
- `Sidebar.jsx`: Switchable list view between **Conversations** and **All Users**. Includes a **Remove Friend** action button in the Friends list tab.
- `MessageThread.jsx`: Main chat window rendering messages ordered chronologically with sender badges, timestamps, and a **Remove Friend** header button. Automatically disables input when `canSend == false`.
- `MessageInput.jsx`: Controlled text input field with submit handler triggering `/messages/send`. Enabled only when `canSend == true`.

---

## 4. API Endpoint Integration Mapping

| Service Method | HTTP Method | Endpoint | Payload / Header | Description |
| :--- | :--- | :--- | :--- | :--- |
| `authService.login` | `POST` | `/auth/login` | `{ username, password }` | Password auth. Sets `refreshToken` cookie if no MFA |
| `authService.signup` | `POST` | `/auth/signup` | `{ username, password, email }` | User registration |
| `authService.refresh` | `POST` | `/auth/refresh` | Cookie: `refreshToken` | Exchanges cookie for fresh `TOKEN_LOGIN` |
| `authService.mfaVerify` | `POST` | `/mfa/verify` | `{ code }` + `TOKEN_TEMPORARY` | Completes 2FA login, sets `refreshToken` cookie |
| `authService.mfaSetup` | `POST` | `/mfa/setup` | `TOKEN_LOGIN` | Generates TOTP secret URI |
| `authService.mfaEnable` | `POST` | `/mfa/enable` | `{ code }` + `TOKEN_LOGIN` | Activates 2FA on account |
| `friendService.getUsers` | `GET` | `/friends/users` | `TOKEN_LOGIN` | Returns list of all registered users |
| `friendService.getFriends` | `GET` | `/friends/list` | `TOKEN_LOGIN` | Returns list of approved friends |
| `friendService.sendRequest`| `POST` | `/friends/request` | `{ receiverId }` | Sends friend request |
| `friendService.cancelRequest`| `POST`| `/friends/request/cancel` | `{ receiverId }` | Cancels outgoing request |
| `friendService.getPending` | `GET` | `/friends/requests` | `TOKEN_LOGIN` | Lists incoming pending requests |
| `friendService.getSent` | `GET` | `/friends/requests/sent` | `TOKEN_LOGIN` | Lists outgoing pending requests |
| `friendService.action` | `POST` | `/friends/request/action` | `{ senderId, status }` | Accepts/Rejects/Blocks request |
| `messageService.getConversations` | `GET` | `/messages/conversations` | `TOKEN_LOGIN` | Returns user conversations |
| `messageService.getMessages` | `POST` | `/messages/list` | Raw `conversationId` body | Returns message history |
| `messageService.sendMessage` | `POST` | `/messages/send` | `{ receiverId, content }` | Sends direct message |

---

## 5. File Structure Reference

```
frontend/
├── FRONTEND_SPECIFICATION.md
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── index.css
    ├── services/
    │   ├── api.js
    │   ├── authService.js
    │   ├── friendService.js
    │   └── messageService.js
    └── components/
        ├── Auth/
        │   ├── AuthPage.jsx
        │   └── MfaModal.jsx
        ├── Layout/
        │   ├── Header.jsx
        │   ├── RequestDrawer.jsx
        │   └── TotpSetupModal.jsx
        └── Chat/
            ├── Sidebar.jsx
            ├── MessageThread.jsx
            └── MessageInput.jsx
```
