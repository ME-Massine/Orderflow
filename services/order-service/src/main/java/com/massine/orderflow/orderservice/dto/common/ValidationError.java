package com.massine.orderflow.orderservice.dto.common;

import java.time.Instant;
import java.util.Map;

public record ValidationError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {}