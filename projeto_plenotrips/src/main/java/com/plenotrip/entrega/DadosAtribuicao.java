package com.plenotrip.entrega;

import java.util.UUID;

public class DadosAtribuicao {
    private UUID driverId;
    private String vehiclePlate;
    private String routeDescription;
    private String[] invoiceKeys; // Recebe invoiceKeys[0], invoiceKeys[1], etc.

    // Getters e Setters
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getRouteDescription() { return routeDescription; }
    public void setRouteDescription(String routeDescription) { this.routeDescription = routeDescription; }

    public String[] getInvoiceKeys() { return invoiceKeys; }
    public void setInvoiceKeys(String[] invoiceKeys) { this.invoiceKeys = invoiceKeys; }
}