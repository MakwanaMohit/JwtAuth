# Project Specification: JwtAuth (Spring Boot Backend)

> **Document Purpose**: Comprehensive system architecture, technical specification, and domain reference for the `JwtAuth` Spring Boot application. This file serves as the definitive reference for development, maintenance, and future enhancements.

---

## 1. Executive Summary & Technology Stack

**JwtAuth** is a Spring Boot 3 RESTful web application designed to demonstrate multi-tier JWT authentication, TOTP-based Multi-Factor Authentication (2FA), a bidirectional friendship workflow, and a direct messaging system stored in MongoDB.

### Technology Stack & Version Matrix
| Component | Technology / Library | Version / Details |
| :--- | :--- | :--- |
| **JDK** | Java SE Development Kit | 17 |
| **Framework** | Spring Boot | 3.5.7 |
| **Security** | Spring Security | 6.x (Stateless, Filter Chain) |
| **Database** | MongoDB | Local (Port 27017, DB: `jwtauth`) |
| **ODM / Persistence**| Spring Data MongoDB | 3.5.7 |
| **JWT Provider** | JJWT (`io.jsonwebtoken`) | 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) |
| **TOTP / 2FA** | Sam Stevens TOTP Library | 1.7.1 (`dev.samstevens.totp`) |
| **Boilerplate Reduction**| Project Lombok | 1.18.42 |
| **Build Tool** | Apache Maven | Wrapper included (`mvnw`, `pom.xml`) |

---

## 2. System Architecture & Database Models

The project follows a standard Spring 3-Tier Layered Architecture: `Controller` → `Service` → `Repository` → `MongoDB`.

```
[ HTTP Client ]
      │
      ▼
[ SecurityFilterChain / JWTFilter ]
      │
      ▼
[ REST Controllers ] ──► [ Service Layer ] ──► [ Mongo Repositories ] ──► [ MongoDB ]
  - AuthController          - AuthService           - UserRepository
  - MfaController           - MfaService            - FriendshipRepository
  - FriendshipController    - FriendshipService     - ConversationRepository
  - MessagingController     - MessagingService      - MessageRepository
```

### Data Schema (MongoDB Collections)

#### A. `app-user` Collection (`com.mk.jwtauth.entity.User`)
Implements Spring Security's `UserDetails`. Default authority assigned: `ROLE_USER`.
* `id` (`ObjectId`, `@Id`): Primary key.
* `username` (`String`, `@Indexed(unique = true)`): Unique account handle.
* `password` (`String`): BCrypt-hashed password.
* `email` (`String`): User email address.
* `mfaEnabled` (`Boolean`): Flag indicating if 2FA/TOTP verification is enforced on login.
* `mfaSecret` (`String`): Base32 secret string for TOTP code generation and verification.

#### B. `friendships` Collection (`com.mk.jwtauth.entity.Friendship`)
Compound unique index on `{ "senderId": 1, "receiverId": 1 }`.
* `id` (`ObjectId`, `@Id`): Primary key.
* `senderId` (`ObjectId`): User ID initiating request.
* `receiverId` (`ObjectId`): Target user ID receiving request.
* `senderUsername` (`String`): Cached handle of sender.
* `receiverUsername` (`String`): Cached handle of receiver.
* `status` (`FriendshipStatus` Enum): Relationship state (`ASKED`, `APPROVED`, `REJECTED`, `BLOCKED`).
* `createdAt` (`LocalDateTime`): Timestamp of initial request.
* `updatedAt` (`LocalDateTime`): Timestamp of last state change.

#### C. `conversations` Collection (`com.mk.jwtauth.entity.Conversation`)
* `id` (`ObjectId`, `@Id`): Primary key.
* `participants` (`List<ObjectId>`, `@Indexed`): List containing participant user IDs (typically 2 users).
* `lastMessage` (`String`): Preview content of the most recent message.
* `lastMessageTime` (`LocalDateTime`): Timestamp of the most recent message.

#### D. `messages` Collection (`com.mk.jwtauth.entity.Message`)
* `id` (`ObjectId`, `@Id`): Primary key.
* `conversationId` (`ObjectId`, `@Indexed`): Reference to parent `Conversation`.
* `senderId` (`ObjectId`): Reference to sending `User`.
* `content` (`String`): Text body of message.
* `createdAt` (`LocalDateTime`): Message timestamp.

---

## 3. Security Architecture & Multi-Tier Token Flow

### Multi-Tier Token Security Strategy
To ensure secure authentication and session management without server-side HTTP session state, the system employs three types of JSON Web Tokens (`TokenType` enum):

1. **`TOKEN_TEMPORARY` (5 minutes validity)**:
   - Issued when a user provides valid credentials at `/auth/login` BUT has `mfaEnabled == true`.
   - Security permissions: Restricted **exclusively** to the endpoint `/mfa/verify`. Access to any other protected endpoint is denied (`403 Forbidden`).
2. **`TOKEN_LOGIN` (15 minutes validity)**:
   - Issued directly at `/auth/login` if `mfaEnabled == false`.
   - Issued at `/mfa/verify` upon successful validation of a 6-digit TOTP code when presented with a valid `TOKEN_TEMPORARY`.
   - Issued at `/auth/refresh` when presenting a valid, non-expired `refreshToken` cookie.
   - Transmitted via response body (`token` field).
   - Security permissions: Grants full access to protected endpoints (`/friends/**`, `/messages/**`, `/mfa/setup`, `/mfa/enable`, `/mfa/disable`).
3. **`TOKEN_REFRESH` (7 days validity)**:
   - Issued once on initial authentication completion (`/auth/login` if MFA is disabled, or `/mfa/verify` if MFA is enabled).
   - Transmitted **exclusively** via an HTTP-only Cookie (`Set-Cookie: refreshToken=...; Path=/; Max-Age=604800; HttpOnly; SameSite=Lax`) to prevent client-side JavaScript access / XSS theft.
   - **Fixed 7-Day Expiration Policy**: The refresh token cookie is **not** rotated or re-issued when calling `/auth/refresh`. It remains valid for exactly 7 days from initial login, after which the user must re-authenticate.

```
                  ┌────────────────────────┐
                  │ POST /auth/login       │
                  └───────────┬────────────┘
                              │
                    Is MFA Enabled?
                   /               \
              No  /                 \  Yes
                 ▼                   ▼
    ┌──────────────────────┐   ┌──────────────────────┐
    │ Returns TOKEN_LOGIN  │   │Returns TOKEN_TEMPORARY│
    │ Sets HttpOnly Cookie │   └──────────┬───────────┘
    │ (refreshToken 7 days)│              │
    └──────────────────────┘   ┌──────────┴───────────┐
                               │ POST /mfa/verify     │
                               └──────────┬───────────┘
                                          │ Valid Code?
                                          ▼
                               Returns TOKEN_LOGIN &
                               Sets HttpOnly Cookie
                               (refreshToken 7 days)
```

### Security Components
- **`SecurityConfig`**: Enables stateless session management (`SessionCreationPolicy.STATELESS`), disables CSRF, configures `BCryptPasswordEncoder`, builds custom request authorization rules using `TokenAccess(TokenType)`, and configures CORS via `CorsConfigurationSource` (`setAllowedOriginPatterns` for `http://localhost:*`, `allowCredentials = true`).
- **`JWTFilter`**: Intercepts requests with header `Authorization: Bearer <token>`, validates token payload, and populates `SecurityContextHolder` with `JwtAuthenticationToken`.
- **`JWTUtilis`**: Generates and parses HMAC-SHA256 signed JWTs containing claims: `subject` (username), `email`, `authorities`, `token-type`, `iat`, and `exp`. Supports 5-min (`TOKEN_TEMPORARY`), 15-min (`TOKEN_LOGIN`), and 7-day (`TOKEN_REFRESH`) tokens.
- **`AuthUtil`**: Helper bean to conveniently retrieve the authenticated `User` entity from `SecurityContextHolder`.

---

## 4. API Endpoints Reference

### A. Authentication Endpoint (`/auth`)
* `POST /auth/login` & `GET /auth/login` (Body: `LoginRequest`)
  - Authenticates username and password.
  - Returns `LoginResponse` containing token, userId, and `tokenType` (`TOKEN_LOGIN` or `TOKEN_TEMPORARY`).
  - Sets HTTP-only cookie `refreshToken` (7 days) if `TOKEN_LOGIN` is issued.
* `POST /auth/signup` & `GET /auth/signup` (Body: `SignupRequest`)
  - Registers a new user account with BCrypt-encoded password.
  - Returns `SignupResponse` (`userId`, `username`).
* `POST /auth/refresh` & `GET /auth/refresh` (Cookie: `refreshToken`)
  - Reads `refreshToken` HTTP-only cookie.
  - Validates token validity and token type (`TOKEN_REFRESH`).
  - Returns a fresh short-lived `TOKEN_LOGIN` access token in `LoginResponse`.
  - Does **not** rotate or reset the 7-day refresh token cookie.

### B. MFA / 2FA Management (`/mfa`)
* `POST /mfa/setup` (Requires `TOKEN_LOGIN`)
  - Generates TOTP secret and returns an `otpauth://` URI suitable for QR code scanning in authenticator apps (Google Authenticator, Authy).
* `POST /mfa/enable` (Requires `TOKEN_LOGIN`, Body: `MfaCodeRequest`)
  - Verifies TOTP code and activates `mfaEnabled = true` on account.
* `POST /mfa/verify` (Requires `TOKEN_TEMPORARY`, Body: `MfaCodeRequest`)
  - Verifies 6-digit TOTP code during 2FA login.
  - Upgrades auth state and returns `LoginResponse` with `TOKEN_LOGIN`.
  - Sets HTTP-only cookie `refreshToken` (7 days).
* `POST /mfa/disable` (Requires `TOKEN_LOGIN`, Body: `MfaCodeRequest`)
  - Verifies TOTP code and deactivates MFA (`mfaEnabled = false`, `mfaSecret = null`).

### C. Friendship Operations (`/friends`)
* `GET /friends/users`
  - Returns list of all registered users (`List<UserDetails>`).
* `POST /friends/request` (Body: `SendRequestDTO`)
  - Sends a friend request (`receiverId`). Manages state transitions: re-opens `REJECTED` requests to `ASKED`, prevents duplicate pending requests.
* `POST /friends/request/cancel` (Body: `SendRequestDTO`)
  - Cancels an outgoing request if status is `ASKED` or `REJECTED`.
* `GET /friends/requests/sent`
  - Lists outgoing requests sent by current user.
* `GET /friends/requests`
  - Lists incoming pending requests (`ASKED`) targeting current user.
* `POST /friends/request/action` (Body: `ActionRequestDTO`)
  - Updates request status (`APPROVED`, `REJECTED`, `BLOCKED`).
* `GET /friends/list`
  - Returns list of current user's approved friends.
* `POST /friends/remove` (Body: `RemoveFriendDTO`)
  - Unfriends a user (removes `APPROVED` friendship).

### D. Messaging Operations (`/messages`)
* `POST /messages/send` (Body: `SendMessageDTO`)
  - Sends a message to `receiverId`. Validates that friendship status is `APPROVED`.
  - Automatically creates or retrieves existing `Conversation` between participants.
* `GET /messages/conversations`
  - Returns list of conversations for current user, including participant info, last message preview, and boolean `canSend` flag.
* `POST /messages/list` (Body: `conversationId` raw string)
  - Fetches message history for specified conversation ordered by `createdAt` descending.

---

## 5. Standardized Data Transfer & Error Handling

### API Response Wrapper (`ApiResponse<T>`)
Most endpoints return responses wrapped in `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Global Exception Handler (`GlobalExceptionHandler`)
Intercepts exceptions across all `@RestController` components:
- `IllegalArgumentException` → HTTP 400 (Bad Request)
- `IllegalStateException` → HTTP 409 (Conflict)
- `RuntimeException` → HTTP 404 (Not Found)
- `MethodArgumentNotValidException` → HTTP 400 (Validation Error message)
- `HttpMessageNotReadableException` → HTTP 400 ("Invalid request body")
- `HttpRequestMethodNotSupportedException` → HTTP 405 ("Method not allowed")

---

## 6. Helper Scripts & External Tools

1. **`send_message.py`**:
   - Python automation script utilizing `requests`.
   - Simulates multiple users logging in, populating user IDs, establishing friendships, and exchanging simulated conversation streams.
2. **`showqr.py`**:
   - Tkinter GUI utility.
   - Takes `otpauth://` URI strings generated by `/mfa/setup` and renders QR code images locally on screen for easy pairing with authenticator mobile apps.
3. **`jdbc_viwa.java`**:
   - Standalone educational Java file (outside Spring container context) demonstrating PostgreSQL JDBC operations (scrollable & updatable result sets).

---

## 7. Environment Configuration Reference & Deployment Setup

### A. `.env` File Schema
System settings, secrets, and origins are externalized into `.env` (excluded from git) and `.env.example` (template):

```env
SERVER_PORT=8080
APP_NAME=JwtAuth
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=jwtauth
MONGODB_USERNAME=
MONGODB_PASSWORD=
JWT_SECRET=0943j8ft78rirfiumctu483oarei0r$^GUGT^&&FR$R&UHI(Y^*(YT*(HU*Y^$$#E#kceric93urcyn4qm94cmrifjed
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173
```

### B. Spring `application.properties` Placeholders
```properties
server.port=${SERVER_PORT:8080}
spring.application.name=${APP_NAME:JwtAuth}

spring.data.mongodb.host=${MONGODB_HOST:localhost}
spring.data.mongodb.port=${MONGODB_PORT:27017}
spring.data.mongodb.database=${MONGODB_DATABASE:jwtauth}
spring.data.mongodb.username=${MONGODB_USERNAME:}
spring.data.mongodb.password=${MONGODB_PASSWORD:}
spring.data.mongodb.auto-index-creation=true

logging.level.org.springframework.security=DEBUG
jwt.secretkey=${JWT_SECRET}
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:3000}
```

### C. Dotenv Initialization
- `JwtauthApplication.java` invokes `Dotenv.configure().ignoreIfMissing().load()` on startup to populate Java System properties from `.env` during local execution and Maven test runs (`./mvnw test`).
- Production deployments can override these variables via standard environment variables or container configuration (Docker / Kubernetes / Cloud).

---

## 8. Summary of Package Structure

```
com.mk.jwtauth
├── JwtauthApplication.java
├── configs
│   └── TotpConfig.java
├── controller
│   ├── AuthController.java
│   ├── FriendshipController.java
│   ├── GlobalExceptionHandler.java
│   ├── HelathCheck.java
│   └── MfaController.java
├── dto
│   ├── ActionRequestDTO.java
│   ├── ApiResponse.java
│   ├── FriendRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MessageResponse.java
│   ├── MfaCodeRequest.java
│   ├── MfaSetupResponse.java
│   ├── RemoveFriendDTO.java
│   ├── SendRequestDTO.java
│   ├── SentRequestDTO.java
│   ├── SignupRequest.java
│   ├── SignupResponse.java
│   ├── TokenData.java
│   └── UserDetails.java
├── entity
│   ├── Conversation.java
│   ├── Friendship.java
│   ├── FriendshipStatus.java
│   ├── Message.java
│   └── User.java
├── messaging
│   ├── ConversationDTO.java
│   ├── MessageDTO.java
│   ├── MessagingController.java
│   ├── MessagingService.java
│   └── SendMessageDTO.java
├── repository
│   ├── ConversationRepository.java
│   ├── FriendshipRepository.java
│   ├── MessageRepository.java
│   └── UserRepository.java
├── security
│   ├── AuthUtil.java
│   ├── JWTFilter.java
│   ├── JwtAuthenticationToken.java
│   ├── SecurityConfig.java
│   └── UserDetailsService.java
├── service
│   ├── AuthService.java
│   ├── FriendshipService.java
│   ├── MfaService.java
│   ├── TokenType.java
│   └── UserService.java
└── utilis
    └── JWTUtilis.java
```
