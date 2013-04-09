import play.api.Application
import play.api.db.DB 
import play.api.GlobalSettings
import play.api._
import play.api.mvc._
import controllers.Secured

object Global extends GlobalSettings with Secured{
  
  override def onStart(app: Application) {
  }
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    super.onRouteRequest(request)
  }
  
  val authFilter = Filter { (next, rh) =>
    next(rh)
  }

}
