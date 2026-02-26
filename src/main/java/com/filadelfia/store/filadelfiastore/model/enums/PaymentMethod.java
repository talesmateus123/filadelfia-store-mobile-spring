package com.filadelfia.store.filadelfiastore.model.enums;

/**
 * Enum representing different payment methods available in the system
 */
public enum PaymentMethod {
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    PIX("PIX"),
    BOLETO("Boleto Bancário"),
    BANK_TRANSFER("Transferência Bancária"),
    CASH("Dinheiro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if payment method requires online processing
     */
    public boolean requiresOnlineProcessing() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PIX;
    }

    /**
     * Check if payment method is instant
     */
    public boolean requiresInstantPayment() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PIX || this == CASH;
    }

    /**
     * Check if payment method requires manual confirmation
     */
    public boolean requiresPaymentConfirmation() {
        return this == BOLETO || this == BANK_TRANSFER;
    }

    /**
     * Get payment method description for customers
     */
    public String getDescription() {
        switch (this) {
            case CREDIT_CARD:
                return "Pagamento com cartão de crédito - Aprovação instantânea";
            case DEBIT_CARD:
                return "Pagamento com cartão de débito - Aprovação instantânea";
            case BOLETO:
                return "Boleto bancário - Prazo de 1-3 dias úteis para compensação";
            case PIX:
                return "PIX - Pagamento instantâneo 24h por dia";
            case BANK_TRANSFER:
                return "Transferência bancária - Confirmação manual";
            case CASH:
                return "Pagamento em dinheiro - Na entrega";
            default:
                return displayName;
        }
    }

    /**
     * Get estimated processing time in hours
     */
    public int getEstimatedProcessingHours() {
        switch (this) {
            case CREDIT_CARD:
            case DEBIT_CARD:
            case PIX:
            case CASH:
                return 0; // Instant
            case BOLETO:
                return 72; // 3 days
            case BANK_TRANSFER:
                return 24; // 1 day
            default:
                return 0;
        }
    }
}
