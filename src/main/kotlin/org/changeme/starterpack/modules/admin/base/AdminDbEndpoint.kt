package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.handler.ForgeDbEndpoint
import com.bolyartech.forge.server.response.ResponseException
import com.bolyartech.forge.server.response.forge.BasicResponseCodes
import com.bolyartech.forge.server.response.forge.ForgeResponse
import com.bolyartech.forge.server.route.InvalidParameterValueException
import com.bolyartech.forge.server.route.MissingParameterValueException
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.data.AdminUser
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

abstract class AdminDbEndpoint(private val pack: AdminLoggedInParamPack) : ForgeDbEndpoint(pack.dbPool) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Throws(ResponseException::class, SQLException::class)
    abstract fun handle(
        ctx: RequestContext,
        dbc: Connection,
        user: AdminUser
    ): ForgeResponse

    override fun handleForge(ctx: RequestContext, dbc: Connection): ForgeResponse {
        val session = ctx.getSession()
        val user = session.getVar<AdminUser>(
            AdminSessionVars.VAR_USER
        )


        return if (user != null) {
            try {
                handle(ctx, dbc, user)
            } catch (e: MissingParameterValueException) {
                logger.error("Error: {}", e.message)
                ForgeResponse(BasicResponseCodes.Errors.ERROR)
            } catch (e: InvalidParameterValueException) {
                logger.error("Error: {}", e.message)
                ForgeResponse(BasicResponseCodes.Errors.ERROR)
            }
        } else {
            ForgeResponse(UserResponseCodes.NOT_LOGGED_IN, "Not logged in")
        }
    }

}