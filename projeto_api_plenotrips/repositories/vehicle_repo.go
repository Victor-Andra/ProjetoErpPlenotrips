package repositories

import (
	"api_projeto_plenotrips/models"
	"database/sql"
)

type VehicleRepository struct {
	db *sql.DB
}

func NewVehicleRepository(db *sql.DB) *VehicleRepository {
	return &VehicleRepository{db: db}
}

func (r *VehicleRepository) FindByPlate(plate string) (*models.Vehicle, error) {
	row := r.db.QueryRow("SELECT id, plate_number, status FROM vehicles WHERE plate_number = $1", plate)
	var vehicle models.Vehicle
	err := row.Scan(&vehicle.ID, &vehicle.Plate, &vehicle.Status)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	return &vehicle, err
}
