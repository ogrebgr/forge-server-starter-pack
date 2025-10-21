package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.db.DbPool
import com.bolyartech.forge.server.handler.DbWebPage
import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.misc.TemplateEngineFactory
import com.bolyartech.forge.server.response.HtmlResponse
import com.bolyartech.forge.server.response.HttpHeader
import com.bolyartech.forge.server.response.RedirectResponse303SeeOther
import com.bolyartech.forge.server.response.Response
import com.bolyartech.forge.server.response.builders.HtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.InvalidParameterValueException
import com.bolyartech.forge.server.route.MissingParameterValueException
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.modules.admin.base.AdminHtmlResponse
import org.changeme.starterpack.modules.admin.base.AdminSessionVars
import org.changeme.starterpack.modules.admin.base.AdminSessionVars.VAR_USER
import org.changeme.starterpack.modules.admin.base.LoginRedirectResponse
import org.changeme.starterpack.modules.admin.pages.LoginWp.Companion.REMEMBER_ME_COOKIE_NAME
import jakarta.servlet.http.Cookie
import org.changeme.starterpack.dagger.AdminModuleTpleFactory
import org.changeme.starterpack.dagger.Deployment
import org.changeme.starterpack.dagger.InternalServerErrorBuilder
import org.changeme.starterpack.dagger.MyServerSignerVerifier
import org.changeme.starterpack.dagger.NotFoundBuilder
import org.changeme.starterpack.misc.CryptoSignerVerifier
import org.changeme.starterpack.misc.MyServerConfiguration
import org.changeme.starterpack.modules.admin.data.AdminUser
import org.changeme.starterpack.modules.admin.data.AdminUserDbh
import org.changeme.starterpack.modules.admin.data.PropertyDbh
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.sql.Connection
import javax.inject.Inject
import kotlin.jvm.javaClass
import kotlin.text.split
import kotlin.text.toInt

abstract class AdminDbWebPage(
    private val pack: AdminLoggedInParamPack
) : DbWebPage(pack.templateEngineFactory, pack.dbPool) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    abstract fun handleAdminPage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine, admin: AdminUser): ResponseBuilder

    override fun handlePage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine): Response {
        val session = ctx.getSession()
        val user = session.getVar<AdminUser>(
            VAR_USER
        )
        tple.assign("_is_logged_in", true)

        return if (user != null) {
            tple.assign("_is_superadmin", user.isSuperAdmin)
            tple.assign("_user_name", user.name)
            try {
                tple.assign("deployment", pack.deployment.dname)
                tple.assign("ip", ctx.getServerData().serverAddress)
                handleAdminPage(ctx, dbc, tple, user).build()
            } catch (e: MissingParameterValueException) {
                tple.export("_subdir", false)
                logger.error("Error: {}", e.message)
                tple.assign("_page", "http_500")
                createHtmlResponse(tple.render("template_http_error.vm"))
            } catch (e: InvalidParameterValueException) {
                tple.export("_subdir", false)
                logger.error("Error: {}", e.message)
                tple.assign("_page", "http_500")
                createHtmlResponse(tple.render("template_http_error.vm"))
            } catch (e: Throwable) {
                tple.export("_subdir", false)
                logger.error("Error handling {}, Error: {}", this.javaClass, e.message)
                logger.error("Exception: ", e)
                tple.assign("_page", "http_500")
                createHtmlResponse(tple.render("template_http_error.vm"))
            }
        } else {
            val rememberMeCookie = ctx.getCookie(REMEMBER_ME_COOKIE_NAME)
            if (rememberMeCookie != null && rememberMeCookie.value != "") {
                val items = rememberMeCookie.value.split("|")
                if (items.size == 2) {
                    val userId = try {
                        items[0].toInt()
                    } catch (e: NumberFormatException) {
                        return LoginRedirectResponse("/admin/", Cookie(REMEMBER_ME_COOKIE_NAME, ""))
                    }

                    if (!pack.signer.verify(items[0], items[1])) {
                        return LoginRedirectResponse("/admin/", Cookie(REMEMBER_ME_COOKIE_NAME, ""))
                    }


                    val userObj = pack.adminUserDbh.loadById(dbc, userId)
                    if (userObj != null && !userObj.isDisabled) {
                        session.setVar<Serializable>(VAR_USER, userObj)
                        tple.assign("_user_name", userObj.name)

                        tple.assign("deployment", pack.deployment.dname)
                        tple.assign("ip", ctx.getServerData().serverAddress)

                        handleAdminPage(ctx, dbc, tple, userObj).build()
                    } else {
                        RedirectResponse303SeeOther("/admin/login")
                    }
                } else {
                    RedirectResponse303SeeOther("/admin/login")
                }
            } else {
                RedirectResponse303SeeOther("/admin/login")
            }
        }
    }

    private fun createHtmlResponse(
        content: String,
        cookiesToSet: List<Cookie> = emptyList(),
        headersToAdd: List<HttpHeader> = emptyList()
    ): HtmlResponse {
        return AdminHtmlResponse(content, cookiesToSet, headersToAdd)
    }
}


data class AdminLoggedInParamPack @Inject constructor(
    @AdminModuleTpleFactory val templateEngineFactory: TemplateEngineFactory,
    val dbPool: DbPool,
    @Deployment val deployment: MyServerConfiguration.Deployment,
    val adminUserDbh: AdminUserDbh,
    val propertyDbh: PropertyDbh,
    @MyServerSignerVerifier val signer: CryptoSignerVerifier,
    @InternalServerErrorBuilder val internalServerErrorBuilder: HtmlResponseBuilder,
    @NotFoundBuilder val notFoundBuilder: HtmlResponseBuilder,
)