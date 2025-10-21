package org.changeme.starterpack.modules.main.base

import com.bolyartech.forge.server.db.DbPool
import com.bolyartech.forge.server.handler.DbWebPage
import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.misc.TemplateEngineFactory
import com.bolyartech.forge.server.response.Response
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.dagger.Deployment
import org.changeme.starterpack.dagger.PublicModuleTpleFactory
import org.changeme.starterpack.misc.MyServerConfiguration
import java.sql.Connection

abstract class PublicDbWebPage(
    @PublicModuleTpleFactory templateEngineFactory: TemplateEngineFactory,
    dbPool: DbPool,
    protected val deployment: MyServerConfiguration.Deployment,
) : DbWebPage(templateEngineFactory, dbPool) {

    abstract fun handlePublicPage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine): ResponseBuilder

    override fun handlePage(ctx: RequestContext, dbc: Connection, tple: TemplateEngine): Response {
        tple.assign("deployment", deployment.dname)
        tple.assign("ip", ctx.getServerData().serverAddress)
        return handlePublicPage(ctx, dbc, tple).build()
    }
}