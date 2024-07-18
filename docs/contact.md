# Contact API Spec

## Create Contact

Endpoint: `POST /api/contacts`

Request header: 
- `X-API-Token: TOKEN` (required)

Request body:
```json
{
    "firstName": "Ilham Muhammad",
    "lastName": "Sidiq",
    "email": "ilham@example.com",
    "phone": "081234567890"
}
```

Response Body (Success):
```json
{
    "data": {
      "id": "random-string",
      "firstName": "Ilham Muhammad",
      "lastName": "Sidiq",
      "email": "ilham@example.com",
      "phone": "081234567890"
    }
}
```

Response Body (Failed):
```json
{
    "error": "Email format invalid, phone format invalid, ...."
}
```


## Update Contact

Endpoint: `PUT /api/contacts/{idContact}`

Request header: 
- `X-API-Token: TOKEN` (required)

Request body:
```json
{
    "firstName": "Ilham Muhammad",
    "lastName": "Sidiq",
    "email": "ilham@example.com",
    "phone": "081234567890"
}
```

Response Body (Success):
```json
    {
      "data": {
        "id": "random-string",
        "firstName": "Ilham Muhammad",
        "lastName": "Sidiq",
        "email": "ilham@example.com",
        "phone": "081234567890"
      }
    } 
```

Response Body (Failed):
```json
{
    "error": "Email format invalid, phone format invalid, ...."
}
```

## Get Contact

Endpoint: `GET /api/contacts/{idContact}`

Request header: 
- `X-API-Token: TOKEN` (required)


Response Body (Success):
```json
{
  "data": {
    "id": "random-string",
    "firstName": "Ilham Muhammad",
    "lastName": "Sidiq",
    "email": "ilham@example.com",
    "phone": "081234567890"
  }
}
```

Response Body (Failed, 404):
```json
{
    "error": "Contact is not found"
}
```

## Search Contact

Endpoint: `GET /api/contacts`

Query Param:
- name: `string`, contact first name or last name, using `like` query, optional
- phone: `string`, contact phone, using `like` query, optional
- email: `string`, contact email, using `like` query, optional
- page: `int`, start from 0, default 0
- size: `int`, default 10

Request header: 
- `X-API-Token: TOKEN` (required)

Response Body (Success):
```json
{
  "paging": {
    "currentPage": 0,
    "totalPage": 10,
    "size": 10
  },
  "data": [
    {
      "id": "random-string",
      "firstName": "Ilham Muhammad",
      "lastName": "Sidiq",
      "email": "ilham@example.com",
      "phone": "081234567890"
    },
    {
      "id": "random-string",
      "firstName": "Ilham Muhammad",
      "lastName": "Sidiq",
      "email": "ilham@example.com",
      "phone": "081234567890"
    }
  ]
}
```

Response Body (Failed):
```json
{
  "data": "Unauthorized"
}
```

## Remove Contact

Endpoint: `DELETE /api/contacts/{idContact}`

Request header: 
- `X-API-Token: TOKEN` (required)

Response Body (Success):
```json
{
  "data": "OK"
}
```

Response Body (Failed):
```json
{
  "data": "Contact is not found"
}
```