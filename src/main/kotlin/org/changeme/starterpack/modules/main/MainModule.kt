package org.changeme.starterpack.modules.main

import com.bolyartech.forge.server.HttpMethod
import com.bolyartech.forge.server.handler.StaticFileHandler
import com.bolyartech.forge.server.misc.MimeTypeResolver
import com.bolyartech.forge.server.misc.VelocityTemplateEngineFactory
import com.bolyartech.forge.server.module.SiteModule
import com.bolyartech.forge.server.route.GetRouteExact
import com.bolyartech.forge.server.route.GetRouteRuntimeResolved
import com.bolyartech.forge.server.route.Route
import com.bolyartech.forge.server.route.RouteExact
import org.changeme.starterpack.dagger.StaticFilesDir
import org.changeme.starterpack.modules.main.pages.HomeWp
import javax.inject.Inject

class MainModule @Inject constructor(
    @StaticFilesDir private val staticFilesDir: String,
    private val mimeTypeResolver: MimeTypeResolver,
    private val homeWp: HomeWp,
) : SiteModule {

    companion object {
        private const val MODULE_SYSTEM_NAME = "main"
        private const val MODULE_VERSION_CODE = 1
        private const val MODULE_VERSION_NAME = "1.0.0"
        private const val PATH_PREFIX = "/"
    }

    override fun createRoutes(): List<Route> {
        val ret = mutableListOf<Route>()

        val map = mapOf<String, String>("event_handler.include.class" to "org.apache.velocity.app.event.implement.IncludeRelativePath")
        VelocityTemplateEngineFactory("/templates/modules/main/", map)

        addCommonRoutes(ret)
        ret.add(RouteExact(HttpMethod.GET, PATH_PREFIX, homeWp))

        return ret
    }

    private fun addCommonRoutes(ret: MutableList<Route>) {
        ret.add(
            GetRouteExact(
                "${PATH_PREFIX}robots.txt",
                StaticFileHandler(staticFilesDir + "/main/robots.txt", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteExact(
                "${PATH_PREFIX}favicon.ico",
                StaticFileHandler(staticFilesDir + "/main/favicon.ico", mimeTypeResolver, true)
            )
        )

        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}img/",
                StaticFileHandler(staticFilesDir + "/main/img/", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}css/",
                StaticFileHandler(staticFilesDir + "/main/css/", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}js/",
                StaticFileHandler(staticFilesDir + "/main/js/", mimeTypeResolver, true)
            )
        )
    }

    override fun getSystemName(): String {
        return MODULE_SYSTEM_NAME
    }

    override fun getShortDescription(): String {
        return ""
    }

    override fun getVersionCode(): Int {
        return MODULE_VERSION_CODE
    }

    override fun getVersionName(): String {
        return MODULE_VERSION_NAME
    }
}