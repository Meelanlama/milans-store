## E-R Diagram for the application
> **Note:** This E-R diagram shows the current structure of the application. It may change in the future accordingly to new features that will be added.

![ER Diagram](./database/ERD%20Crows_Foot%20Notation.jpg)

### Current Features:
So far, the application includes the following features:
- **User Authentication**: Registration, email verification for new-user, login, password reset, and JWT-based authentication.
- **Product and Category Management**: CRUD operations for products and categories, image uploads for products and categories.
- **File Uploading**: Upload product, profiles and category images.
- **Cart Functionality**: Adding/removing products to/from the cart, viewing the user's cart, and clearing the cart.
- **Order Management**: Creating, viewing, updating, and canceling orders, viewing detailed order information.
- **Refund Management**: Requesting, approving, and rejecting refund requests based on order status.
- **Product Reviews and Ratings**: Adding, editing, deleting reviews, and calculating average ratings for products.
- **Admin Capabilities**: Managing users, managing products, orders, and categories, with the ability to perform actions like approving/rejecting refunds, managing product status, etc.
- **Advanced Filtering and Search**: Filtering products and orders by various criteria (date, status, etc.), searching products by keyword, and searching orders by identifier or user email.
- **Email Notifications**: Sending order confirmation emails, update of order status notifications, refund status notifications, and account verification emails.
- **Pagination**: Implemented pagination for large sets of orders, products, and reviews for both users and admins.
- **Role-based Access Control (RBAC)**: Ensuring different access levels (admin, user) for API operations and features.
- **Order Invoice Generation**: Right now, Generating downloadable PDF invoices for completed orders. And, Will send the invoice in users email after ordering product.
- **CSV/Excel Export**: Download order data to CSV or Excel format of 30 days for reporting purposes by admin.
- **Swagger UI**: Integrated Swagger UI for API documentation.

### Plan to add these features:
- **Payment Gateway Integration**: Integrate Online payment gateways for more payment flexibility.
- **Promotions and Flash Sales**: Run time-limited promotions and flash sales to the products.
- **Enhanced Security**: Add extra security features like two-factor authentication.
- **User Wishlist**: Let users save products to a wishlist for future purchase.
- **Product Recommendations**: Suggest products to users based on purchase history.
- **Seller**: Sellers can register, add products, and manage their own products. Users can view products added by specific sellers and visit their seller pages.

## Setup

**1. Clone the application**

`git clone https://github.com/your-username/milans-store.git`

---

**2. Create MySQL database (if not using Docker)**

`CREATE DATABASE milan_store;`

> _Skip this step if you're running the app via Docker. The database will be auto-created._

**Change MySQL username and password as per your credentials**

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

- Swagger Api Docs: [http://localhost:8080/milan-docs](http://localhost:8080/milan-docs)

To stop the containers:

`docker-compose down`

---

**5. Run the app using Maven (alternative)**

If you prefer to run locally without Docker:

- Update `application.yml` (host: `localhost`, port: `3306`)

- Then run:

`./mvnw spring-boot:run`

---

## Explore Rest APIs

**Test these APIs using POSTMAN.**

- [Postman Collections](./docs/Milan's%20Store.postman_collection.json) – API endpoints for testing with Postman.
- Download the Postman Collection file from the [link](./docs/Milan's%20Store.postman_collection.json) and import it into Postman.

###  Authentication

| Method | URL                              | Description                                      |
| ------ | -------------------------------- |--------------------------------------------------|
| POST   | `auth/register`        | Register a new user                              |
| GET    | `auth/verify-register` | Verify user email using token sent via email     |
| POST   | `auth/login`           | Log in and receive access + refresh tokens       |
| POST   | `auth/refresh`         | Refresh access token using a valid refresh token |
| POST   | `auth/forget-password` | Send password reset link to registered email     |
| GET    | `auth/reset-password`  | Validate password reset token first              |
| POST   | `auth/reset-password`  | Reset a new password using a valid reset token   |

### Category Management(Admin Only)

|Method|URL|Description|
|---|---|---|
|POST|`/category/create`|Create a new category|
|PUT|`/category/update/{categoryId}`|Update an existing category|
|DELETE|`/category/delete/{categoryId}`|Delete a category by ID|
|GET|`/category/{categoryId}`|Get category details by ID|
|GET|`/category/getAllCategories`|Get all categories|
|GET|`/category/{categoryId}/productsByCategory`|Get products associated with a category|
|POST|`/category/upload-image/{categoryId}`|Upload image for category|
|GET|`/category/image/{categoryId}`|Get category image|

### Product Management

| Method | URL                                              | Description                                      |
|--------|--------------------------------------------------|--------------------------------------------------|
| DELETE | `/products/delete/{productId}`                   | Delete a product (Admin Only)                    |
| GET    | `/products/{productId}`                          | Get product by ID                                |
| GET    | `/products/search`                               | Search products by keyword                       |
| GET    | `/products/image/{productId}`                    | Get product image                                |
| GET    | `/products/getInactiveProducts`                  | Get all inactive products (paginated) (Admin Only) |
| GET    | `/products/getActiveProducts`                    | Get all active products (paginated)              |
| POST   | `/products/upload-image/{productId}`             | Upload product image (Admin Only)                |
| POST   | `/products/create`                               | Create a new product (Admin Only)                |
| PUT    | `/products/update/{productId}`                   | Update product details (Admin Only)              |


###  User Management(For logged-in)

|Method|URL| Description                                  |
|---|---|----------------------------------------------|
|DELETE|`/users/delete-profile-image`| Delete user profile image|
|DELETE|`/users/account-delete`| Delete user account                          |
|GET|`/users/search`| Search users (Admin only)                    |
|GET|`/users/profile-image`| Get profile image of logged-in user          |
|GET|`/users/my-profile`| Get current user profile                     |
|GET|`/users/all-users`| Get all users (Admin only)                   |
|POST|`/users/upload-profile-image`| Upload a new profile image                   |
|POST|`/users/change-password`| Change current user's password               |
|PUT|`/users/profile-update`| Update user profile details                  |

### Cart Management (For logged-in User Only)

|Method|URL|Description|
|---|---|---|
|POST|`/carts/add`|Add item to cart|
|GET|`/carts/myCart`|View current user's cart|
|DELETE|`/carts/remove/{cartItemId}`|Remove item from cart|
|DELETE|`/carts/clear`|Clear all items in cart|

### Order Management

|Method|URL| Description                                           |
|---|---|-------------------------------------------------------|
|POST|`/orders/create`| Create a new order (User only)                        |
|POST|`/orders/cancel-order/{orderIdentifier}`| Cancel an order                                       |
|PUT|`/orders/{orderId}/status`| Update order status    (Admin only)                               |
|GET|`/orders/my-orders`| Get current user's orders                             |
|GET|`/orders/all`| Get all orders  (Admin only)                                      |
|GET|`/orders/search`| Search order by identifier (Admin only)                           |
|GET|`/orders/filter`| Filter orders by criteria   (Admin only)                          |
|GET|`/orders/order-statuses`| Get all possible order statuses                       |
|GET|`/orders/export-excel`| Export orders from last 30 days to Excel (Admin only) |
|GET|`/orders/download-invoice/{orderIdentifier}`| Download invoice PDF for an order (Admin only)                    |

### Refund Management

|Method|URL| Description                                          |
|---|---|------------------------------------------------------|
|POST|`/refunds/request-refund/{orderIdentifier}`| Request refund for an order that is delivered (User) |
|POST|`/refunds/approve-refund/{refundId}`| Approve a refund request   (Admin only)              |
|POST|`/refunds/reject-refund/{refundId}`| Reject a refund request    (Admin only)              |
|GET|`/refunds/my-refunds`| Get user's refund requests (Admin only)              |
|GET|`/refunds/all-refunds`| Get all refund requests   (Admin only)               |

### Review Management (For logged-in user)

|Method|URL| Description                                    |
|---|---|------------------------------------------------|
|POST|`/reviews/create/{productId}`| Submit a review for the delivered products only|
|PUT|`/reviews/edit/{reviewId}`| Update a review                                |
|DELETE|`/reviews/{reviewId}`| Delete a review                                |
|GET|`/reviews/product/{productId}`| Get product reviews                            |
|GET|`/reviews/my-reviews`| Get user's reviews                             |
|GET|`/reviews/average-rating/{productId}`| Get average rating for a product               |
|GET|`/reviews/average-rating-formatted/{productId}`| Get formatted average rating for a product     |

