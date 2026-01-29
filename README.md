# User Service

A secure user management service built with Spring Boot, implementing JWT-based authentication and authorization.

The service implements a robust security configuration using Spring Security with:

- Stateless session management
- JWT-based authentication
- Protected endpoints with specific access rules
- CSRF protection disabled for REST API

### Available Endpoints

- Authentication:
    - `POST /auth/register` - Register new user
    - `POST /auth/login` - Authenticate user and get tokens
    - `POST /auth/refresh` - Refresh access token using refresh token
    - `GET /auth/me` - Get current user profile
- Password Management:
    - `POST /auth/change-password` - Change user password
    - `POST /auth/reset/request` - Request password reset
    - `POST /auth/reset/confirm` - Confirm password reset
- Profile:
    - `PATCH /auth/profile` - Update user profile

### Protected Endpoints

- Public endpoints:
    - `POST /auth/login`
    - `POST /auth/register`
    - `POST /auth/refresh`
    - `POST /auth/reset/request`
    - `POST /auth/reset/confirm`
- All other endpoints require valid JWT token

## Authentication

### Authentication Flow

1. Client registers (`POST /auth/register`) or logs in (`POST /auth/login`) with credentials
2. Server validates credentials and returns access and refresh tokens
3. Client includes access token in Authorization header (`Bearer <token>`) for protected endpoints
4. When access token expires, client uses refresh token to get new pair of tokens
5. For password changes or resets, specific endpoints handle the security flow

### JWT Authentication

The service uses a custom JWT Authentication Filter that:

- Extracts JWT token from Authorization header
- Validates token authenticity
- Loads user details
- Sets authentication in Security Context

## Configuration

### Application Configuration

- BCrypt password encoding
- Custom UserDetailsService implementation
- DAO Authentication Provider
- Custom Authentication Manager

### Security Dependencies

- Spring Security
- JWT (JSON Web Tokens)
- Jakarta Validation
- Spring MVC
