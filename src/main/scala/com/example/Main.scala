package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {
  // Create ActorSystem with root behavior
  val rootBehavior = Behaviors.setup[Nothing] { context =>
    // Create UserRegistry actor
    val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
    context.watch(userRegistryActor)

    // Create UserRoutes instance
    val userRoutes = new UserRoutes(userRegistryActor)(context.system)
    val routes = userRoutes.userRoutes

    implicit val system = context.system
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Starting the server
    val host = "0.0.0.0"
    val port = sys.env.getOrElse("PORT", "9000").toInt
    
    val bindingFuture = Http().newServerAt(host, port).bind(routes)

    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        context.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        context.log.error(s"Failed to bind HTTP server: ${ex.getMessage}")
        context.system.terminate()
    }

    // Add shutdown hook
    sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
      println("Shutting down server...")
    }

    Behaviors.empty
  }

  // Start the ActorSystem
  val system = ActorSystem[Nothing](rootBehavior, "scala-backend")
}
