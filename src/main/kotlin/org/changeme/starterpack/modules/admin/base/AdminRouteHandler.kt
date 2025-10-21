package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.handler.RouteHandler
import com.bolyartech.forge.server.response.Response
import com.bolyartech.forge.server.response.builders.InternalServerErrorHtmlResponseBuilder
import com.bolyartech.forge.server.route.InvalidParameterValueException
import com.bolyartech.forge.server.route.MissingParameterValueException
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.data.AdminUser
import org.slf4j.LoggerFactory
import java.sql.Connection

abstract class AdminRouteHandler(private val pack: AdminLoggedInParamPack) : RouteHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    abstract fun handleAdmin(ctx: RequestContext, dbc: Connection, user: AdminUser): Response

    override fun handle(ctx: RequestContext): Response {
        val session = ctx.getSession()
        val user = session.getVar<AdminUser>(
            AdminSessionVars.VAR_USER
        )

        return if (user != null) {
            pack.dbPool.connection.use {
                try {
                    handleAdmin(ctx, it, user)
                } catch (e: MissingParameterValueException) {
                    logger.error("Error: {}", e.message)
                    InternalServerErrorHtmlResponseBuilder("").build()
                } catch (e: InvalidParameterValueException) {
                    logger.error("Error: {}", e.message)
                    InternalServerErrorHtmlResponseBuilder("").build()
                }
            }
        } else {
            InternalServerErrorHtmlResponseBuilder("").build()
        }
    }
}