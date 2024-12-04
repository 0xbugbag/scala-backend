package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Main {
  private def startHttpServer(routes: UserRoutes)(implicit system: ActorSystem[_]): Unit = {
    // Needed for the Future and other things
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val host = "0.0.0.0"
    val port = sys.env.getOrElse("PORT", "9000").toInt
    
    val futureBinding = Http().newServerAt(host, port).bind(routes.userRoutes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
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

    val system = ActorSystem[Nothing](rootBehavior, "scala-backend")

    // Add shutdown hook
    sys.addShutdownHook {
      system.terminate()
    }
  }
}
