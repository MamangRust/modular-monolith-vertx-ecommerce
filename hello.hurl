# ============================================================
# E2E API Gateway Tests — vertx-ecommerce (port 5000)
# Flow: register -> login -> category -> merchant -> product CRUD
# ============================================================

# ------------------------------------------------------------
# 1. Register a new user (auto-assigned ROLE_ADMIN)
# ------------------------------------------------------------
POST http://localhost:5000/register
Content-Type: application/json

{
  "firstname": "Hurl",
  "lastname": "Tester",
  "email": "{{user_email}}",
  "password": "Dragon123!"
}

HTTP 201
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.id" exists
jsonpath "$.data.email" == "{{user_email}}"

[Captures]
user_id: jsonpath "$.data.id"

# ------------------------------------------------------------
# 2. Login to get JWT access token
# ------------------------------------------------------------
POST http://localhost:5000/login
Content-Type: application/json

{
  "email": "{{user_email}}",
  "password": "Dragon123!"
}

HTTP 200
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.access_token" exists

[Captures]
access_token: jsonpath "$.data.access_token"

# ------------------------------------------------------------
# 3. Create a category (form data, ADMIN)
# ------------------------------------------------------------
POST http://localhost:5000/categories/manage
Authorization: Bearer {{access_token}}
Content-Type: application/x-www-form-urlencoded
[FormParams]
name: Hurl Category {{category_slug}}
description: Category created by hurl
slugCategory: {{category_slug}}

HTTP 201
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.id" exists

[Captures]
category_id: jsonpath "$.data.id"

# ------------------------------------------------------------
# 4. Create a merchant (JSON, JWT)
# ------------------------------------------------------------
POST http://localhost:5000/merchants/manage
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "user_id": {{user_id}},
  "name": "Hurl Merchant",
  "description": "Merchant created by hurl",
  "address": "Jl. Testing No. 1",
  "contact_email": "merchant@example.com",
  "contact_phone": "081234567890"
}

HTTP 201
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.id" exists

[Captures]
merchant_id: jsonpath "$.data.id"

# ------------------------------------------------------------
# 5. Create a product (form data, JWT)
# ------------------------------------------------------------
POST http://localhost:5000/products/manage
Authorization: Bearer {{access_token}}
Content-Type: application/x-www-form-urlencoded
[FormParams]
merchant_id: {{merchant_id}}
category_id: {{category_id}}
name: Produk Test
description: Produk untuk testing hurl
price: 150000
count_in_stock: 50
brand: TestBrand
weight: 1000
rating: 5
slug_product: {{product_slug}}
barcode: {{product_barcode}}

HTTP 201
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.id" exists
jsonpath "$.data.name" == "Produk Test"

[Captures]
product_id: jsonpath "$.data.id"

# ------------------------------------------------------------
# 6. GET all products (public)
# ------------------------------------------------------------
GET http://localhost:5000/products

HTTP 200
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data" exists

# ------------------------------------------------------------
# 7. GET product by id (public)
# ------------------------------------------------------------
GET http://localhost:5000/products/{{product_id}}

HTTP 200
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.id" == {{product_id}}
jsonpath "$.data.name" == "Produk Test"

# ------------------------------------------------------------
# 8. Update product (form data, JWT)
# ------------------------------------------------------------
POST http://localhost:5000/products/manage/{{product_id}}
Authorization: Bearer {{access_token}}
Content-Type: application/x-www-form-urlencoded
[FormParams]
merchant_id: {{merchant_id}}
category_id: {{category_id}}
name: Produk Test Updated
description: Update deskripsi produk
price: 175000
count_in_stock: 60
brand: TestBrand
weight: 1200
rating: 4
slug_product: {{product_slug}}-updated
barcode: {{product_barcode}}

HTTP 200
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.name" == "Produk Test Updated"

# ------------------------------------------------------------
# 9. Delete product permanently (JWT)
# ------------------------------------------------------------
DELETE http://localhost:5000/products/manage/deletePermanent/{{product_id}}
Authorization: Bearer {{access_token}}

HTTP 200
[Asserts]
jsonpath "$.status" == "success"
