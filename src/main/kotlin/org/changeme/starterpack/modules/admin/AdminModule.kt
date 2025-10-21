package org.changeme.starterpack.modules.admin

import com.bolyartech.forge.server.HttpMethod
import com.bolyartech.forge.server.handler.StaticFileHandler
import com.bolyartech.forge.server.misc.MimeTypeResolverImpl
import com.bolyartech.forge.server.module.SiteModule
import com.bolyartech.forge.server.route.GetRouteRuntimeResolved
import com.bolyartech.forge.server.route.Route
import com.bolyartech.forge.server.route.RouteExact
import org.changeme.starterpack.dagger.StaticFilesDir
import org.changeme.starterpack.modules.admin.pages.HomeWp
import org.changeme.starterpack.modules.admin.pages.LoginWp
import javax.inject.Inject

class AdminModule @Inject constructor(
    @StaticFilesDir private val staticFilesDir: String,
    private val homeWp: HomeWp,
    private val loginWp: LoginWp,
) : SiteModule {

    companion object {
        private const val MODULE_SYSTEM_NAME = "admin"
        private const val MODULE_VERSION_CODE = 1
        private const val MODULE_VERSION_NAME = "1.0.0"
        private const val PATH_PREFIX = "/admin/"
    }


    override fun createRoutes(): List<Route> {
        val ret = kotlin.collections.ArrayList<Route>()

        val mimeTypeResolver = MimeTypeResolverImpl()

        ret.add(RouteExact(HttpMethod.GET, PATH_PREFIX, homeWp))
        ret.add(RouteExact(HttpMethod.GET, PATH_PREFIX + "login", loginWp))
        ret.add(RouteExact(HttpMethod.POST, PATH_PREFIX + "login", loginWp))

        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}img/",
                StaticFileHandler(staticFilesDir + "/admin/img/", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}fontawesome/",
                StaticFileHandler(staticFilesDir + "/admin/fontawesome-free-6.1.1-web/", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}css/",
                StaticFileHandler(staticFilesDir + "/admin/css/", mimeTypeResolver, true)
            )
        )
        ret.add(GetRouteRuntimeResolved("${PATH_PREFIX}js/", StaticFileHandler(staticFilesDir + "/admin/js/", mimeTypeResolver, true)))
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}fonts/",
                StaticFileHandler(staticFilesDir + "/admin/fonts/", mimeTypeResolver, true)
            )
        )
        ret.add(
            GetRouteRuntimeResolved(
                "${PATH_PREFIX}plugins/",
                StaticFileHandler(staticFilesDir + "/admin/plugins/", mimeTypeResolver, true)
            )
        )
        return ret
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