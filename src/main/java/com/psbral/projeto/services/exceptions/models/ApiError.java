package com.psbral.projeto.services.exceptions.models;

import java.time.Instant;

public record ApiError (
    Instant timestamp,
    int value,
    String message,
    String error,
    String path
) {}
