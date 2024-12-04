package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  private def startHttpServer(routes: UserRoutes)(implicit system: ActorSystem[_]): Unit = {
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val host = "0.0.0.0"
    val port = sys.env.getOrElse("PORT", "9000").toInt
    
    system.log.info(s"Starting server on $host:$port...")
    
    val futureBinding = Http().newServerAt(host, port).bind(routes.userRoutes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("""
          |
          |=================================
          |Server successfully started!
          |Listening on: http://{}:{}
          |Available endpoints:
          | - GET  /
          | - GET  /health
          | - GET  /users
          | - POST /users
          |=================================
          |""".stripMargin, address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      val routes = new UserRoutes(userRegistryActor)(context.system)
      startHttpServer(routes)(context.system)

      Behaviors.empty
    }

    ActorSystem[Nothing](rootBehavior, "scala-backend")
  }
}
