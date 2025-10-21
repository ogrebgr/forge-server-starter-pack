package org.changeme.starterpack.modules.admin.pages

import com.bolyartech.forge.server.misc.TemplateEngine
import com.bolyartech.forge.server.response.builders.OkHtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.ResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import org.changeme.starterpack.modules.admin.base.AdminDbWebPage
import org.changeme.starterpack.modules.admin.base.AdminLoggedInParamPack
import org.changeme.starterpack.modules.admin.data.AdminUser
import java.sql.Connection
import javax.inject.Inject

class HomeWp @Inject constructor(
    pack: AdminLoggedInParamPack,
) : AdminDbWebPage(pack) {

    override fun handleAdminPage(
        ctx: RequestContext,
        dbc: Connection,
        tple: TemplateEngine,
        admin: AdminUser
    ): ResponseBuilder {

        tple.assign("_page", "home")
        return OkHtmlResponseBuilder(tple.render("template.vm"))
    }
}