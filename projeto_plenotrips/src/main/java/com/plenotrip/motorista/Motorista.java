package com.plenotrip.motorista;

import java.util.Date;
import java.util.UUID;

public class Motorista {
    private UUID id;
    private String name;
    private String licenseNumber;
    private String status; // "AVAILABLE", "IN_TRANSIT", "OFF_DUTY"
    private Date createdAt;
    private Date updatedAt;

    public Motorista() {}

    public Motorista(String name, String licenseNumber, String status) {
        this.setName(name);
        this.setLicenseNumber(licenseNumber);
        this.setStatus(status);
    }

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
