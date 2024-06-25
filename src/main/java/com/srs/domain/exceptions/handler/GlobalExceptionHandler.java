package com.srs.domain.exceptions.handler;

import com.srs.domain.exceptions.CapacityFullException;
import com.srs.domain.models.GlobalErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CapacityFullException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Mono<GlobalErrorMessage> handleCapacityFullException(CapacityFullException capacityFullException, ServerHttpRequest request) {
        log.error("CapacityFullException: {}", capacityFullException.getMessage());
        String path = String.valueOf(request.getPath());
        return Mono.just(GlobalErrorMessage.builder().title("Bad Request").message(capacityFullException.getMessage()).path(path).build());
    }
}
