### SQL Sample
Access Local DB from:
`http://localhost:8080/h2-console`

Run following cURL to see the changes in DB:

Get ALL users:

`curl -X GET http://localhost:8080/v1/users`

GET User By ID:

`curl -X GET http://localhost:8080/v1/users/1`


CREATE a new User:

`curl -X POST http://localhost:8080/v1/users \
-H "Content-Type: application/json" \
-d '{
"name": "New User",
"email": "new.user@example.com"
}'`