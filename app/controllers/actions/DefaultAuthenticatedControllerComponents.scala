package controllers.actions

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait AuthenticatedControllerComponents extends ControllerComponents {

  def actionBuilder: DefaultActionBuilder

  def identify: AuthAction

  def requireVat: VatRequiredAction

  def auth(): ActionBuilder[AuthorisedRequest, AnyContent] =
    actionBuilder andThen
      identify

  def authAndRequireVat(): ActionBuilder[AuthorisedMandatoryVrnRequest, AnyContent] =
    auth() andThen
      requireVat

}

case class DefaultAuthenticatedControllerComponents @Inject()(
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  executionContext: ExecutionContext,
  identify: AuthAction,
  requireVat: VatRequiredAction
) extends AuthenticatedControllerComponents
