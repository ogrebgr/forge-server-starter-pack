package org.changeme.starterpack.modules.admin.pages

import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.response.builders.OkHtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.modules.admin.base.AdminDbWebPage
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.base.AdminSessionVars
import org.changeme.starterpack.modules.admin.data.AdminUser
import jakarta.servlet.http.Cookie
import java.sql.Connection
import javax.inject.Inject

class LogoutWp @Inject constructor(
    pack: AdminLoggedInParamPack,
) : AdminDbWebPage(pack) {

    override fun handleAdminPage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine, admin: AdminUser): ResponseBuilder {
        val session = ctx.getSession()
        session.removeVar(AdminSessionVars.VAR_USER)

        tple.assign("_page", "logout")
        return OkHtmlResponseBuilder(tple.render("template.vm")).addCookie(Cookie(LoginWp.REMEMBER_ME_COOKIE_NAME, ""))
    }
}