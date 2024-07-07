package com.srs.domain.exceptions.handler;

import com.srs.domain.exceptions.CapacityFullException;
import com.srs.domain.models.GlobalErrorMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.security.SignatureException;

import static com.srs.domain.utils.ApplicationConstants.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CapacityFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleCapacityFullException(CapacityFullException capacityFullException, ServerHttpRequest request) {
        log.error("CapacityFullException: {}", capacityFullException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(capacityFullException.getMessage()).path(path).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleIllegalArgumentException(IllegalArgumentException illegalArgumentException, ServerHttpRequest request) {
        log.error("IllegalArgumentException: {}", illegalArgumentException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(illegalArgumentException.getMessage()).path(path).build());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleIllegalStateException(IllegalStateException illegalStateException, ServerHttpRequest request) {
        log.error("IllegalStateException: {}", illegalStateException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(illegalStateException.getMessage()).path(path).build());
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleMalformedJwtException(MalformedJwtException malformedJwtException, ServerHttpRequest request) {
        log.error("MalformedJwtException: {}", malformedJwtException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(malformedJwtException.getMessage()).path(path).build());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleExpiredJwtException(ExpiredJwtException expiredJwtException, ServerHttpRequest request) {
        log.error("ExpiredJwtException: {}", expiredJwtException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(expiredJwtException.getMessage()).path(path).build());
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleUnsupportedJwtException(UnsupportedJwtException unsupportedJwtException, ServerHttpRequest request) {
        log.error("UnsupportedJwtException: {}", unsupportedJwtException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(unsupportedJwtException.getMessage()).path(path).build());
    }

    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleSignatureException(SignatureException signatureException, ServerHttpRequest request) {
        log.error("SignatureException: {}", signatureException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(signatureException.getMessage()).path(path).build());
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleJwtException(JwtException jwtException, ServerHttpRequest request) {
        log.error("JwtException: {}", jwtException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title(BAD_REQUEST).message(jwtException.getMessage()).path(path).build());
    }
}
