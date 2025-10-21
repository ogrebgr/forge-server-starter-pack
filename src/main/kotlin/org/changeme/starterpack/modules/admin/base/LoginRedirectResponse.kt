package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.response.RedirectResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse


class LoginRedirectResponse(private val location: String, private val rememberMeCookie: Cookie? = null) : RedirectResponse {
    override fun getLocation(): String {
        return location
    }

    override fun toServletResponse(resp: HttpServletResponse): Long {
        if (rememberMeCookie != null) {
            resp.addCookie(rememberMeCookie)
        }

        resp.status = HttpServletResponse.SC_SEE_OTHER
        resp.setHeader(HEADER_LOCATION, location)
        return 0
    }

    companion object {
        private const val HEADER_LOCATION = "Location"
    }

}
