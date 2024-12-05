package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import scala.concurrent.Future
import com.example.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) extends CorsHandler {
  import JsonFormats._
  
  private implicit val timeout: Timeout = 
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers.apply)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

  val userRoutes: Route = corsHandler {
    concat(
      // Root path
      pathEndOrSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, 
            s"""
            |<!DOCTYPE html>
            |<html>
            |<head>
            |    <title>Scala Backend API</title>
            |    <style>
            |        body { font-family: Arial, sans-serif; margin: 40px; }
            |        .endpoint { background: #f4f4f4; padding: 10px; margin: 10px 0; border-radius: 4px; }
            |    </style>
            |</head>
            |<body>
            |    <h1>Scala Backend API</h1>
            |    <p>Server is up and running! Available endpoints:</p>
            |    <div class="endpoint">GET /health - Health check</div>
            |    <div class="endpoint">GET /users - List all users</div>
            |    <div class="endpoint">POST /users - Create a user</div>
            |    <div class="endpoint">GET /users/{name} - Get user by name</div>
            |    <div class="endpoint">DELETE /users/{name} - Delete user</div>
            |    <p>Server Time: ${java.time.Instant.now}</p>
            |</body>
            |</html>
            """.stripMargin))
        }
      },
      // Health check
      path("health") {
        get {
          complete(HealthResponse("UP", java.time.Instant.now.toString))
        }
      },
      // In your UserRoutes.scala or a new MessageRoutes.scala
      path("messages") {
        post {
          entity(as[Message]) { message =>
            complete {
              // Handle the message (e.g., save to database, send email)
              StatusCodes.OK -> Map("success" -> true, "message" -> "Message sent successfully")
            }
          }      
        }      
      },
      // Users routes
      pathPrefix("users") {
        concat(
          pathEnd {
            concat(
              get {
                complete(getUsers())
              },
              post {
                entity(as[User]) { user =>
                  onSuccess(createUser(user)) { performed =>
                    complete((StatusCodes.Created, performed))
                  }
                }
              })
          },
          path(Segment) { name =>
            concat(
              get {
                rejectEmptyResponse {
                  onSuccess(getUser(name)) { response =>
                    complete(response.maybeUser)
                  }
                }
              },
              delete {
                onSuccess(deleteUser(name)) { performed =>
                  complete((StatusCodes.OK, performed))
                }
              })
          })
      })
  }
}
