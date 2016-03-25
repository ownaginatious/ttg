## Steps:
1. Run npm install
2. Run mongod
3. Start app: node app.js

## michuservice API
#### GET /michuservice/:id
Retrieves the JSON data stored for the given id
###### Example Request:
> GET /michuservice/qw4z30u

##### Responses:
200 OK
```json
{
  data: <json_object>
}
```
404 PAGE NOT FOUND
```json
{
  error: <string>
}
```

500 INTERNAL SERVER ERROR
```json
{
  error: <string>
}
```
___
#### POST /michuservice
Stores provided JSON data and generates a random id which is returned to the client
###### Example request:
> POST /michuservice
> 
> entity body:
 ```json
 {
   classes: [
     {
       name: 'Japanese 101',
       number: 26101,
       schedule: { ... }
     },
     {
       name: 'Physics I for Engineers',
       number: 36101,
       schedule: { ... }
     },
     ...
   ]
 }
 ```

##### Responses:

200 OK
```json
{
  id: <string>
}
```
400 BAD REQUEST
```json
{
  error: <string>
}
```

500 INTERNAL SERVER ERROR
```json
{
  error: <string>
}
```
###### Note: The service does not allow storing duplicate JSON documents. In the event that a duplicate store is attempted, the service will just update the last_access_dt field of the currently stored JSON document and return its id.
___
#### DELETE /michuservice?date=:date
Removes all JSON documents that were last accessed before the given date
######Example requests:
> DELETE /michuservice?date=2016-03-23
> 
 DELETE /michuservice?date=2016-03-23T03:06:23

##### Responses:

200 OK
```json
{
  deleted_count: <integer>
}
```
400 BAD REQUEST
```json
{
  error: <string>
}
```

500 INTERNAL SERVER ERROR
```json
{
  error: <string>
}
```