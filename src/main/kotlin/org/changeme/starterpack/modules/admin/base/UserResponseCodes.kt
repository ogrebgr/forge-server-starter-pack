package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.response.forge.ForgeResponseCode

enum class UserResponseCodes(private val _code: Int) : ForgeResponseCode {
    /**
     * Registration related codes
     */
    REGISTRATION_REFUSED(-7),
    USERNAME_EXISTS(-8),
    PASSWORD_TOO_SHORT(-9),
    INVALID_USERNAME(-10),
    INVALID_PASSWORD(-11),


    /**
     * Login related codes
     */
    INVALID_LOGIN(-12), // user + password does not match valid account
    NOT_LOGGED_IN(-13), // not logged in

    NO_ENOUGH_PRIVILEGES(-14),

    INVALID_SCREEN_NAME(-50),
    SCREEN_NAME_EXISTS(-51),
    SCREEN_NAME_CHANGE_NOT_SUPPORTED(-52);

    override fun getCode(): Int {
        return _code
    }
}