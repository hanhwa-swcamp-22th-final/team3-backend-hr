package com.ohgiraffers.team3backendhr.hr.command.domain.aggregate.quantitativeevaluation;

public enum QuantEvalStatus {
    TEMPORARY,
    CONFIRMED,

    /**
     * Legacy aliases kept only so existing rows from older batch runs can still be read safely.
     */
    PREVIEW,
    SETTLED;

    public static QuantEvalStatus fromBatchStatus(String status, boolean hasTScore) {
        if (status == null || status.isBlank()) {
            return hasTScore ? CONFIRMED : TEMPORARY;
        }

        return switch (status.trim().toUpperCase()) {
            case "CONFIRMED", "SETTLED" -> CONFIRMED;
            case "TEMPORARY", "PREVIEW" -> TEMPORARY;
            default -> hasTScore ? CONFIRMED : TEMPORARY;
        };
    }
}
