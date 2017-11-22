package io.scalac.slack.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import io.scalac.slack.Config
import spray.json._

import scala.concurrent.Future

/**
 * Created on 29.01.15 22:43
 */
object SlackApiClient extends ApiClient{

  val log = Logging

  implicit val system = ActorSystem("SlackApiClient")
  implicit val dispatcher = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def get[T <: ResponseObject](endpoint: String, params: Map[String, String] = Map.empty[String, String])(implicit reader: JsonReader[T]): Future[T] = request(HttpMethods.GET, endpoint, params)
  def post[T <: ResponseObject](endpoint: String, params: Map[String, String] = Map.empty[String, String])(implicit reader: JsonReader[T]): Future[T] = request(HttpMethods.POST, endpoint, params)

  def request[T <: ResponseObject](method: HttpMethod, endpoint: String, params: Map[String, String] = Map.empty[String,String])(implicit reader: JsonReader[T]): Future[T] = {

    val uri = Uri(apiUrl(endpoint)).withQuery(Uri.Query(params))

    val futureResponse = Http().singleRequest(HttpRequest(
      method = method,
      uri = uri)
    ).flatMap( response => {
      import scala.concurrent.duration._
      val strictEntity: Future[HttpEntity.Strict] = response.entity.toStrict(3.seconds)

      strictEntity.flatMap { e =>
        e.dataBytes
          .runFold(ByteString.empty) { case (acc, b) => acc ++ b}
          .map(_.toString())
      }
    })

    (for {
      responseJson <- futureResponse
      response = JsonParser(responseJson).convertTo[T]
    } yield response) recover {
      case cause => throw new Exception("Something went wrong", cause)
    }

  }

  def apiUrl(endpoint: String) = Config.baseUrl(endpoint)
}
