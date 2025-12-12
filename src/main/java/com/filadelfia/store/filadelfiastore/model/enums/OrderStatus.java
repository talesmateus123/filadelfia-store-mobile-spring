package com.filadelfia.store.filadelfiastore.model.enums;

public enum OrderStatus {
    PENDING("Pendente"),
    CONFIRMED("Confirmado"),
    PROCESSING("Em Processamento"),
    SHIPPED("Enviado"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelado"),
    REFUNDED("Reembolsado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isProcessing() {
        return this == PROCESSING;
    }

    public boolean isShipped() {
        return this == SHIPPED;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean canBeProcessed() {
        return this == CONFIRMED;
    }

    public boolean canBeShipped() {
        return this == PROCESSING;
    }

    public boolean canBeDelivered() {
        return this == SHIPPED;
    }
}
