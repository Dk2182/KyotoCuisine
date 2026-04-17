# Kyoto Cuisine - No Tomcat Version

This version runs without Tomcat or Servlets. It uses Java's built-in `HttpServer`.

## What you need
- Java 17+ installed
- MySQL running
- MySQL Connector/J jar placed in `lib/`

Example:
- `lib/mysql-connector-j-9.3.0.jar`

## Database setup
1. Create the database by running `sql/schema.sql`
2. Insert demo data by running `sql/seed.sql`
3. Update `src/DBConnection.java` if your DB name, username, or password are different

Default values in `DBConnection.java`:
- database: `kyoto_db`
- username: `root`
- password: `password`

## Run in VS Code
1. Open this folder in VS Code
2. Make sure the MySQL Connector/J jar is inside `lib/`
3. Restart VS Code if the jar was just added
4. Open `src/MainServer.java`
5. Run `MainServer`

## Open the app
Go to:

`http://localhost:8080/login.html`

## Demo accounts
- `customer@kyoto.com` / `123`
- `staff@kyoto.com` / `123`
- `admin@kyoto.com` / `123`

## Notes
- This is a simplified no-Tomcat version meant to make local testing easier
- It uses HTML forms and redirects
- It does not implement full session management
- Login uses the database if available, and falls back to hardcoded demo accounts if DB login fails
