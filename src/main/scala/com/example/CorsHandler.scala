package com.example

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

trait CorsHandler {
  // List of allowed origins - replace with your frontend URL
  private val allowedOrigins = List(
    "https://scala-backend.onrender.com",
    "http://localhost:3000"  // for local development
  )

  private def corsResponseHeaders(origin: String) = List(
    `Access-Control-Allow-Origin`(HttpOrigin(origin)),
    `Access-Control-Allow-Credentials`(true),
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With"),
    `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)
  )

  def corsHandler(r: Route): Route = extractRequest { request =>
    val origin = request.header[Origin].map(_.origins.head.toString)
      .filter(allowedOrigins.contains)
      .getOrElse(allowedOrigins.head)

    addAccessControlHeaders(origin) {
      preflightRequestHandler(origin) ~ r
    }
  }

  private def addAccessControlHeaders(origin: String): Directive0 = {
    respondWithHeaders(corsResponseHeaders(origin))
  }

  private def preflightRequestHandler(origin: String): Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(corsResponseHeaders(origin)))
  }
}
