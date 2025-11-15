# Filadelfia Store — Project Presentation

Filadelfia Store is a virtual store being developed for the church to sell clothing and other items produced by the youth group. The goal is a simple, maintainable online shop that supports product listings, orders, and basic admin management.

## Purpose
- Provide an online channel for the youth group to sell merchandise.
- Manage products, inventory, and orders.
- Keep tech simple and easy to maintain by volunteers.

## Key features (MVP)
- **Multi-role user system** (Admin, Manager, Customer)
- **Product catalog** with categories and image management
- **Shopping cart** with session and database persistence
- **Complete checkout process** with multiple payment methods
- **Order management** with status tracking and notifications
- **Invoice generation** (Nota Fiscal) with PDF export
- **Payment integration** (Credit Card, Boleto bancário, PIX)
- **Role-based dashboards** and access control
- **Responsive UI** using server-side rendering (Thymeleaf)

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
   
   # Email Configuration (for password reset)
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   EMAIL_FROM=noreply@filadelfiastore.com
   BASE_URL=http://localhost:8080
   ```
4. **Email Setup for Gmail:**
   - Enable 2-Step Verification in your Google Account
   - Generate an App Password: Google Account → Security → 2-Step Verification → App passwords
   - Use the generated 16-character password as MAIL_PASSWORD
5. Run with `mvn spring-boot:run` or the IDE run configuration.
6. Seed initial data (products, admin user) or create via admin UI.

## TODO

### Phase 1: MVC User Management & Role-Based Access (Priority: High)
- [x] **1.1 Authentication System** (COMPLETED)
  - [x] MVC Login/Logout Implementation
  - [x] User registration flow
  - [x] Forgot password with email verification
  - [x] Spring Security configuration

- [ ] **1.2 Role-Based Access Control** (6-8 hours)
  - [ ] Update UserRole enum (ADMIN, MANAGER, USER)
  - [ ] Implement role-based navigation menus
  - [ ] ADMIN: Full access to users, categories, products, orders
  - [ ] MANAGER: Access to categories, products, order management (no user management)
  - [ ] USER: Access to shopping cart, orders, profile management
  - [ ] Add method-level security annotations (@PreAuthorize)
  - [ ] Create role-specific dashboards

- [ ] **1.3 Complete User Management (MVC)** (4-6 hours)
  - [ ] Migrate User CRUD from API to MVC controllers
  - [ ] Add proper validation and error handling
  - [ ] Implement user profile management for all roles

### Phase 2: E-Commerce Core Entities (Priority: High)
- [ ] **2.1 Shopping Cart System** (8-10 hours)
  - [ ] Create Cart and CartItem entities
  - [ ] Implement session-based cart for guests
  - [ ] Implement database cart for logged users
  - [ ] Add/remove/update cart items (MVC controllers)
  - [ ] Cart persistence and synchronization
  - [ ] Cart summary and total calculations

- [ ] **2.2 Order Management System** (12-15 hours)
  - [ ] Create Order, OrderItem, OrderStatus entities
  - [ ] Implement checkout process (MVC)
  - [ ] Order status workflow (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
  - [ ] Order history for customers
  - [ ] Order management dashboard for ADMIN/MANAGER
  - [ ] Stock management and validation

- [ ] **2.3 Customer Information** (4-6 hours)
  - [ ] Create Customer entity (extends User)
  - [ ] Add shipping address management
  - [ ] Customer profile with order history
  - [ ] Address validation and formatting

### Phase 3: Payment System (Priority: High)
- [ ] **3.1 Payment Methods Implementation** (15-20 hours)
  - [ ] Create Payment, PaymentMethod entities
  - [ ] Credit Card payment integration
  - [ ] Boleto bancário integration
  - [ ] PIX payment integration
  - [ ] Payment status tracking
  - [ ] Payment confirmation handling

- [ ] **3.2 Invoice Generation (Nota Fiscal)** (10-12 hours)
  - [ ] Create Invoice entity
  - [ ] Generate PDF invoices
  - [ ] Tax calculations (if applicable)
  - [ ] Invoice numbering system
  - [ ] Email invoice to customers
  - [ ] Invoice management for ADMIN/MANAGER

### Phase 4: Product Management Enhancement (Priority: Medium)
- [ ] **4.1 Image Management** (8-10 hours)
  - [ ] Implement file upload for product images
  - [ ] Configure local/cloud storage
  - [ ] Image validation and processing
  - [ ] Multiple images per product
  - [ ] Image galleries in product views

- [ ] **4.2 Product Features** (6-8 hours)
  - [ ] Product variants (size, color, etc.)
  - [ ] Stock management and alerts
  - [ ] Product reviews and ratings
  - [ ] Featured products
  - [ ] Product search and filtering

### Phase 5: Search & Navigation (Priority: Medium)
- [ ] **5.1 Search System** (6-8 hours)
  - [ ] Global product search
  - [ ] Category-based filtering
  - [ ] Price range filtering
  - [ ] Search suggestions and autocomplete
  - [ ] Search results pagination

- [ ] **5.2 Category Management** (4-6 hours)
  - [ ] Migrate Category CRUD to pure MVC
  - [ ] Category hierarchy (if needed)
  - [ ] Category-based product listing
  - [ ] Category image management

### Phase 6: Reporting & Analytics (Priority: Low)
- [ ] **6.1 Admin Reports** (8-10 hours)
  - [ ] Sales reports
  - [ ] Product performance
  - [ ] Customer statistics
  - [ ] Revenue analytics
  - [ ] Export to PDF/Excel

### Phase 7: Testing & Quality Assurance (Priority: High - Continuous)
- [ ] **7.1 MVC Testing** (15-20 hours ongoing)
  - [ ] Controller integration tests
  - [ ] Service layer unit tests
  - [ ] Repository tests
  - [ ] Security configuration tests
  - [ ] Form validation tests
  - [ ] End-to-end workflow tests
  - [ ] Aim for 70%+ test coverage

### Standby Features (Future Implementation)
- [ ] **REST API Implementation** (On Hold)
  - [ ] JWT authentication
  - [ ] API endpoints for mobile app
  - [ ] API documentation with Swagger
  - [ ] Rate limiting and security

### Technical Improvements
- [ ] **Performance & Infrastructure** (Ongoing)
  - [ ] Database query optimization
  - [ ] Caching strategy
  - [ ] Image optimization
  - [ ] Email template improvements
  - [ ] Error handling and logging

### Completed Tasks ✓
- [x] Project architecture setup
- [x] MySQL database configuration
- [x] Spring Security authentication system
- [x] User registration and login flows
- [x] Password reset with email verification
- [x] Basic User, Product, and Category entities
- [x] Initial CRUD operations (API-based)
- [x] Swagger/OpenAPI documentation
- [x] Exception handling framework
- [x] Thymeleaf template structure

### Architecture Decision: MVC-First Approach
The project has pivoted to prioritize **Server-Side MVC architecture** with Thymeleaf templates over REST API implementation. This approach provides:
- Better SEO optimization
- Simpler state management
- Reduced complexity for volunteer maintainers
- Traditional web application user experience
- Built-in CSRF protection

**REST API implementation is on standby** for future mobile application needs.

## User Roles & Permissions

### 🔴 ADMIN Role
- **Full system access**
- Manage users (create, update, delete)
- Manage categories and products
- View and manage all orders
- Access to reports and analytics
- System configuration

### 🟡 MANAGER Role
- **Limited administrative access**
- Manage categories and products
- Process and manage customer orders
- View sales reports
- **Cannot manage users**

### 🟢 USER Role (Customers)
- **Customer-facing features**
- Browse products and categories
- Add items to shopping cart
- Place orders and make payments
- View order history
- Manage personal profile

## Development Roadmap (Sprints)

### Sprint 1 (Weeks 1-2): User Management & Security
1. Complete MVC migration for User CRUD
2. Implement role-based access control
3. Create role-specific dashboards
4. Add comprehensive form validation

### Sprint 2 (Weeks 3-4): E-Commerce Foundation
1. Shopping cart system (session + database)
2. Order management entities and workflows
3. Customer information management
4. Basic checkout process

### Sprint 3 (Weeks 5-6): Payment Integration
1. Payment methods (Credit Card, Boleto, PIX)
2. Invoice generation (Nota Fiscal)
3. Payment status tracking
4. Email notifications

### Sprint 4 (Weeks 7-8): Product Enhancement
1. Image upload and management
2. Product search and filtering
3. Category hierarchy
4. Inventory management

### Sprint 5 (Weeks 9-10): Testing & Polish
1. Comprehensive testing suite
2. Performance optimization
3. Security hardening
4. Documentation and deployment guides

### Future Enhancements (Post-MVP)
- REST API implementation for mobile apps
- Advanced reporting and analytics
- Multi-language support
- Docker containerization
- CI/CD pipeline
- Third-party integrations (shipping, payment gateways)

## Contribution
Contributions from the church community are welcome. Open issues for desired features or tasks. Keep changes small and add tests for core logic when possible.

Contact the project maintainer for access and deployment instructions.