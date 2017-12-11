# Akka HTTP Hello World

This is a sample [Akka HTTP](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-M2/scala/http/) application packaged up in a [Docker](https://www.docker.com) container. You can use this project as a starting point for your Akka HTTP projects. The project includes the necessary configuration files for dockerizing the app.

## SBT mode

### Building and Running the Application:

Build the application:

```bash
sbt universal:stage
```

Run the application tha was built:

```bash
target/universal/stage/bin/akka-http-hello-world
```

### Launch the server directly from SBT

```bash
sbt run
```

## Maven mode

### Launch the server directly from Maven

```bash
mvn -s [path_to_settings.xml] scala run
```

### Create and run Uber jar server artifact

```bash
mvn clean package
java -jar target/uber-akka-http-hello-world-1.0.2-SNAPSHOT.jar
```

## test the service with curl on localhost

```bash
curl http://localhost:8080/compute
```

## Docker Stuff

Launch in a Docker container. You need to have Docker and Docker Compose installed.

```bash
$ docker-compose up
```

Launch image

```bash
docker images
docker run -it -p 8080:8080 akkahttphelloworld_webservice
```


## OpenShift

### Log in into OpenShift

```bash
oc login -u [login] [openshift url]
```

### Deploy on OpenShift

```bash
git commit -m "changed version"
git push origin master
oc start-build service-layer-template --follow
```

### test Streaming with curl

curl -O [url service]/streaming

## Useful Links

* [Akka HTTP - The What, Why and How](https://www.youtube.com/watch?v=y_slPbktLr0) (Video)
* [Akka HTTP documentation](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-RC4/scala/http/)
* [Akka HTTP Microservice Example](https://www.typesafe.com/activator/template/akka-http-microservice) (Tutorial with code)
* [Docker documentation](https://docs.docker.com/)
