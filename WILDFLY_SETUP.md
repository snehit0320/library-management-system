# WildFly Setup Guide for Library Management System

## Why WildFly?

WildFly is a full Jakarta EE application server that includes:
- ✅ Built-in JSF 4.0 support
- ✅ Built-in CDI (Weld) support
- ✅ Built-in Hibernate/JPA support
- ✅ No configuration needed for JSF/CDI

## Option 1: Download and Install WildFly (Recommended)

### Step 1: Download WildFly

1. Go to: https://www.wildfly.org/downloads/
2. Download **WildFly 31.0.1.Final** (or latest version)
3. Extract it to a folder (e.g., `C:\wildfly`)

### Step 2: Start WildFly

1. Open Command Prompt or PowerShell
2. Navigate to WildFly's `bin` directory:
   ```bash
   cd C:\wildfly\bin
   ```
3. Run:
   ```bash
   standalone.bat
   ```
   (On Linux/Mac: `./standalone.sh`)

4. Wait for WildFly to start (you'll see "WFLYSRV0025" message)

### Step 3: Deploy the Application

**Method A: Copy WAR file**
1. Copy `target\library-management-1.0.0.war` to:
   ```
   C:\wildfly\standalone\deployments\
   ```
2. WildFly will automatically deploy it

**Method B: Use Maven plugin**
```bash
mvn wildfly:deploy
```

### Step 4: Access the Application

Open your browser and go to:
- **http://localhost:8080/library-management-1.0.0/**

## Option 2: Use WildFly Maven Plugin (Embedded)

If you want to run WildFly embedded with Maven:

```bash
mvn wildfly:run
```

This will:
- Download WildFly automatically
- Start it
- Deploy your application
- Make it available at http://localhost:8080/library-management-1.0.0/

## Default WildFly Ports

- **HTTP**: 8080 (your application)
- **Management Console**: 9990 (http://localhost:9990)
- **Management Username**: admin
- **Management Password**: admin (change this in production!)

## Accessing the Application

Once deployed, access:
- **Home**: http://localhost:8080/library-management-1.0.0/home.xhtml
- **Add Book**: http://localhost:8080/library-management-1.0.0/addBook.xhtml
- **Issue Book**: http://localhost:8080/library-management-1.0.0/issueBook.xhtml
- **Return Book**: http://localhost:8080/library-management-1.0.0/returnBook.xhtml
- **Reports**: http://localhost:8080/library-management-1.0.0/reports.xhtml

## Troubleshooting

### Port 8080 already in use
- Stop other servers (Tomcat, etc.)
- Or change WildFly port in `standalone/configuration/standalone.xml`

### Application not deploying
- Check `standalone/log/server.log` for errors
- Ensure WAR file is in `standalone/deployments/` folder
- Check that file is named correctly (should end with `.war`)

### JSF/CDI not working
- WildFly has built-in support, should work automatically
- Check that `beans.xml` is in `WEB-INF/` folder (it is!)

## Stopping WildFly

Press `Ctrl+C` in the terminal, or:
```bash
jboss-cli.bat --connect command=:shutdown
```

