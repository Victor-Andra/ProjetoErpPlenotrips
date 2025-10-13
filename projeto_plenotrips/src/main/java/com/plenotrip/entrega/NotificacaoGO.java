package com.plenotrip.entrega;

import java.util.List;
import java.util.UUID;

public class NotificacaoGO {
    private UUID driverId;
    private String vehiclePlate;
    private String routeDescription;
    private List<InvoiceItem> invoices;

    // Getters e Setters
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getRouteDescription() { return routeDescription; }
    public void setRouteDescription(String routeDescription) { this.routeDescription = routeDescription; }

    public List<InvoiceItem> getInvoices() { return invoices; }
    public void setInvoices(List<InvoiceItem> invoices) { this.invoices = invoices; }

    // Classe interna para itens de fatura
    public static class InvoiceItem {
        private String key; // Chave de acesso de 44 dígitos

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }
}