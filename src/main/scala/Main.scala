package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {

  // Setting up ActorSystem and ExecutionContext
  implicit val system: ActorSystem = ActorSystem("scala-backend")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  // Define your routes
  val route: Route =
    pathPrefix("api") {
      path("hello") {
        get {
          complete(StatusCodes.OK, "Hello from Scala Backend!")
        }
      } ~
      path("user") {
        get {
          complete(StatusCodes.OK, """{"name": "Hanum", "profession": "Data Worker"}""")
        }
      }
    }

  // Starting the server
  val host = "0.0.0.0"
  val port = sys.env.getOrElse("PORT", "9000").toInt
  val bindingFuture = Http().newServerAt(host, port).bind(route)

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Server online at http://${address.getHostString}:${address.getPort}/\nPress CTRL+C to stop...")
    case Failure(ex) =>
      println(s"Failed to bind HTTP endpoint, terminating system. Error: ${ex.getMessage}")
      system.terminate()
  }

  // Add a shutdown hook to handle graceful termination
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
    println("Shutting down server...")
  }
}
