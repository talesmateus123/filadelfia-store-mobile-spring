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

- [x] **1.2 Role-Based Access Control** (COMPLETED)
  - [x] Update UserRole enum (ADMIN, MANAGER, USER)
  - [x] Implement role-based navigation menus
  - [x] ADMIN: Full access to users, categories, products, orders
  - [x] MANAGER: Access to categories, products, order management (no user management)
  - [x] USER: Access to shopping cart, orders, profile management
  - [x] Add method-level security annotations (@PreAuthorize)
  - [x] Create role-specific dashboards

- [ ] **1.3 Complete User Management (MVC)** (4-6 hours)
  - [ ] Migrate User CRUD from API to MVC controllers
  - [ ] Add proper validation and error handling
  - [ ] Implement user profile management for all roles

- [x] **1.3.1 CSS abstraction** (COMPLETED)
  - [x] Search and remove unused css styles
  - [x] Separate style.css file in abstracted files

### Phase 2: E-Commerce Core Entities (Priority: High)
- [x] **2.1 Shopping Cart System** (COMPLETED)
  - [x] Create Cart and CartItem entities
  - [x] Implement session-based cart for guests
  - [x] Implement database cart for logged users
  - [x] Add/remove/update cart items (MVC controllers)
  - [x] Cart persistence and synchronization
  - [x] Cart summary and total calculations

- [x] **2.2 Order Management System** (COMPLETED - MVC Structure)
  - [x] Create Order, OrderItem, OrderStatus entities
  - [x] Implement checkout process (MVC)
  - [x] Order status workflow (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
  - [x] Order history for customers
  - [x] Order management dashboard for ADMIN/MANAGER
  - [ ] Stock management and validation (pending business logic)

- [x] **2.3 Feature Products Page** (COMPLETED - November 2025)
  - [x] Create Feature Products as the Home page to the customer
  - [x] Create A Category base view for the customer
  - [x] All thif flows has to have search feature
  - [x] When clicking some product, the Customer will hava a detailed view with images (TODO), descrition, Add to cart button.
  - [x] Create the cart page
  - [x] Implement shipping address flow
  - [x] Implement user feedback comments when finishing the purchase
  - [x] Implement Payment page (Don't implement Payment yet, only the view)
  - [x] Create a feedback section for the product details page
  - [x] When disable a product it's not possible to activate it more
  - [x] The Icons within the "h4" tags, in the "action-card"'s div are not working well, try to use ion-icons folder instead

- [ ] **2.4 Customer Information** (4-6 hours)
  - [ ] Create Customer entity (User field)
  - [ ] Add shipping address management
  - [ ] Customer profile with order history
  - [ ] Address validation and formatting

### Phase 3: Payment System (Priority: High)
- [x] **3.1 Payment Methods Implementation** (15-20 hours) - COMPLETED
  - [x] Create Payment, PaymentMethod entities
  - [x] Credit Card payment integration
  - [x] Boleto bancário integration
  - [x] PIX payment integration
  - [x] Payment status tracking
  - [x] Payment confirmation handling

- [ ] **3.1.1 Payment Gateway Implementation** (2 hours)
  - [ ] Integrate payment with some payment gateway, like, PayPal, or PagSeguro

- [ ] **3.2 Implement erro logging throught crashlytics or similar** (10-12 hours)

- [ ] **3.3 Invoice Generation (Nota Fiscal)** (10-12 hours)
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
- [x] **MVC Architecture Implementation** (November 2025)
  - [x] Complete role-based access control (ADMIN, MANAGER, USER)
  - [x] Role-specific dashboards and navigation
  - [x] Cart and Order entities with business logic
  - [x] Shopping cart system (session + database persistence)
  - [x] Order management system with status workflow
  - [x] MVC controllers for cart, orders, and checkout
  - [x] Repository layer with custom queries
  - [x] Service layer with business logic
  - [x] DTO mapping and validation
  - [x] Thymeleaf templates for all cart/order flows
  - [x] Payment method and order status enums
  - [x] Build compilation and runtime fixes
  - [x] **CSS Architecture Modularization** (November 2025)
    - [x] CSS abstraction and removal of unused styles
    - [x] Modular CSS component architecture
    - [x] Created component-based CSS files (base, layout, forms, tables, buttons, notifications, navigation, dashboard, utilities, profile)
    - [x] Updated template includes for modular CSS loading
    - [x] Maintained backward compatibility with legacy styles
    - [x] Fixed broken user profile page styling and functionality
    - [x] Added outline button variants and profile-specific styles
  - [x] **Feature Products Page Implementation** (November 2025)
    - [x] Customer-facing homepage with featured products showcase
    - [x] Category-based product browsing and filtering
    - [x] Enhanced search functionality (name and description)
    - [x] Detailed product views with add-to-cart functionality
    - [x] Public product catalog accessible to all users
    - [x] Responsive product grid layouts and modern UI
    - [x] Integration with existing cart system for authenticated users
    - [x] Login prompts and registration call-to-actions for guests
  - [x] **Payment System Implementation** (November 2025)
    - [x] Complete Payment and PaymentMethod entities with JPA mappings
    - [x] Payment status management with lifecycle tracking (PENDING → PROCESSING → CONFIRMED)
    - [x] Credit Card payment processing with gateway simulation
    - [x] PIX payment integration with QR code and copy-paste generation
    - [x] Boleto bancário integration with barcode and due date management
    - [x] Bank Transfer and Cash payment method support
    - [x] Payment repository with comprehensive queries and statistics
    - [x] Payment service layer with business logic and validation
    - [x] Payment web controller with user and admin interfaces
    - [x] Payment confirmation, cancellation, and refund capabilities
    - [x] Processing fee calculation and payment method validation
    - [x] Payment statistics and reporting functionality
    - [x] Webhook/callback handling structure for payment gateways

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