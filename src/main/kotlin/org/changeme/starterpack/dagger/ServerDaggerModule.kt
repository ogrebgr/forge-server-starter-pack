package org.changeme.starterpack.dagger


import com.bolyartech.forge.server.ForgeServer
import com.bolyartech.forge.server.handler.RouteHandler
import com.bolyartech.forge.server.misc.MimeTypeResolver
import com.bolyartech.forge.server.misc.MimeTypeResolverImpl
import com.bolyartech.forge.server.misc.TemplateEngineFactory
import com.bolyartech.forge.server.misc.VelocityTemplateEngineFactory
import com.bolyartech.forge.server.response.Response
import com.bolyartech.forge.server.response.builders.BadRequestHtmlResponseBuilder
import com.bolyartech.forge.server.response.builders.HtmlResponseBuilder
import com.bolyartech.forge.server.route.RequestContext
import com.google.gson.Gson
import com.goterl.lazysodium.LazySodium
import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.LibraryLoader
import dagger.Module
import dagger.Provides
import org.changeme.starterpack.misc.CryptoSigner
import org.changeme.starterpack.misc.CryptoSignerImpl
import org.changeme.starterpack.misc.CryptoSignerVerifier
import org.changeme.starterpack.misc.CryptoSignerVerifierImpl
import org.changeme.starterpack.misc.MyServerConfiguration
import java.nio.file.FileSystem
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class ServerDaggerModule(
    private val fileSystem: FileSystem,
    private val forgeConfig: ForgeServer.ConfigurationPack,
    private val myConf: MyServerConfiguration
) {


    @Provides
    fun provideFileSystem(): FileSystem {
        return fileSystem
    }

    @Provides
    @StaticFilesDir
    fun provideStaticFilesDir(): String {
        return forgeConfig.forgeServerConfiguration.staticFilesDir
    }

    @Provides
    fun provideConfigurationPack(): ForgeServer.ConfigurationPack {
        return forgeConfig
    }

    @Provides
    fun provideMimeTypeResolver(): MimeTypeResolver {
        return MimeTypeResolverImpl()
    }

    @Provides
    fun provideMyServerConfiguration(): MyServerConfiguration {
        return myConf
    }

    @Provides
    @UploadsDir
    fun provideUploadsDir(): String {
        return forgeConfig.forgeServerConfiguration.uploadsDirectory
    }

    @Provides
    @DownloadsDir
    fun provideDownloadsDir(): String {
        return forgeConfig.forgeServerConfiguration.downloadsDirectory
    }

    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Deployment
    fun provideDeployment(): MyServerConfiguration.Deployment {
        return myConf.deployment
    }

    @Provides
    @NotFoundHandler
    fun provideNotFoundHandler(@NotFoundBuilder b: HtmlResponseBuilder): RouteHandler {
        return object : RouteHandler {
            override fun handle(ctx: RequestContext): Response {
                return b.build()
            }
        }
    }

    @Provides
    @InternalServerErrorHandler
    fun provideInternalServerErrorHandler(@InternalServerErrorBuilder b: HtmlResponseBuilder): RouteHandler {
        return object : RouteHandler {
            override fun handle(ctx: RequestContext): Response {
                return b.build()
            }
        }
    }

    @Provides
    @NotFoundBuilder
    fun provideNotFoundBuilder(@PublicModuleTpleFactory tplef: TemplateEngineFactory): HtmlResponseBuilder {
        val tple = tplef.createNew()
        tple.assign("_subdir", "_http_errors")
        tple.assign("_page", "http_404")
        return HtmlResponseBuilder(404).body(tple.render("template_http_error.vm"))
    }

    @Provides
    @InternalServerErrorBuilder
    fun provideInternalServerErrorBuilder(@PublicModuleTpleFactory tplef: TemplateEngineFactory): HtmlResponseBuilder {
        val tple = tplef.createNew()
        tple.assign("_subdir", "_http_errors")
        tple.assign("_page", "http_500")
        return HtmlResponseBuilder(500).body(tple.render("template_http_error.vm"))
    }

    @Provides
    @BadRequestBuilder
    fun provideBadRequestBuilder(@PublicModuleTpleFactory tplef: TemplateEngineFactory): HtmlResponseBuilder {
        val tple = tplef.createNew()
        tple.assign("_subdir", "_http_errors")
        tple.assign("_page", "http_400")
        return BadRequestHtmlResponseBuilder(tple.render("template_http_error.vm"))
    }

    @Provides
    @PaymentRequiredErrorBuilder
    fun providePaymentRequiredErrorBuilder(@PublicModuleTpleFactory tplef: TemplateEngineFactory): HtmlResponseBuilder {
        val tple = tplef.createNew()
        tple.assign("_subdir", "_http_errors")
        tple.assign("_page", "http_402")
        return HtmlResponseBuilder(402).body(tple.render("template_http_error.vm"))
    }

    @PublicModuleTpleFactory
    @Provides
    fun providePublicTemplateEngineFactory(): TemplateEngineFactory {
        val map =
            mapOf<String, String>("event_handler.include.class" to "org.apache.velocity.app.event.implement.IncludeRelativePath")
        return VelocityTemplateEngineFactory("/templates/main/", map)
    }

    @AdminModuleTpleFactory
    @Provides
    fun provideAdminTemplateEngineFactory(): TemplateEngineFactory {
        val map =
            mapOf<String, String>("event_handler.include.class" to "org.apache.velocity.app.event.implement.IncludeRelativePath")
        return VelocityTemplateEngineFactory("/templates/admin/", map)
    }

    @Provides
    @MyServerSigner
    @Singleton
    fun provideMasterSigner(lz: LazySodium): CryptoSigner {
        return CryptoSignerImpl(lz, myConf.cryptoSecretKey, 1)
    }

    @Provides
    @MyServerSignerVerifier
    @Singleton
    fun provideMasterSignerVerifier(lz: LazySodium): CryptoSignerVerifier {
        return CryptoSignerVerifierImpl(lz, myConf.cryptoSecretKey, myConf.cryptoPublicKey, 1)
    }

    @Provides
    @Singleton
    fun provideLazySodium(): LazySodium {
        return LazySodiumJava(SodiumJava(LibraryLoader.Mode.BUNDLED_ONLY))
    }

}


@Module
abstract class ServerDaggerModuleBinds

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class StaticFilesDir

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicModuleTpleFactory


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NotFoundHandler

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class InternalServerErrorHandler

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NotFoundBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class BadRequestBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class InternalServerErrorBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PaymentRequiredErrorBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UploadsDir

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DownloadsDir

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Deployment

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AdminModuleTpleFactory

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyServerSigner

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyServerSignerVerifier