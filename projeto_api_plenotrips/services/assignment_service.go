package services

import (
	"api_projeto_plenotrips/models"
	"api_projeto_plenotrips/repositories"
	"database/sql"
	"errors"
	"fmt"
	"regexp"
)

type AssignmentService struct {
	driverRepo     *repositories.DriverRepository
	vehicleRepo    *repositories.VehicleRepository
	assignmentRepo *repositories.AssignmentRepository
	db             *sql.DB // ← Mantém para transações
}

func NewAssignmentService(db *sql.DB) *AssignmentService {
	return &AssignmentService{
		driverRepo:     repositories.NewDriverRepository(db),
		vehicleRepo:    repositories.NewVehicleRepository(db),
		assignmentRepo: repositories.NewAssignmentRepository(db),
		db:             db,
	}
}

func (s *AssignmentService) ValidateAndCreate(req models.AssignmentRequest) error {
	// Validação 1: Motorista existe e está AVAILABLE
	driver, err := s.driverRepo.FindByID(req.DriverID)
	if err != nil {
		return fmt.Errorf("erro ao buscar motorista: %v", err)
	}
	if driver == nil {
		return errors.New("motorista não encontrado")
	}
	if driver.Status != "AVAILABLE" {
		return errors.New("motorista não está disponível")
	}

	// Validação 2: Veículo existe e está OPERATIONAL
	vehicle, err := s.vehicleRepo.FindByPlate(req.VehiclePlate)
	if err != nil {
		return fmt.Errorf("erro ao buscar veículo: %v", err)
	}
	if vehicle == nil {
		return errors.New("veículo não encontrado")
	}
	if vehicle.Status != "OPERATIONAL" {
		return errors.New("veículo não está operacional")
	}

	// Validação 3: Notas fiscais
	for _, inv := range req.Invoices {
		if len(inv.Key) != 44 {
			return errors.New("chave de acesso da NF-e deve ter 44 dígitos")
		}
		if !regexp.MustCompile(`^\d{44}$`).MatchString(inv.Key) {
			return errors.New("chave de acesso deve conter apenas dígitos")
		}
		// Modelo está nos dígitos 21-22 (índices 20-21 em Go)
		modelo := inv.Key[20:22]
		if modelo != "55" {
			return errors.New("modelo da NF-e deve ser 55")
		}
	}

	// Transação de banco de dados
	tx, err := s.db.Begin()
	if err != nil {
		return fmt.Errorf("erro ao iniciar transação: %v", err)
	}
	defer tx.Rollback()

	// Atualiza status do motorista e veículo
	if err := s.assignmentRepo.UpdateDriverStatus(tx, req.DriverID, "IN_TRANSIT"); err != nil {
		return fmt.Errorf("erro ao atualizar status do motorista: %v", err)
	}
	if err := s.assignmentRepo.UpdateVehicleStatus(tx, vehicle.ID, "IN_TRANSIT"); err != nil {
		return fmt.Errorf("erro ao atualizar status do veículo: %v", err)
	}

	// Cria atribuição
	assignmentID, err := s.assignmentRepo.CreateAssignment(tx, req.DriverID, vehicle.ID, req.RouteDescription)
	if err != nil {
		return fmt.Errorf("erro ao criar atribuição: %v", err)
	}

	// Cria notas fiscais
	for _, inv := range req.Invoices {
		if err := s.assignmentRepo.CreateInvoice(tx, assignmentID, inv.Key); err != nil {
			return fmt.Errorf("erro ao criar nota fiscal: %v", err)
		}
	}

	// Confirma transação
	if err := tx.Commit(); err != nil {
		return fmt.Errorf("erro ao confirmar transação: %v", err)
	}

	return nil
}
