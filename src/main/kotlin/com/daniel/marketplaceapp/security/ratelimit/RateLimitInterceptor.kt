package com.daniel.marketplaceapp.security.ratelimit

import com.daniel.marketplaceapp.security.exception.TooManyRequestsException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val redis: StringRedisTemplate
) : HandlerInterceptor {
    override fun preHandle(
        req: HttpServletRequest,
        res: HttpServletResponse,
        handler: Any
    ): Boolean {
        val limit = limits[req.requestURI] ?: return true
        val limitWindowSeconds = limit.windowSeconds.toString()
        val ip = extractIp(req)
        val key = "rate:${req.requestURI.replace("/", ":")}:$ip"
        val count = redis.execute(
            RATE_LIMIT_SCRIPT,
            listOf(key),
            limitWindowSeconds,
        )
        if (count > limit.max) {
            val retryAfter = redis.getExpire(key).toString()
            throw TooManyRequestsException(
                "Too many requests, retry after $retryAfter seconds",
                retryAfter
            )
        }
        return true
    }

    private fun extractIp(req: HttpServletRequest): String {
        return req.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: req.remoteAddr
    }

    companion object {
        private val limits = mapOf(
            "/api/auth/login" to Limit(10, 150),
            "/api/auth/register" to Limit(5, 300)
        )

        private val RATE_LIMIT_SCRIPT = RedisScript.of<Long>(
            """
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """.trimIndent(), Long::class.java)
    }
}
