# Akka HTTP Hello World

This is a sample [Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-M2/scala/http/) application packaged up in a [Docker](https://www.docker.com) container. You can use this project as a starting point for your Akka HTTP projects. The project includes the necessary configuration files for dockerizing the app.

## Building and Running the Application

Build the Akka application:

```bash
$ sbt assembly
```

Launch in a Docker container. You need to have Docker and Docker Compose installed.

```bash
$ docker-compose up
```

Launch image

```bash
docker images
docker run -it -p 8080:8080 akkahttphelloworld_webservice
```

Send a test request, where xxx.xxx.xxx.xxx is the IP address of the Docker container:

```bash
$ curl http://xxx.xxx.xxx.xxx
Hello, World!
```

## Useful Links

* [Akka HTTP - The What, Why and How](https://www.youtube.com/watch?v=y_slPbktLr0) (Video)
* [Akka HTTP documentation](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC4/scala/http/)
* [Akka HTTP Microservice Example](https://www.typesafe.com/activator/template/akka-http-microservice) (Tutorial with code)
* [Docker documentation](https://docs.docker.com/)
