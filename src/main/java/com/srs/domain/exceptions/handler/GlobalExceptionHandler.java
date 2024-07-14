package com.srs.domain.exceptions.handler;

import com.srs.domain.exceptions.CapacityFullException;
import com.srs.domain.models.GlobalErrorMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.MappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.SignatureException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleException(Exception exception, ServerHttpRequest request) {
        log.error("Exception: {}", exception.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder()
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(exception.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleRuntimeException(RuntimeException runtimeException, ServerWebExchange exchange) {
        log.error("RuntimeException: {}", runtimeException.getMessage(), runtimeException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(runtimeException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(MappingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleMappingException(MappingException mappingException, ServerWebExchange exchange) {
        log.error("MappingException: {}", mappingException.getMessage(), mappingException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Mapping Error")
                .message(mappingException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(CapacityFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleCapacityFullException(CapacityFullException capacityFullException, ServerWebExchange exchange) {
        log.error("CapacityFullException: {}", capacityFullException.getMessage(), capacityFullException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(capacityFullException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleIllegalArgumentException(IllegalArgumentException illegalArgumentException, ServerWebExchange exchange) {
        log.error("IllegalArgumentException: {}", illegalArgumentException.getMessage(), illegalArgumentException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(illegalArgumentException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleIllegalStateException(IllegalStateException illegalStateException, ServerWebExchange exchange) {
        log.error("IllegalStateException: {}", illegalStateException.getMessage(), illegalStateException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(illegalStateException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleMalformedJwtException(MalformedJwtException malformedJwtException, ServerWebExchange exchange) {
        log.error("MalformedJwtException: {}", malformedJwtException.getMessage(), malformedJwtException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(malformedJwtException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleExpiredJwtException(ExpiredJwtException expiredJwtException, ServerWebExchange exchange) {
        log.error("ExpiredJwtException: {}", expiredJwtException.getMessage(), expiredJwtException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(expiredJwtException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleUnsupportedJwtException(UnsupportedJwtException unsupportedJwtException, ServerWebExchange exchange) {
        log.error("UnsupportedJwtException: {}", unsupportedJwtException.getMessage(), unsupportedJwtException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(unsupportedJwtException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleSignatureException(SignatureException signatureException, ServerWebExchange exchange) {
        log.error("SignatureException: {}", signatureException.getMessage(), signatureException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(signatureException.getMessage())
                .path(path)
                .build());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleJwtException(JwtException jwtException, ServerWebExchange exchange) {
        log.error("JwtException: {}", jwtException.getMessage(), jwtException);
        String path = exchange.getRequest().getPath().toString();
        return Mono.just(GlobalErrorMessage.builder()
                .title("Bad Request")
                .message(jwtException.getMessage())
                .path(path)
                .build());
    }
}