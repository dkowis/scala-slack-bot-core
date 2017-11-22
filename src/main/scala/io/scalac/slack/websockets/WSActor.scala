package io.scalac.slack.websockets

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.QueueOfferResult.{Dropped, Enqueued, Failure}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import io.scalac.slack._

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created on 28.01.15 19:45
  * TODO: this is probably a redundant actor, but I'm not sure how to do it a different way yet.
  */
class WSActor(eventBus: MessageEventBus) extends Actor {

  private val out = context.actorOf(Props(classOf[OutgoingMessageProcessor], self, eventBus))
  private val in = context.actorOf(Props(classOf[IncomingMessageProcessor], eventBus))

  implicit val system: ActorSystem = context.system
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case WebSocket.Connect(host, port, resource, ssl) =>

      val incoming: Sink[Message, Future[Done]] =
        Sink.foreach[Message] {
          case message: TextMessage =>
            in ! message.getStrictText
        }

      val overflowStrategy = akka.stream.OverflowStrategy.dropHead
      val outgoing = Source.queue[Message](100, overflowStrategy)

      //Create the new WebSocket flow
      val flow: Flow[Message, Message, SourceQueueWithComplete[Message]] =
        Flow.fromSinkAndSourceMat(
          incoming,
          outgoing)(Keep.right)

      val scheme = if (ssl) {
        "https"
      } else {
        "http"
      }
      val uri = Uri.from(
        host = host,
        port = port,
        scheme = scheme,
        path = resource
      )

      //Old headers, might need to specify these in the WebSocketRequest
      //HttpHeaders.RawHeader("Upgrade", "websocket"),
      //HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
      //HttpHeaders.RawHeader("Sec-WebSocket-Key", Config.websocketKey))
      val (upgradeResponse, outgoingQueue) =
      Http().singleWebSocketRequest[SourceQueueWithComplete[Message]](
        WebSocketRequest(uri),
        flow
      )
      context.become(sendable(outgoingQueue))
  }

  def sendable(outgoing: SourceQueueWithComplete[Message]): Receive = {
    case WebSocket.Release =>
      //TODO: close the websocket
      outgoing.complete() //This might end it all, because my outgoing is done?

    case WebSocket.Send(message) => //message to send

      println(s"SENT MESSAGE: $message ")
      val result = outgoing.offer(TextMessage(message))
      //TODO: should probably handle something in this, instead of doing *nothing*
      result.map {
        case Enqueued =>
        //Something
        case Dropped =>
        //shit!
        case Failure(uhoh) =>
        //SHIT!
      }

    case ignoreThis => // ignore I don't know why they take an ignore this unless it's the _

  }
}

object WebSocket {

  sealed trait WebSocketMessage

  case class Connect(
                      host: String,
                      port: Int,
                      resource: String,
                      withSsl: Boolean = false) extends WebSocketMessage

  case class Send(msg: String) extends WebSocketMessage

  case object Release extends WebSocketMessage

}

