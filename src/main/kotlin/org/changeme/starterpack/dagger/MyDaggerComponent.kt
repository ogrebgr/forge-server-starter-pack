package org.changeme.starterpack.dagger

import dagger.Component
import org.changeme.starterpack.MyServer
import javax.inject.Singleton

@Singleton
@Component(
    modules = [ServerDaggerModule::class,
        ServerDaggerModuleBinds::class,
        DbDaggerModule::class,
        DbDaggerModuleBinds::class]
)
interface MyDaggerComponent {
    fun provideServer(): MyServer
}