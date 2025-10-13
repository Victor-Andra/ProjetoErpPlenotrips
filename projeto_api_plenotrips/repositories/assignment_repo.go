package repositories

import (
	"database/sql"

	"github.com/google/uuid"
)

type AssignmentRepository struct {
	db *sql.DB
}

func NewAssignmentRepository(db *sql.DB) *AssignmentRepository {
	return &AssignmentRepository{db: db}
}

func (r *AssignmentRepository) CreateAssignment(tx *sql.Tx, driverID, vehicleID uuid.UUID, routeDesc string) (uuid.UUID, error) {
	var id uuid.UUID
	err := tx.QueryRow(
		"INSERT INTO delivery_assignments (route_description, driver_id, vehicle_id, status) VALUES ($1, $2, $3, 'ASSIGNED') RETURNING id",
		routeDesc, driverID, vehicleID,
	).Scan(&id)
	return id, err
}

func (r *AssignmentRepository) CreateInvoice(tx *sql.Tx, assignmentID uuid.UUID, key string) error {
	_, err := tx.Exec(
		"INSERT INTO delivery_invoices (delivery_assignment_id, invoice_number, invoice_details) VALUES ($1, $2, '{}')",
		assignmentID, key,
	)
	return err
}

func (r *AssignmentRepository) UpdateDriverStatus(tx *sql.Tx, driverID uuid.UUID, status string) error {
	_, err := tx.Exec("UPDATE drivers SET status = $1 WHERE id = $2", status, driverID)
	return err
}

func (r *AssignmentRepository) UpdateVehicleStatus(tx *sql.Tx, vehicleID uuid.UUID, status string) error {
	_, err := tx.Exec("UPDATE vehicles SET status = $1 WHERE id = $2", status, vehicleID)
	return err
}
