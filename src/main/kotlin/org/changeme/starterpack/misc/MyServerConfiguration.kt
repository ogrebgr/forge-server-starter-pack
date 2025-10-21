package org.changeme.starterpack.misc

import com.bolyartech.forge.server.config.ForgeConfigurationException
import com.bolyartech.forge.server.misc.hexStringToByteArray
import com.goterl.lazysodium.LazySodiumJava
import com.goterl.lazysodium.SodiumJava
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.LibraryLoader
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.CRYPTO_PUBLIC_KEY
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.CRYPTO_SECRET_KEY
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.DEPLOYMENT
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.DEPLOYMENT_URL
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.SMTP_FROM_MAIL
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.SMTP_FROM_MAIL_PASSWORD
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.SMTP_HOST
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.SMTP_PORT
import org.changeme.starterpack.misc.MyServerConfigurationLoader.Companion.SMTP_USE_SSL
import org.slf4j.LoggerFactory
import java.io.*
import java.text.MessageFormat
import java.util.*

data class MyServerConfiguration(
    val deployment: Deployment,
    val deploymentUrl: String,

    val smtpHost: String,
    val smtpPort: Int,
    val smtpUseSsl: Boolean,
    val smtpFromMail: String,
    val smtpFromPassword: String,

    val cryptoSecretKey: Key,
    val cryptoPublicKey: Key,
) {
    enum class Deployment(val dname: String) {
        DEVELOPMENT("development"),
        TESTING("testing"),
        STAGING("staging"),
        PRODUCTION("production");
    }
}


interface MyServerConfigurationLoader {
    fun load(): MyServerConfiguration

    companion object {
        const val DEPLOYMENT = "deployment"
        const val DEPLOYMENT_URL = "deployment_url"

        const val SMTP_HOST = "smtp_host"
        const val SMTP_PORT = "smtp_port"
        const val SMTP_USE_SSL = "smtp_use_ssl"
        const val SMTP_FROM_MAIL = "smtp_from_mail"
        const val SMTP_FROM_MAIL_PASSWORD = "smtp_from_mail_password"

        const val CRYPTO_SECRET_KEY = "crypto_secret_key"
        const val CRYPTO_PUBLIC_KEY = "crypto_public_key"
    }
}

class MyServerConfigurationLoaderFile(private val configDir: String) : MyServerConfigurationLoader {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val CONF_FILENAME = "my.conf"
    }


    override fun load(): MyServerConfiguration {
        val confFile = File(configDir, CONF_FILENAME)
        if (!confFile.exists()) {
            logger.error("Cannot find configuration file: {}", confFile.absolutePath)
            throw IllegalStateException(
                MessageFormat.format(
                    "Cannot find configuration file: {}",
                    confFile.absolutePath
                )
            )
        }

        val prop = Properties()
        try {
            val `is`: InputStream = BufferedInputStream(FileInputStream(confFile))
            prop.load(`is`)
            `is`.close()
        } catch (e: IOException) {
            logger.error("Cannot load config file")
            throw IllegalStateException(e)
        }


        val smtpHost = prop.getProperty(SMTP_HOST)
        if (smtpHost.isNullOrEmpty()) {
            throw ForgeConfigurationException("SMTP_HOST is missing/empty")
        }

        val smtpPort = prop.getProperty(SMTP_PORT)
        if (smtpPort.isNullOrEmpty()) {
            throw ForgeConfigurationException("$SMTP_PORT is missing/empty")
        }

        val smtpPortInt = try {
            smtpPort.toInt()
        } catch (e: NumberFormatException) {
            throw ForgeConfigurationException("$SMTP_PORT is not integer")
        }

        if (smtpPortInt <= 0) {
            throw ForgeConfigurationException("$SMTP_PORT is not integer")
        }

        val smtpUseSsl = prop.getProperty(SMTP_USE_SSL)
        val smtpUseSslFinal: Boolean = smtpUseSsl.equals("1") || smtpUseSsl.lowercase(Locale.getDefault()).equals("yes") ||
                smtpUseSsl.lowercase(Locale.getDefault()).equals("y")

        val smtpFromMail = prop.getProperty(SMTP_FROM_MAIL)
        if (smtpFromMail.isNullOrEmpty()) {
            throw ForgeConfigurationException("$SMTP_FROM_MAIL is missing/empty")
        }

        val smtpFromMailPassword = prop.getProperty(SMTP_FROM_MAIL_PASSWORD)
        if (smtpFromMailPassword.isNullOrEmpty()) {
            throw ForgeConfigurationException("$SMTP_FROM_MAIL_PASSWORD is missing/empty")
        }

        val deploymentRaw = prop.getProperty(DEPLOYMENT)
        if (deploymentRaw.isNullOrEmpty()) {
            throw ForgeConfigurationException("$DEPLOYMENT is missing/empty")
        }

        val deployment = when (deploymentRaw) {
            MyServerConfiguration.Deployment.DEVELOPMENT.dname -> MyServerConfiguration.Deployment.DEVELOPMENT
            MyServerConfiguration.Deployment.TESTING.dname -> MyServerConfiguration.Deployment.TESTING
            MyServerConfiguration.Deployment.STAGING.dname -> MyServerConfiguration.Deployment.STAGING
            MyServerConfiguration.Deployment.PRODUCTION.dname -> MyServerConfiguration.Deployment.PRODUCTION
            else -> {
                throw ForgeConfigurationException("Invalid value for $DEPLOYMENT -> $deploymentRaw")
            }
        }

        val deploymentUrl = prop.getProperty(DEPLOYMENT_URL)
        if (deploymentUrl.isNullOrEmpty()) {
            throw ForgeConfigurationException("$DEPLOYMENT_URL is missing/empty")
        }

        val cryptoSecretKeyStr = prop.getProperty(CRYPTO_SECRET_KEY)
        if (cryptoSecretKeyStr.isNullOrEmpty()) {
            throw ForgeConfigurationException("$CRYPTO_SECRET_KEY is not set")
        }

        val cryptoPublicKeyStr = prop.getProperty(CRYPTO_PUBLIC_KEY)
        if (cryptoPublicKeyStr.isNullOrEmpty()) {
            throw ForgeConfigurationException("$CRYPTO_PUBLIC_KEY is not set")
        }

        val publicKey = try {
            cryptoPublicKeyStr.hexStringToByteArray()
        } catch (e: IllegalArgumentException) {
            throw ForgeConfigurationException("Invalid value for $CRYPTO_PUBLIC_KEY")
        }

        val secretKey = try {
            cryptoSecretKeyStr.hexStringToByteArray()
        } catch (e: IllegalArgumentException) {
            throw ForgeConfigurationException("Invalid value for $CRYPTO_SECRET_KEY")
        }
        val lazySodium = LazySodiumJava(SodiumJava(LibraryLoader.Mode.BUNDLED_ONLY))
        val secretKeyKey = Key.fromBytes(secretKey)
        val publicKeyKey = Key.fromBytes(publicKey)

        val signature = lazySodium.cryptoSignDetached("test", secretKeyKey)

        if (!lazySodium.cryptoSignVerifyDetached(signature, "test", publicKeyKey)) {
            throw ForgeConfigurationException("Specified $CRYPTO_SECRET_KEY and CRYPTO_PUBLIC_KEY not really a key pair. Test sign/verify failed.")
        }

        return MyServerConfiguration(deployment, deploymentUrl, smtpHost, smtpPortInt, smtpUseSslFinal, smtpFromMail, smtpFromMailPassword, secretKeyKey, publicKeyKey)
    }
}