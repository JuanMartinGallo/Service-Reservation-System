package com.srs.infrastructure.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class ErrorWebExceptionHandlerConfig {

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(List<ViewResolver> viewResolvers) {
        return (exchange, ex) -> {
            if (ex instanceof ResponseStatusException responseStatusException) {
                if (responseStatusException.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.TEXT_HTML)
                            .render("errors/404")
                            .flatMap(response -> response.writeTo(exchange, new DefaultContext(viewResolvers)));
                }
            }
            // Handle other exceptions and render generic error page
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_HTML)
                    .render("errors/error")
                    .flatMap(response -> response.writeTo(exchange, new DefaultContext(viewResolvers)))
                    .onErrorResume(e -> Mono.error(ex));
        };
    }

    @Bean
    public RouterFunction<ServerResponse> errorRoute() {
        return RouterFunctions.route()
                .GET("/errors", this::renderErrorPage)
                .build();
    }

    private Mono<ServerResponse> renderErrorPage(ServerRequest request) {
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .render("errors/404");
    }

    private record DefaultContext(List<ViewResolver> viewResolvers) implements ServerResponse.Context {

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return HandlerStrategies.withDefaults().messageWriters();
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return viewResolvers;
        }
    }
}
