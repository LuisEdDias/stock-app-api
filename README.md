# StockApp API
## 📌 Overview
This API was originally developed to support an internal stock management system, with a strong focus on hands-on learning using Java and Spring Boot.
As the project evolved and the domain complexity increased, the application entered a structured refactoring process to improve architecture, organization, and generalization.

The current goal is to transform this system into a professional portfolio project, aligned with real-world software engineering best practices.

The main focus at this stage is the backend API, prioritizing clean architecture, domain modeling, security, and extensibility.


## 🎯 Project Goals
- Provide a REST API for corporate stock management
- Apply best practices related to:
  - Layered architecture
  - Domain-driven design principles
  - JWT-based security 
  - Code organization and documentation
- Evolve a functional system into a well-structured technical case
- Serve as a main portfolio project for software engineering positions


## 🧱 Technologies
- Java 17
- Spring Boot
- Spring Security
- JWT (Auth0)
- PostgreSQL
- Docker
- Swagger / OpenAPI (in progress)
- JUnit 5 / Mockito
- Maven


## 🏗️ Architecture (Work in Progress)
The application follows a domain-oriented layered architecture, with clear separation of responsibilities:
- Controller (REST API)
- Service (business rules)
- Repository (data access)
- DTOs (request / response models)
- Shared modules (exceptions, validation, utilities)

> ### ⚠️ Note
> This repository initially contains a legacy version of the project.  
> The refactoring process is being performed incrementally, with small and well-documented commits, allowing the evolution of the system to be clearly tracked.


## 🔐 Authentication and Security
- JWT-based authentication
- Role-based access control
- Endpoint protection using Spring Security

> During the refactoring process, the authorization model is being generalized, removing company-specific rules to make the API reusable in different contexts.


## 📦 Domain Overview

The API currently covers the following main domains:
- User
- Item
- Item Model
- Category
- Inventory
- Box

As part of the ongoing redesign, the item domain is being improved to include more realistic concepts, such as:
- Generic item status
- Decoupled inspection / quality process
- Optional acquisition or purchase batch information


## 📊 Reporting and Analytics

The API provides endpoints for stock-related statistics, including:
- Total number of items
- Aggregations by status, category, model, inventory, and box

Statistics are being isolated into a dedicated module to improve:
- Performance
- Maintainability
- Scalability


## 🚀 Running the Project
### Prerequisites
- Java 17+
- Maven
- Docker (optional)
- PostgreSQL

### Basic execution
In the project's root directory:

1. Rename the `application-dev.example.yml` to `application-dev.yml`
2. Replace the placeholders with your local configuration
3. Make sure PostgreSQL is running
4. Run the application with:
``` bash
./mvnw clean install
# Or use './mvnw clean install -DskipTests' to skip the tests

java -jar target/StockApp-API-V1.jar
```

### Running with Docker (work in progress)
In the project's root directory:
```` bash
docker-compose up --build
````


## 📄 API Documentation (in progress)

The API documentation is available via Swagger / OpenAPI:

`http://localhost:8080/swagger-ui.html`


## 🛠️ Project Status

### 🚧 Actively under refactoring

### Current focus:
- Authentication system improvement
- Package and module reorganization
- Business rule generalization
- Item domain redesign
- Technical documentation
- Gradual introduction of automated tests


## 🗺️ Roadmap (High Level)
- Complete refactoring of the Item module
- Generalize roles and permissions
- Improve item creation and validation rules
- Fully isolate statistics module
- Add service-level tests
- Add architectural documentation


## 👤 Author

Luís Eduardo Dias  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Perfil-blue?logo=linkedin)](https://www.linkedin.com/in/luisvdias94)  
Backend / Full Stack Developer  
Java • Spring Boot • Angular • PostgreSQL