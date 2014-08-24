
[![Build Status](https://travis-ci.org/kpmeen/clammyscan.svg?branch=master)](https://travis-ci.org/kpmeen/clammyscan)
# ClammyScan

There isn't really all that much to it. The Play Reactive Mongo plugin, which this library depends on, comes with a gridfsBodyParser that allows streaming file uploads directly into MongoDB. ClammyScan implements its own BodyParser, that will both scan the file stream with clamd (over TCP using INSTREAM) and save it to MongoDB. If the file contains a virus or is otherwise infected, it is removed from GridFS...and returns an HTTP NotAcceptable. If the file is OK, the Controller will have the file part available for further processing in the request.

## Installation

Add the following repository to your build.sbt file:

```scala
resolvers += "JCenter" at "http://jcenter.bintray.com/"
```
And the dependency for ClammyScan:

```scala
libraryDependencies += "net.scalytica" %% "clammyscan" % "0.3"
```

## Configuration

ClammyScan has some configurable parameters. At the moment the configurable parameters are as follows, and should be located in the application.conf file:

```hocon
# ClammyScan configuration
# ~~~~~
clammyscan {
  clamd {
    host="clamserver"
    port="3310"
    timeout="0" # Timeout is in milliseconds, where 0 means infinite. (See clamd documentation)
  }
}
```
The properties should be fairly self-explanatory.

## Usage

Currently the body parser *requires* the presence of a *filename* as an argument in the Controller (this will change soon). This means a minimal controller would look something like this:

```scala
object Application extends Controller with MongoController with ClammyBodyParser {
  
  def gfs = GridFS(db)
  
  def upload(filename: String) = Action.async(clammyBodyParser(gfs, filename)) { implicit request =>
    futureMultipartFile.map(file => {
      logger.info(s"Saved file with name ${file.filename}")
      Ok
    }).recover {
      case e: Throwable => InternalServerError(Json.obj("message" -> e.getMessage))
    }
  }
}
```
It is also possible to, optionally, specify any additional metadata to use in GridFS for the saved file. For example, if you have a few request parameters that need to be set, this can be done by passing them to the body parser.

```scala
object Application extends Controller with MongoController with ClammyBodyParser {

  def gfs = GridFS(db)
  
  def upload(param1: String, param2: String, filename: String) = Action.async(clammyBodyParser(gfs, filename, Map[String, String]("param1" -> param1, "param2" -> param2))) { implicit request =>
    futureMultipartFile.map(file => {
      logger.info(s"Saved file with name ${file.filename}")
      Ok
    }).recover {
      case e: Throwable => InternalServerError(Json.obj("message" -> e.getMessage))
    }
  }
}
```

# Building and Testing

Currently the tests depend on the precense of a clamd instance running on a host called "clamserver" on port 3310. This is a bit...inconvenient...and will be addressed at some point. A good option for running the tests is to have a vagrant box with clamd running on it (with hostname = "clamserver").
