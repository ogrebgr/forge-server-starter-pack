package org.changeme.starterpack.modules.admin.pages

import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.response.builders.OkHtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.MyServer
import org.changeme.starterpack.modules.admin.base.AdminDbWebPage
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.data.AdminUser
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShutdownWp @Inject constructor(
    pack: AdminLoggedInParamPack,
) : AdminDbWebPage(pack) {

    private lateinit var myServer: MyServer

    fun init(liMasterServer: MyServer) {
        this.myServer = liMasterServer
    }

    override fun handleAdminPage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine, admin: AdminUser): ResponseBuilder {
//        thread {
//            liMasterServer.shutdown()
//        }
        return OkHtmlResponseBuilder("shutdown")
    }

}