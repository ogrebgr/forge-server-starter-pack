package org.changeme.starterpack.modules.main.pages

import com.bolyartech.forge.server.db.DbPool
import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.misc.TemplateEngineFactory
import com.bolyartech.forge.server.response.HtmlResponse
import com.bolyartech.forge.server.response.builders.OkHtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.dagger.Deployment
import org.changeme.starterpack.dagger.PublicModuleTpleFactory
import org.changeme.starterpack.misc.MyServerConfiguration
import org.changeme.starterpack.modules.main.base.PublicDbWebPage
import java.sql.Connection
import javax.inject.Inject

class HomeWp @Inject constructor(
    @PublicModuleTpleFactory templateEngineFactory: TemplateEngineFactory,
    dbPool: DbPool,
    @Deployment deployment: MyServerConfiguration.Deployment,
) : PublicDbWebPage(templateEngineFactory, dbPool, deployment) {

    override fun handlePublicPage(
        ctx: RequestContext,
        dbc: Connection,
        tple: TemplateEngine
    ): ResponseBuilder {

        tple.export("_page", "home")
        return OkHtmlResponseBuilder(tple.render("template.vm"))
    }
}