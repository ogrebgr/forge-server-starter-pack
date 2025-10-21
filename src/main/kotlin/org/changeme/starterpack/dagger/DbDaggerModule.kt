package org.changeme.starterpack.dagger

import com.bolyartech.forge.server.ForgeServer
import com.bolyartech.forge.server.db.DbPool
import com.bolyartech.forge.server.db.HikariCpDbConfiguration
import com.bolyartech.forge.server.db.HikariCpDbPool
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.changeme.starterpack.modules.admin.data.AdminUserBlowfishDbh
import org.changeme.starterpack.modules.admin.data.AdminUserBlowfishDbhImpl
import org.changeme.starterpack.modules.admin.data.AdminUserDbh
import org.changeme.starterpack.modules.admin.data.AdminUserDbhImpl
import org.changeme.starterpack.modules.admin.data.PropertyDbh
import org.changeme.starterpack.modules.admin.data.PropertyDbhImpl
import javax.inject.Singleton
import javax.sql.DataSource

@Module
class DbDaggerModule(private val dbConfig: HikariCpDbConfiguration) {
    @Provides
    @Singleton
    fun provideComboPooledDataSource(): DataSource {
        return ForgeServer.createDataSourceHelper(dbConfig)
    }

    @Provides
    @Singleton
    internal fun provideDbPool(dbSource: DataSource): DbPool {
        return HikariCpDbPool(dbSource)
    }
}

@Module
abstract class DbDaggerModuleBinds {
    @Binds
    internal abstract fun bindAdminUserBlowfishDbh(impl: AdminUserBlowfishDbhImpl): AdminUserBlowfishDbh

    @Binds
    internal abstract fun bindAdminUserDbh(impl: AdminUserDbhImpl): AdminUserDbh

    @Binds
    internal abstract fun bindPropertyDbh(impl: PropertyDbhImpl): PropertyDbh
}