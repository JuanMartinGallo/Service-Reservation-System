package com.srs.domain.models;

import lombok.Builder;

@Builder
public record GlobalErrorMessage(String title, String message, String path) {

}