# 🎓 University Management Portal

> A full-stack academic management system built with Angular and Spring Boot, featuring secure role-based access control for administrators, teachers, and students.

This project is developed as part of the Information Systems Engineering (GISI) Final Degree Project (TFG) curriculum.

## 🚀 Key Features

* **🔐 Secure Authentication:** JWT-based stateless authentication and authorization using Spring Security.
* **👥 Role-Based Dashboards:** * **Admin:** Complete system overview, user provisioning, and degree/subject management.
    * **Teacher:** Faculty workspace to manage assigned classes, view student rosters, and handle grading.
    * **Student:** Personalized dashboard to enroll in degree-specific subjects and track academic progress.
* **📚 Academic Catalog:** Dynamic assignment of subjects to specific degree programs.
* **📱 Responsive UI:** Modern, responsive, and animated user interface built with Bootstrap and standalone Angular components.

## 🛠️ Tech Stack

**Frontend**
* [Angular 17+](https://angular.dev/) (Standalone Components, Routing, RxJS)
* [Bootstrap 5](https://getbootstrap.com/) & Bootstrap Icons
* HTML5 / CSS3 / TypeScript

**Backend**
* [Spring Boot 3+](https://spring.io/projects/spring-boot) (Java)
* Spring Security & JWT
* Spring Data JPA (Hibernate)

**Database**
* [PostgreSQL](https://www.postgresql.org/)

## ⚙️ Local Setup & Installation

### Prerequisites
* Node.js and npm installed
* Angular CLI installed globally (`npm install -g @angular/cli`)
* Java Development Kit (JDK) 17 or higher
* PostgreSQL running locally (Port 5432)

### Backend (Spring Boot)
1. Open the backend project in IntelliJ IDEA (or your preferred IDE).
2. Configure your PostgreSQL database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
   spring.datasource.username=your_postgres_user
   spring.datasource.password=your_postgres_password