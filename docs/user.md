# User Api Spec

## Register User
endpoint: `POST /api/users`

Request body:
```json
{
    "name": "Ilham Muhammad Sidiq",
    "username": "ilhaam.ms",
    "password": "12345"
}
```

Response body (Success):
```json
{
    "data": "OK"
}
```

Response body (Failed):
```json
{
    "error": "Username must not blank, ???"
}
```

## Login User
endpoint: `POST /api/auth/login`

Request body:
```json
{
    "username": "ilhaam.ms",
    "password": "12345"
}
```

Response body (Success):
```json
{
    "data": {
      "token": "TOKEN",
      "expiredAt": 2222222 // milisecond
    }
}
```

Response body (Failed, 401):
```json
{
    "error": "Username or password wrong"
}
```

## Get User
endpoint: `GET /api/users/current`

Request header: `X-API-Token: TOKEN` (required)

Response body (Success):
```json
{
    "data": {
      "name": "Ilham Muhammad Sidiq",
      "username": "ilhaam.ms"
    }
}
```

Response body (Failed, 401):
```json
{
    "error": "Unauthorized"
}
```


## Update User

endpoint: `PATCH /api/users/current`

Request header: `X-API-Token: TOKEN` (required)

Request body:
```json
{
    "name": "Ilham Sidiq", // put if only want to update username
    "password": "new password" // put if only want to update password
}
```

Response body (Success):
```json
{
    "data": {
      "username": "ilhaam.ms",
      "name": "Ilham Muhammad Sidiq"
    }
}
```

Response body (Failed, 401):
```json
{
    "error": "Unauthorized"
}
```


## Logout User
endpoint: `DELETE /api/auth/logout`

Request header: `X-API-Token: TOKEN` (required)

Response body (Success):
```json
{
    "data": "OK"
}
```