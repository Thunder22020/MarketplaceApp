package com.daniel.marketplaceapp.core.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BaseApiException::class)
    fun handleApiException(
        ex: BaseApiException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.error("Handling API exception", ex)

        val body = ApiError(
            status = ex.status.value(),
            code = ex.code,
            message = ex.message ?: "Unexpected error",
            path = request.requestURI,
        )
        return ResponseEntity.status(ex.status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.error("Validation exception", ex)
        val details = ex.bindingResult.fieldErrors.map {
            ApiErrorDetail(it.field, it.defaultMessage ?: "Invalid value")
        }
        val body = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "VALIDATION_ERROR",
            message = "Validation failed",
            path = request.requestURI,
            details = details
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        log.error("Handling exception", ex)

        val body = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "INTERNAL_SERVER_ERROR",
            message = "Something went wrong",
            path = request.requestURI,
        )
        return ResponseEntity.status(500).body(body)
    }
}