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
- [x] develop the project archtecture
- [x] mysql data base setup
- [x] develop products resource
- [ ] development of Products CRUD
- [ ] development of Category CRUD
- [ ] development of User CRUD
- [ ] to implement not found 
- [ ] to implement security
- [ ] exception handling
- [ ] automated tests implementation

## Contribution
Contributions from the church community are welcome. Open issues for desired features or tasks. Keep changes small and add tests for core logic when possible.

Contact the project maintainer for access and deployment instructions.