package models

import "github.com/google/uuid"

type AssignmentRequest struct {
	DriverID         uuid.UUID     `json:"driverId"`
	VehiclePlate     string        `json:"vehiclePlate"`
	RouteDescription string        `json:"routeDescription"`
	Invoices         []InvoiceItem `json:"invoices"`
}

type InvoiceItem struct {
	Key string `json:"key"`
}

type Driver struct {
	ID     uuid.UUID
	Status string
}

type Vehicle struct {
	ID     uuid.UUID
	Plate  string
	Status string
}
