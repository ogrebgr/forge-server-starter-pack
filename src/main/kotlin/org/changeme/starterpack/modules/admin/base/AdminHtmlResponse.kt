package org.changeme.starterpack.modules.admin.base

import com.bolyartech.forge.server.response.HtmlResponse
import com.bolyartech.forge.server.response.HttpHeader
import jakarta.servlet.http.Cookie

class AdminHtmlResponse(
    body: String,
    cookiesToSet: List<Cookie> = emptyList(),
    headersToAdd: List<HttpHeader> = emptyList(),
    enableGzipSupport: Boolean = true
) : HtmlResponse(body, cookiesToSet, headersToAdd, enableGzipSupport)