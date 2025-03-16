# QuickCart Order Service

## Overview
QuickCart Order Service is a Spring Boot application that manages orders, products, and payments. It provides RESTful APIs for creating, updating, and retrieving orders, as well as handling payments and refunds.

## Technologies Used
- Java
- Spring Boot
- Maven
- JUnit 5
- Mockito

## Prerequisites
- Java 11 or higher
- Maven 3.6.0 or higher

## Setup and Installation
1. Clone the repository:
   ```sh
   git clone https://github.com/MayankShivhare999/quickcart-order-service.git

## API Endpoints

### Order Controller
- `POST /orders` - Create a new order
- `GET /orders` - Get all orders placed by the authenticated user
- `PUT /orders/{id}` - Update an existing order
- `DELETE /orders/{id}` - Cancel an order

### Product Service
- `POST /products/ids` - Get products by their IDs

### Payment Service
- `POST /payments` - Create a new payment
- `POST /payments/refund` - Refund a payment