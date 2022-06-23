## Coding Task - Mailbox at Scale with Scala ##

### RESTful API Service

Mailinator is a web service for checking email sent to public, temporary email addresses. There are many similar services, but Mailinator was one of the first.

Your task is to implement an API service that performs the same functions as Mailinator. It should expose the following HTTP endpoints.

- `POST /mailboxes`: Create a new, random email address.
- `POST /mailboxes/{email address}/messages`: Create a new message for a specific email address.
- `GET /mailboxes/{email address}/messages`: Retrieve an index of messages sent to an email address, including sender, subject, and id, in recency order. Support cursor-based pagination through the index.
- `GET /mailboxes/{email address}/messages/{message id}`: Retrieve a specific message by id.
- `DELETE /mailboxes/{email address}`: Delete a specific email address and any associated messages. ***(no implemented yet)***
- `DELETE /mailboxes/{email address}/messages/{message id}`: Delete a specific message by id. ***(no implemented yet)***

Whether email addresses need to be created before they can receive messages is up to you.

Additional requirements:

- The input and output formats should be well-structured JSON.
- Old messages must eventually be expired, so you'll need to implement an eviction or garbage collection strategy. ***(no implemented yet)***
- Make sure to support concurrent access, either through multi-threading or multi-processing.

Please use a statically typed language for your implementation, if you know one, and the application framework of your choice. You may use a database, but you don't have to. Document your code and include automated tests.

* Used Scala
* Used Akka-http and Actor model to implement the above requirements.
* Used Automated testing

### Extra Credit

The original Mailinator service ran as a single Java process on a single machine, storing all data on the heap.

- Implement a solution that stores all data entirely in-memory and in-process (do not use Redis or a similar memory store, and do not write to disk). Make sure to manage the size of the process heap to avoid OOM exceptions.
- Include an automated benchmark suite that measures the performance of your solution. Suggest some ways to improve performance. ***(no implemented yet)***
- Support receiving messages via SMTP. ***(no implemented yet)***

## Evaluation Criteria

You will be primarily evaluated on the following points:

- Functional correctness.
- Implementation clarity and extensibility.
- Performance-mindedness, if applicable.
- Thoroughness of tests, if applicable.


## How to start the server with SBT



### Launch the server directly from SBT (dev mode)

```bash
sbt run
```


### Building and Running the Application (prod):

Build the application:

```bash
sbt universal:stage
```

Run the application that was built:

```bash
target/universal/stage/bin/akka-http-mailinator
```

## test the service with curl on localhost

```bash
curl http://localhost:8080/[rest end point]
```


