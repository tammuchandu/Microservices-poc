# Spring Boot Four Microservices

A Java 21 / Spring Boot 3.5.7 Maven multi-module project containing:

| Service | Port | Responsibility | Database |
|---|---:|---|---|
| User Service | 8081 | User CRUD | MySQL |
| Product Service | 8082 | Product CRUD | MySQL |
| Order Service | 8083 | Place, view and cancel orders; calls User and Product | MySQL |
| Aggregator Service | 8080 | Combines responses from other services | None |

## Requirements

- JDK 21
- Maven 3.9+
- MySQL Server
- Create the schema once:

```sql
CREATE DATABASE mydb;
```

The three business services currently use the same schema `mydb` for a simple local learning setup. For stronger database-per-service isolation, configure a separate schema for each service.

## Configure the database password

In each PowerShell terminal used to run a service, set:

```powershell
$env:DB_PASSWORD="your_mysql_password"
```

Optional overrides:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
```

Do not commit real database passwords to source control.

## Build all modules

From the project root:

```powershell
mvn clean package -DskipTests
```

## Run the services

Open four terminals in the project root. Set `DB_PASSWORD` in terminals 1, 2 and 3.

Terminal 1:

```powershell
$env:DB_PASSWORD="your_mysql_password"
mvn -pl user-service spring-boot:run
```

Terminal 2:

```powershell
$env:DB_PASSWORD="your_mysql_password"
mvn -pl product-service spring-boot:run
```

Terminal 3 (start after User and Product):

```powershell
$env:DB_PASSWORD="your_mysql_password"
mvn -pl order-service spring-boot:run
```

Terminal 4 (start after the other services):

```powershell
mvn -pl aggregator-service spring-boot:run
```

## Sample data

On startup, User Service inserts a demo user if its email is not already present:

```json
{
  "id": 1,
  "name": "Demo User",
  "email": "demo.user@example.com",
  "phone": "9876543210"
}
```

The actual generated user ID is assigned by MySQL. Product Service inserts product ID `2001` if it does not already exist:

```json
{
  "id": 2001,
  "name": "Keyboard",
  "description": "USB keyboard",
  "price": 1499.00,
  "stock": 25
}
```

## API endpoints

### User Service — `http://localhost:8081`

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

Create user body:

```json
{
  "name": "Chandu",
  "email": "chandu@example.com",
  "phone": "9876543210"
}
```

### Product Service — `http://localhost:8082`

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

Create product body:

```json
{
  "name": "Mouse",
  "description": "Wireless mouse",
  "price": 799.00,
  "stock": 20
}
```

### Order Service — `http://localhost:8083`

- `GET /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/user/{userId}`
- `POST /api/orders`
- `PUT /api/orders/{id}/cancel`

Place order body (use an existing user ID and product ID):

```json
{
  "userId": 1,
  "productId": 2001,
  "quantity": 2
}
```

### Aggregator Service — `http://localhost:8080`

- `GET /api/products`
- `GET /api/summary/{userId}`
- `GET /api/dashboard/{userId}`

Example:

```text
http://localhost:8080/api/dashboard/1
```

## Important behavior notes

- Order Service checks that the user and product exist and checks available product stock before saving an order.
- This learning version does **not** decrement product stock when an order is placed, and cancellation does not restore stock. Implement a dedicated stock-reservation API in Product Service before using this for real commerce.
- The services communicate over HTTP using Spring `RestClient`.
- No service shares JPA entities with another service; Order Service uses DTO records for remote User and Product responses.
