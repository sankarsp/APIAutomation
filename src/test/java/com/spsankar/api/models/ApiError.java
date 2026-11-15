package com.spsankar.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Error body returned by the API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiError(String code, String message) {
}
