package controllers

import play.api.mvc._


trait Secured {
  
  def username(request: RequestHeader) = request.session.get(Security.username)
  
  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Authentication.login)
  
  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
  
}