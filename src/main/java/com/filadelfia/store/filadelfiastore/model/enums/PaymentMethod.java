package com.filadelfia.store.filadelfiastore.model.enums;

public enum PaymentMethod {
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    PIX("PIX"),
    BOLETO("Boleto Bancário"),
    CASH("Dinheiro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresInstantPayment() {
        return this == CREDIT_CARD || this == DEBIT_CARD || this == PIX || this == CASH;
    }

    public boolean requiresPaymentConfirmation() {
        return this == BOLETO;
    }
}
