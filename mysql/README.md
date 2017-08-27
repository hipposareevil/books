# MySQL Database

This contains a preliminary SQL dump file that is used to bootstrap the database. It contains the *admin* user with initial password of *admin*. This can be updated via curl commands to */user/1*.

Process to update the *admin*'s password to 'super':

```
$> bearer=$( curl -X POST http://localhost:8080/authorize/token --header 'Content-Type: application/json' -d '{"name":"admin", "password":"admin"}')
$> curl -X PUT http://localhost:8080/user/1 -H "Authorization: ${bearer}" -H "Content-Type: application/json" -d '{ "id": 1, "name":"admin", "password":"super"}'

```
