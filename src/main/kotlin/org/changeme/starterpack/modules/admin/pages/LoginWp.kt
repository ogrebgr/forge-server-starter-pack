package org.changeme.starterpack.modules.admin.pages

import com.bolyartech.forge.server.HttpMethod
import com.bolyartech.forge.server.handler.DbWebPage
import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.response.HtmlResponse
import com.bolyartech.forge.server.response.Response
import com.bolyartech.forge.server.route.MissingParameterValueException
import com.bolyartech.forge.server.route.RequestContext
import jakarta.servlet.http.Cookie
import org.changeme.starterpack.dagger.MyServerSigner
import org.changeme.starterpack.misc.CryptoSigner
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.base.AdminSessionVars.VAR_USER
import org.changeme.starterpack.modules.admin.base.LoginRedirectResponse
import org.changeme.starterpack.modules.admin.data.AdminUser
import org.changeme.starterpack.modules.admin.data.AdminUserBlowfishDbh
import org.changeme.starterpack.modules.admin.data.AdminUserDbh
import org.mindrot.jbcrypt.BCrypt
import java.io.Serializable
import java.sql.Connection
import javax.inject.Inject


class LoginWp @Inject constructor(
    private val pack: AdminLoggedInParamPack,
    private val blowfishDbh: AdminUserBlowfishDbh,
    private val adminUserDbh: AdminUserDbh,
    @MyServerSigner private val signer: CryptoSigner,
) : DbWebPage(pack.templateEngineFactory, pack.dbPool) {

    companion object {
        private const val PAUSE_AFTER_UNSUCCESSFUL_LOGIN_MILLIS = 500L

        private const val PARAM_USERNAME = "username"
        private const val PARAM_PASSWORD = "password"
        const val REMEMBER_ME_COOKIE_NAME = "rm"
    }

    private fun postMode(ctx: RequestContext, dbc: Connection, tple: TemplateEngine): Response {
        val username = ctx.getFromPost(PARAM_USERNAME) ?: throw MissingParameterValueException()
        val password = ctx.getFromPost(PARAM_PASSWORD) ?: throw MissingParameterValueException()


        val bu = blowfishDbh.loadByUsername(dbc, username) ?: return invalidLogin(tple, username)

        if (!BCrypt.checkpw(password, bu.password)) {
            return invalidLogin(tple, username)
        }

        val user = adminUserDbh.loadById(dbc, bu.user) ?: return invalidLogin(tple, username)
        if (user.isDisabled) {
            return invalidLogin(tple, username)
        }

        val session = ctx.getSession()
        session.setVar<Serializable>(VAR_USER, user)

        val cookieVal = user.id.toString() + "|" + signer.sign(user.id.toString())
        val c = Cookie(REMEMBER_ME_COOKIE_NAME, cookieVal)
        c.maxAge = 60 * 60 * 24 * 365

        return LoginRedirectResponse("/admin/", c)
    }

    private fun getMode(ctx: RequestContext, tple: TemplateEngine, ip: String): Response {
        val user = ctx.getSession().getVar<AdminUser>(
            VAR_USER
        )

        if (user != null) {
            return LoginRedirectResponse("/admin/")
        }

        val rememberMeCookie = ctx.getCookie(REMEMBER_ME_COOKIE_NAME)
        if (rememberMeCookie != null && rememberMeCookie.value != "") {
            return LoginRedirectResponse("/admin/")
        }

        tple.assign("deployment", pack.deployment.dname)
        tple.assign("ip", ip)
        return HtmlResponse(tple.render("login/login.vm"))
    }

    private fun invalidLogin(tple: TemplateEngine, username: String): HtmlResponse {
        Thread.sleep(PAUSE_AFTER_UNSUCCESSFUL_LOGIN_MILLIS)
        tple.assign("invalid_login", true)
        tple.assign("username", username)
        return HtmlResponse(tple.render("login/login.vm"))
    }

    override fun handlePage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine): Response {
        return if (ctx.isMethod(HttpMethod.GET)) {
            getMode(ctx, tple, ctx.getServerData().serverAddress)
        } else if (ctx.isMethod(HttpMethod.POST)) {
            postMode(ctx, dbc, tple)
        } else {
            throw IllegalStateException("Unknown method")
        }
    }
}