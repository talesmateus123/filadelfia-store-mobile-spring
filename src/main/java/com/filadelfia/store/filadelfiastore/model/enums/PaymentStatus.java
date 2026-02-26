package com.filadelfia.store.filadelfiastore.model.enums;

/**
 * Enum representing different payment status states
 */
public enum PaymentStatus {
    PENDING("Pendente", "Payment is awaiting processing"),
    PROCESSING("Processando", "Payment is being processed"),
    AUTHORIZED("Autorizado", "Payment has been authorized but not captured"),
    CAPTURED("Capturado", "Payment has been captured successfully"),
    CONFIRMED("Confirmado", "Payment has been confirmed"),
    CANCELLED("Cancelado", "Payment has been cancelled"),
    FAILED("Falhou", "Payment has failed"),
    REFUNDED("Reembolsado", "Payment has been refunded"),
    EXPIRED("Expirado", "Payment has expired"),
    REJECTED("Rejeitado", "Payment has been rejected");

    private final String displayName;
    private final String description;

    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if payment is in a final state (cannot be changed)
     */
    public boolean isFinalState() {
        return this == CONFIRMED || this == CANCELLED || this == FAILED || 
               this == REFUNDED || this == EXPIRED || this == REJECTED;
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return this == CONFIRMED || this == CAPTURED;
    }

    /**
     * Check if payment is pending or in progress
     */
    public boolean isInProgress() {
        return this == PENDING || this == PROCESSING || this == AUTHORIZED;
    }

    /**
     * Check if payment has failed
     */
    public boolean hasFailed() {
        return this == FAILED || this == CANCELLED || this == EXPIRED || this == REJECTED;
    }

    /**
     * Get next possible statuses from current status
     */
    public PaymentStatus[] getPossibleNextStatuses() {
        switch (this) {
            case PENDING:
                return new PaymentStatus[]{PROCESSING, CANCELLED, EXPIRED};
            case PROCESSING:
                return new PaymentStatus[]{AUTHORIZED, CONFIRMED, FAILED, REJECTED};
            case AUTHORIZED:
                return new PaymentStatus[]{CAPTURED, CANCELLED};
            case CAPTURED:
                return new PaymentStatus[]{CONFIRMED, REFUNDED};
            case CONFIRMED:
                return new PaymentStatus[]{REFUNDED};
            default:
                return new PaymentStatus[]{};
        }
    }

    /**
     * Get CSS class for status display
     */
    public String getCssClass() {
        switch (this) {
            case PENDING:
            case PROCESSING:
            case AUTHORIZED:
                return "badge-warning";
            case CAPTURED:
            case CONFIRMED:
                return "badge-success";
            case CANCELLED:
            case FAILED:
            case EXPIRED:
            case REJECTED:
                return "badge-danger";
            case REFUNDED:
                return "badge-info";
            default:
                return "badge-secondary";
        }
    }
}
