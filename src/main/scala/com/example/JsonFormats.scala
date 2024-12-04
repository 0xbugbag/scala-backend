package com.example

import com.example.UserRegistry.ActionPerformed
import spray.json.DefaultJsonProtocol

object JsonFormats {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User.apply)
  implicit val usersJsonFormat = jsonFormat1(Users.apply)
  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed.apply)
  implicit val healthResponseFormat = jsonFormat2(HealthResponse.apply)
}
