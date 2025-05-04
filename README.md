## E-R Diagram for the application

![ER Diagram](./database/ERD%20Crows_Foot%20Notation.jpg)

## Steps to Setup

**1. Clone the application**

`git clone https://github.com/your-username/milans-store.git`

---

**2. Create MySQL database (if not using Docker)**

`CREATE DATABASE milan_store;`

> _Skip this step if you're running the app via Docker. The database will be auto-created._

**Change MySQL username and password as per your installation**

+ Open the `application.yml` file located at `src/main/resources/application.yml`.

+ Update the `spring.datasource.username` and `password` properties with your MySQL credentials.

---

**3. Create `.env` file**

In the project root directory, create a `.env` file and add your email credentials:

`EMAIL_USERNAME=your-email@gmail.com EMAIL_PASSWORD=your-app-password`

> _This is used for email notifications (e.g. order confirmations and its status, verify account when register, reset password)._

---

**4. Run the app using Docker (recommended)**

`docker-compose up --build`

- App will be accessible at: [http://localhost:8080](http://localhost:8080)

- Swagger Docs: [http://localhost:8080/milan-docs](http://localhost:8080/milan-docs)

To stop the containers:

`docker-compose down`

---

**5. Run the app using Maven (alternative)**

If you prefer to run locally without Docker:

- Update `application.yml` to point to your local DB (host: `localhost`, port: `3306`)

- Then run:

`./mvnw spring-boot:run`

## Explore Rest APIs

###  Authentication APIs

| Method | URL                              | Description                                      |
| ------ | -------------------------------- |--------------------------------------------------|
| POST   | `/store/v1/auth/register`        | Register a new user                              |
| GET    | `/store/v1/auth/verify-register` | Verify user email using token sent via email     |
| POST   | `/store/v1/auth/login`           | Log in and receive access + refresh tokens       |
| POST   | `/store/v1/auth/refresh`         | Refresh access token using a valid refresh token |
| POST   | `/store/v1/auth/forget-password` | Send password reset link to registered email     |
| GET    | `/store/v1/auth/reset-password`  | Validate password reset token first              |
| POST   | `/store/v1/auth/reset-password`  | Reset a new password using a valid reset token   |

### Category Management APIs

|Method|URL|Description|
|---|---|---|
|POST|`/store/v1/category/create`|Create a new category|
|PUT|`/store/v1/category/update/{categoryId}`|Update an existing category|
|DELETE|`/store/v1/category/delete/{categoryId}`|Delete a category by ID|
|GET|`/store/v1/category/{categoryId}`|Get category details by ID|
|GET|`/store/v1/category/getAllCategories`|Get all categories|
|GET|`/store/v1/category/{categoryId}/productsByCategory`|Get products associated with a category|
|POST|`/store/v1/category/upload-image/{categoryId}`|Upload image for category|
|GET|`/store/v1/category/image/{categoryId}`|Get category image|

### Product Management

| Method | URL                                                        | Description                            |
|--------|------------------------------------------------------------|----------------------------------------|
| DELETE | /store/v1/products/delete/{productId}                      | Delete a product                        |
| GET    | /store/v1/products/{productId}                             | Get product by ID                       |
| GET    | /store/v1/products/search                                  | Search products by keyword             |
| GET    | /store/v1/products/image/{productId}                       | Get product image                       |
| GET    | /store/v1/products/getInactiveProducts                     | Get all inactive products (paginated)  |
| GET    | /store/v1/products/getActiveProducts                       | Get all active products (paginated)    |
| POST   | /store/v1/products/upload-image/{productId}                | Upload product image                    |
| POST   | /store/v1/products/create                                  | Create a new product                    |
| PUT    | /store/v1/products/update/{productId}                      | Update product details                  |

###  User Management APIs

|Method|URL|Description|
|---|---|---|
|DELETE|`/store/v1/users/delete-profile-image`|Delete user profile image|
|DELETE|`/store/v1/users/account-delete`|Delete user account|
|GET|`/store/v1/users/search`|Search users (Admin only)|
|GET|`/store/v1/users/profile-image`|Get profile image of logged-in user|
|GET|`/store/v1/users/my-profile`|Get current user profile|
|GET|`/store/v1/users/all-users`|Get all users (Admin only)|
|POST|`/store/v1/users/upload-profile-image`|Upload a new profile image|
|POST|`/store/v1/users/change-password`|Change current user's password|
|PUT|`/store/v1/users/profile-update`|Update user profile details|

### Cart Management APIs

|Method|URL|Description|
|---|---|---|
|POST|`/store/v1/carts/add`|Add item to cart|
|GET|`/store/v1/carts/myCart`|View current user's cart|
|DELETE|`/store/v1/carts/remove/{cartItemId}`|Remove item from cart|
|DELETE|`/store/v1/carts/clear`|Clear all items in cart|

### Order Management APIs

|Method|URL|Description|
|---|---|---|
|POST|`/store/v1/orders/create`|Create a new order (User only)|
|POST|`/store/v1/orders/cancel-order/{orderIdentifier}`|Cancel an order|
|PUT|`/store/v1/orders/{orderId}/status`|Update order status|
|GET|`/store/v1/orders/my-orders`|Get current user's orders|
|GET|`/store/v1/orders/all`|Get all orders|
|GET|`/store/v1/orders/search`|Search order by identifier|
|GET|`/store/v1/orders/filter`|Filter orders by criteria|
|GET|`/store/v1/orders/order-statuses`|Get all possible order statuses|
|GET|`/store/v1/orders/export-excel`|Export orders from last 30 days to Excel|
|GET|`/store/v1/orders/download-invoice/{orderIdentifier}`|Download invoice PDF for an order|

### Refund Management APIs

|Method|URL|Description|
|---|---|---|
|POST|`/store/v1/refunds/request-refund/{orderIdentifier}`|Request refund for an order|
|POST|`/store/v1/refunds/approve-refund/{refundId}`|Approve a refund request|
|POST|`/store/v1/refunds/reject-refund/{refundId}`|Reject a refund request|
|GET|`/store/v1/refunds/my-refunds`|Get user's refund requests|
|GET|`/store/v1/refunds/all-refunds`|Get all refund requests|

### Review Management APIs

|Method|URL|Description|
|---|---|---|
|POST|`/store/v1/reviews/create/{productId}`|Submit a review for the delivered products only|
|PUT|`/store/v1/reviews/edit/{reviewId}`|Update a review|
|DELETE|`/store/v1/reviews/{reviewId}`|Delete a review|
|GET|`/store/v1/reviews/product/{productId}`|Get product reviews|
|GET|`/store/v1/reviews/my-reviews`|Get user's reviews|
|GET|`/store/v1/reviews/average-rating/{productId}`|Get average rating for a product|
|GET|`/store/v1/reviews/average-rating-formatted/{productId}`|Get formatted average rating for a product|