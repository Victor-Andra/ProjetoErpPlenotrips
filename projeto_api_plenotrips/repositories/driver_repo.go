package repositories

import (
	"api_projeto_plenotrips/models"
	"database/sql"

	"github.com/google/uuid"
)

type DriverRepository struct {
	db *sql.DB // ← Recebe a conexão injetada
}

func NewDriverRepository(db *sql.DB) *DriverRepository {
	return &DriverRepository{db: db}
}

func (r *DriverRepository) FindByID(id uuid.UUID) (*models.Driver, error) {
	row := r.db.QueryRow("SELECT id, status FROM drivers WHERE id = $1", id)
	var driver models.Driver
	err := row.Scan(&driver.ID, &driver.Status)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	return &driver, err
}
