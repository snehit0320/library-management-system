# Library Management System

A comprehensive digital library management system built with JSF (Jakarta Server Faces), Hibernate ORM, and MySQL database. This application provides a complete solution for managing books, members, and transactions in a library.

## Features

- **Book Management**: Add new books to the library with ISBN validation
- **Member Management**: Track library members with contact information
- **Book Issuing**: Issue books to members with due date tracking
- **Book Returning**: Return books and calculate fines for overdue items
- **Reports**: View currently issued books and overdue items with detailed information
- **Event-Driven Actions**: Event listeners for borrow/return actions
- **Validation**: Comprehensive validation for ISBN, dates, email, and phone numbers
- **Modern UI**: Beautiful and responsive user interface

## Technology Stack

- **Java**: JDK 21
- **JSF**: Jakarta Server Faces 4.0.1
- **CDI**: Jakarta CDI 4.0.1 (Built-in with WildFly)
- **Hibernate**: 6.4.1.Final (ORM Framework)
- **Database**: H2 Database 2.2.224 (Embedded - No separate installation needed)
- **Server**: WildFly 31+ (Recommended) or Apache Tomcat 10
- **Build Tool**: Maven

> **Note**: 
> - **WildFly is recommended** as it has built-in JSF and CDI support, making deployment easier
> - The project uses H2 embedded database by default, which means no separate database server installation is required
> - Hibernate automatically creates the database in memory
> - If you prefer MySQL, you can uncomment the MySQL configuration in `hibernate.cfg.xml` and `pom.xml`

## Prerequisites

1. JDK 21 installed
2. **WildFly 31+** (Recommended) OR Apache Tomcat 10
3. Maven 3.6+ installed

> **For WildFly setup, see [WILDFLY_SETUP.md](WILDFLY_SETUP.md)**

## Database Setup

**Using H2 Embedded Database (Default - Recommended for Development):**

No setup required! H2 is an embedded database that runs in-memory. Hibernate will automatically:
- Create the database on first run
- Create all tables based on your entity classes
- Store data in memory (data persists during application runtime)

The database is configured in `src/main/resources/hibernate.cfg.xml` with:
- Driver: `org.h2.Driver`
- URL: `jdbc:h2:mem:library_db` (in-memory database)
- Username: `sa`
- Password: (empty)

**Note**: With in-memory database, data will be lost when the application stops. For production, consider using a persistent database like MySQL or PostgreSQL.

**Using MySQL (Optional):**

If you prefer MySQL:
1. Install and start MySQL Server
2. Create a database:
   ```sql
   CREATE DATABASE library_db;
   ```
3. Uncomment MySQL configuration in `pom.xml` and `hibernate.cfg.xml`
4. Update database credentials in `hibernate.cfg.xml`
5. Hibernate will automatically create tables on first run

## Project Structure

```
java da3/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/library/
│   │   │       ├── bean/          # JSF Managed Beans
│   │   │       ├── converter/     # JSF Converters
│   │   │       ├── dao/           # Data Access Objects
│   │   │       ├── listener/      # Event Listeners
│   │   │       ├── model/         # Hibernate Entities
│   │   │       └── util/          # Utility Classes
│   │   ├── resources/
│   │   │   └── hibernate.cfg.xml # Hibernate Configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml        # Web Application Configuration
│   │       │   └── faces-config.xml # JSF Configuration
│   │       ├── resources/
│   │       │   └── css/
│   │       │       └── style.css  # Stylesheet
│   │       ├── index.xhtml        # Entry Point
│   │       ├── home.xhtml         # Home Page
│   │       ├── addBook.xhtml      # Add Book Page
│   │       ├── issueBook.xhtml    # Issue Book Page
│   │       ├── returnBook.xhtml   # Return Book Page
│   │       └── reports.xhtml      # Reports Page
└── pom.xml                         # Maven Configuration
```

## Building and Deployment

### 1. Build the Project

```bash
mvn clean package
```

This will create a WAR file in the `target` directory: `library-management-1.0.0.war`

### 2. Deploy to WildFly (Recommended)

**Option A: Manual Deployment**
1. Download and start WildFly (see [WILDFLY_SETUP.md](WILDFLY_SETUP.md))
2. Copy WAR file to `WILDFLY_HOME/standalone/deployments/`
3. Access at: `http://localhost:8080/library-management-1.0.0/`

**Option B: Maven Plugin**
```bash
mvn wildfly:deploy
```

**Option C: Embedded WildFly**
```bash
mvn wildfly:run
```

### 3. Deploy to Tomcat (Alternative)

1. Copy the WAR file to Tomcat's `webapps` directory:
   ```bash
   copy target\library-management-1.0.0.war C:\path\to\tomcat\webapps\
   ```
2. Start Tomcat server
3. Access the application at: `http://localhost:8080/library-management-1.0.0/`

## Usage Guide

### Navigation Flow

1. **Home**: Main dashboard showing quick statistics
2. **Add Book**: Add new books to the library
   - ISBN validation (10 or 13 digits with checksum validation)
   - Required fields: ISBN, Title, Author, Publisher, Publication Date, Quantity
3. **Issue Book**: Issue books to members
   - Select from available books
   - Enter member ID
   - Set issue date and due date (default: 14 days)
4. **Return Book**: Return issued books
   - View all currently issued books
   - Click "Return" button to return a book
   - Automatic fine calculation for overdue books ($5 per day)
5. **Reports**: View comprehensive reports
   - Currently issued books
   - Overdue books with fine amounts

### Validators and Converters

- **ISBN Converter**: Formats ISBN with hyphens for display
- **ISBN Validator**: Validates ISBN format and checksum (10 or 13 digits)
- **Date Converter**: Converts between String and LocalDate
- **Email Validator**: Validates email format
- **Phone Validator**: Validates phone number format (10-15 digits)

### Event Listeners

- **Borrow Action Listener**: Triggered when a book is issued
- **Return Action Listener**: Triggered when a book is returned

## Configuration

### Hibernate Configuration

Edit `src/main/resources/hibernate.cfg.xml` to configure:
- Database connection settings
- Hibernate dialect
- Schema generation strategy (currently set to "update")

### JSF Configuration

Edit `src/main/webapp/WEB-INF/faces-config.xml` to:
- Register custom converters and validators
- Configure navigation rules

## Troubleshooting

### Common Issues

1. **Database Connection Error (if using MySQL)**
   - Verify MySQL is running
   - Check database credentials in `hibernate.cfg.xml`
   - Ensure database `library_db` exists
   - **Note**: With H2 embedded database (default), no separate database server is needed

2. **ClassNotFoundException**
   - Ensure all dependencies are downloaded: `mvn clean install`
   - Check Tomcat classpath includes all JAR files

3. **JSF Pages Not Loading**
   - Verify `web.xml` servlet mapping is correct
   - Check `faces-config.xml` is in `WEB-INF` directory
   - Ensure JSF dependencies are included in WAR
   - Verify `beans.xml` is present in `WEB-INF` for CDI activation

4. **CDI/Managed Bean Issues**
   - Ensure `beans.xml` is in `WEB-INF` directory
   - Verify Weld (CDI implementation) is included in dependencies
   - Check that beans are annotated with `@Named` and appropriate scope annotations

5. **Validation Errors**
   - Check that converters and validators are properly registered in `faces-config.xml`
   - Verify managed beans are annotated correctly

## Development

### Running in Development Mode

1. Use an IDE (Eclipse, IntelliJ IDEA, NetBeans) with Maven support
2. Configure Tomcat server in IDE
3. Deploy and run directly from IDE

### Testing

1. Start MySQL server
2. Create database `library_db`
3. Deploy application to Tomcat
4. Access application and test all features

## License

This project is created for educational purposes.

## Author

Snehit Ram Kumar Y 

