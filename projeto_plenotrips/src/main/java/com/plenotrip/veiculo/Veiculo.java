package com.plenotrip.veiculo;

import java.util.Date;
import java.util.UUID;

public class Veiculo {
    private UUID id;
    private String plateNumber;
    private String model;
    private String status; 
    private Date createdAt;
    private Date updatedAt;

    public Veiculo(){}
    
    public Veiculo(String plateNumber, String model, String status) {
        this.setPlateNumber(plateNumber);
        this.setModel(model);
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

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getPlateNumber() {
		return plateNumber;
	}

	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}
