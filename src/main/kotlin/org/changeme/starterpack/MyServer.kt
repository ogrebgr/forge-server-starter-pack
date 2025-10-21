package org.changeme.starterpack

import com.bolyartech.forge.server.AbstractForgeServerAdapter
import com.bolyartech.forge.server.ForgeServer
import com.bolyartech.forge.server.WebServer
import com.bolyartech.forge.server.db.DbPool
import com.bolyartech.forge.server.handler.RouteHandler
import com.bolyartech.forge.server.jetty.WebServerJetty
import com.bolyartech.forge.server.module.SiteModule
import org.changeme.starterpack.dagger.InternalServerErrorHandler
import org.changeme.starterpack.dagger.NotFoundHandler
import org.changeme.starterpack.modules.admin.AdminModule
import org.changeme.starterpack.modules.main.MainModule
import java.nio.file.FileSystem
import javax.inject.Inject
import javax.sql.DataSource

class MyServer @Inject constructor(
    private val mainModule: MainModule,
    private val adminModule: AdminModule,
    private val dbPool: DbPool,
    private val dataSource: DataSource,
    @NotFoundHandler private val notFoundHandler: RouteHandler,
    @InternalServerErrorHandler private val internalServerErrorHandler: RouteHandler,
) : AbstractForgeServerAdapter() {

    override fun testDbConnection() {
        dbPool.connection.use {
            // just to check if it is successful
        }
    }

    override fun createWebServer(
        forgeConfig: ForgeServer.ConfigurationPack,
        fileSystem: FileSystem,
    ): WebServer {
        return WebServerJetty(
            forgeConfig,
            createModules(),
            notFoundHandler,
            internalServerErrorHandler,
            WebServerJetty.createDbSessionDataStoreFactory(dataSource)
        )
    }

    private fun createModules(): List<SiteModule> {
        return listOf(mainModule, adminModule)
    }
}