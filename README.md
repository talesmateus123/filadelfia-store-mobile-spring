# Filadelfia Store — Project Presentation

Filadelfia Store is a virtual store being developed for the church to sell clothing and other items produced by the youth group. The goal is a simple, maintainable online shop that supports product listings, orders, and basic admin management.

## Purpose
- Provide an online channel for the youth group to sell merchandise.
- Manage products, inventory, and orders.
- Keep tech simple and easy to maintain by volunteers.

## Key features (MVP)
- Product catalog with categories and images
- Shopping cart and checkout
- Order management for administrators
- Basic authentication for admin tasks
- Responsive UI using server-side rendering (Thymeleaf)

## Technology stack
- Backend: Spring Boot (Java)
- Templating: Thymeleaf
- Database: MySQL
- Build: Maven
- Optional: Tailwind for styling, Spring Security for admin auth

## Getting started (developer notes)
1. Clone the repository.
2. Configure application properties (MySQL connection, port).
3. Set up the following environment variables for local development:
   ```env
   DATABASE_URL=jdbc:mysql://localhost:3306/database
   DATABASE_USERNAME=username
   DATABASE_PASSWORD=password
   DDL_AUTO=update
   DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect
   DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
   ```
4. Run with `mvn spring-boot:run` or the IDE run configuration.
5. Seed initial data (products, admin user) or create via admin UI.

## TODO

### Phase 1: Authentication & Security Enhancements (Priority: High)
- [x] **1.1 MVC Logout Implementation** (2-4 hours)
  - [x] Create logout endpoint in SecurityConfig
  - [x] Add logout button to navigation/header template
  - [x] Configure logout success URL
  - [x] Clear session and security context
  - [x] Add CSRF token handling for logout form

- [ ] **1.2 MVC Login Failure Feedback** (2-3 hours)
  - [ ] Configure custom authentication failure handler
  - [ ] Pass error messages to login view
  - [ ] Display user-friendly error messages
  - [ ] Add Thymeleaf conditionals for error alerts
  - [ ] Style error messages consistently

- [ ] **1.3 Bearer Token Security (JWT Implementation)** (8-12 hours)
  - [ ] Add JWT dependencies (jjwt-api, jjwt-impl, jjwt-jackson)
  - [ ] Create JWT utility class (generate, validate, parse)
  - [ ] Implement JWT authentication filter
  - [ ] Create token-based login endpoint `/api/auth/login`
  - [ ] Implement refresh token mechanism
  - [ ] Configure SecurityFilterChain for stateless API
  - [ ] Add JwtTokenProvider class
  - [ ] Add JwtAuthenticationFilter class
  - [ ] Add AuthController for API authentication

- [ ] **1.4 Endpoint Security Configuration** (4-6 hours)
  - [ ] Define public endpoints (home, products, login, register)
  - [ ] Define authenticated endpoints (cart, checkout, profile)
  - [ ] Define admin-only endpoints (admin panels)
  - [ ] Configure role-based access control (ROLE_USER, ROLE_ADMIN)
  - [ ] Add method-level security annotations
  - [ ] Test unauthorized access scenarios

### Phase 2: Data & Timestamp Validation (Priority: Medium)
- [ ] **2.1 Verify created_at and updated_at Functionality** (2-3 hours)
  - [ ] Add @EntityListeners(AuditingEntityListener.class) to entities
  - [ ] Enable JPA Auditing with @EnableJpaAuditing
  - [ ] Add @CreatedDate and @LastModifiedDate annotations
  - [ ] Test timestamp behavior on create/update
  - [ ] Fix timezone issues (ensure UTC storage)
  - [ ] Verify timestamps display correctly in UI

### Phase 3: Search Functionality (Priority: Medium)
- [ ] **3.1 Implement Search Bar in All Flows** (6-8 hours)
  - [ ] Add search endpoints for Products
  - [ ] Add search endpoints for Categories
  - [ ] Add search endpoints for Users
  - [ ] Implement repository LIKE queries or full-text search
  - [ ] Create reusable search form fragment
  - [ ] Add search results page/section
  - [ ] Implement pagination for search results
  - [ ] Add search filters (category, price range)
  - [ ] Handle empty search results gracefully

### Phase 4: Media Management (Priority: Medium)
- [ ] **4.1 Implement photoUrl for Products and Categories** (10-15 hours)
  - [ ] Add photoUrl field to Product entity
  - [ ] Add photoUrl field to Category entity
  - [ ] Implement file upload controller endpoint
  - [ ] Configure file storage (local or cloud)
  - [ ] Add image validation (type, size limits)
  - [ ] Create image upload UI component
  - [ ] Implement image preview before upload
  - [ ] Add default placeholder images
  - [ ] Implement image deletion on entity delete
  - [ ] Optimize image storage (thumbnails, compression)
  - [ ] Create FileStorageService
  - [ ] Create FileUploadController
  - [ ] Add FileStorageProperties configuration

### Phase 5: Testing (Priority: High - Continuous)
- [ ] **5.1 Automated Tests Implementation** (15-20 hours ongoing)
  - [ ] Unit tests for UserService
  - [ ] Unit tests for ProductService
  - [ ] Unit tests for CategoryService
  - [ ] Integration tests for UserRepository
  - [ ] Integration tests for ProductRepository
  - [ ] Integration tests for CategoryRepository
  - [ ] Controller tests for ProductController (MockMvc)
  - [ ] Controller tests for CategoryController (MockMvc)
  - [ ] Controller tests for UserController (MockMvc)
  - [ ] Security configuration tests
  - [ ] JWT authentication integration tests
  - [ ] API endpoint tests
  - [ ] Aim for 70%+ test coverage

### Completed Tasks ✓
- [x] Develop the project architecture
- [x] MySQL database setup
- [x] Develop products resource
- [x] Development of Products CRUD
- [x] Development of Category CRUD
- [x] Development of User CRUD
- [x] Exception handling
- [x] Test Category CRUD
- [x] Fix error messages not dismissing in create_category flow
- [x] Test Products CRUD
- [x] Test User CRUD
- [x] Implement not found handling
- [x] Implement holes within frontend CRUD
- [x] Implement Swagger/OpenAPI documentation

### Future Enhancements (Not in Current Scope)
- Password reset flow with email verification
- Shopping cart persistence (database + session)
- Order management system with status workflow
- Email notifications (order confirmation, password reset)
- Logging and monitoring setup
- Docker containerization
- CI/CD pipeline (GitHub Actions)
- Database migration strategy (Flyway/Liquibase)

## Development Order (Sprints)

### Sprint 1 (Week 1-2): Core Security
1. MVC Logout Implementation
2. MVC Login Failure Feedback
3. Verify created_at/updated_at timestamps
4. Basic unit tests for existing services

### Sprint 2 (Week 3-4): API Security
1. Bearer Token (JWT) Implementation
2. Endpoint Security Configuration
3. Auth controller tests
4. JWT integration tests

### Sprint 3 (Week 5-6): Search & Media
1. Search functionality across all entities
2. Photo upload implementation
3. Image management tests
4. Search integration tests

### Sprint 4 (Week 7): Testing & Polish
1. Complete test coverage (aim for 70%+)
2. Bug fixes and refinements
3. Documentation review
4. Performance optimization

## Contribution
Contributions from the church community are welcome. Open issues for desired features or tasks. Keep changes small and add tests for core logic when possible.

Contact the project maintainer for access and deployment instructions.