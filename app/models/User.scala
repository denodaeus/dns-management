package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class User(userId: Int, loginName: String)

object User {

  def canAccessSupportSite(username: String, password: String): Boolean = {
    val users = DB.withConnection("users")(implicit c => {
      sqlQuery(username, password).as(user *)
    })
    users.size == 1
  }
  
  private def sqlQuery(username: String, password: String): Sql = {
    SQL("""
        SELECT u.user_id, u.login_name, *
          FROM users.users u
         INNER JOIN guardian.passwords pwd ON u.user_id::VARCHAR = pwd.password_id
         INNER JOIN users.user_role_privs urp ON u.user_id = urp.user_id 
         WHERE login_name = {username}
           AND password_value = {password}
           AND password_type = 'UsrAct'
           AND user_status_id = 1
           AND role_id IN (1)
        """).on("username" -> username).on("password" -> password)
  }
  
  val user = {
    get[Int]("user_id") ~
    get[String]("login_name") map {
      case userId~login => User(userId, login)
    }
  }
}
