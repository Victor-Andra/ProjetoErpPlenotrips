package com.plenotrip.notaFiscal;

import java.util.Date;
import java.util.UUID;

public class NotaFiscal {
	private UUID id;
    private UUID deliveryAssignmentId;
    private String invoiceNumber;
    private String invoiceDetails;
    private Date createdAt;

    public NotaFiscal() {}

    public NotaFiscal(UUID deliveryAssignmentId, String invoiceNumber, String invoiceDetails) {
        this.setDeliveryAssignmentId(deliveryAssignmentId);
        this.setInvoiceNumber(invoiceNumber);
        this.setInvoiceDetails(invoiceDetails);
    }

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getInvoiceDetails() {
		return invoiceDetails;
	}

	public void setInvoiceDetails(String invoiceDetails) {
		this.invoiceDetails = invoiceDetails;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public UUID getDeliveryAssignmentId() {
		return deliveryAssignmentId;
	}

	public void setDeliveryAssignmentId(UUID deliveryAssignmentId) {
		this.deliveryAssignmentId = deliveryAssignmentId;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
