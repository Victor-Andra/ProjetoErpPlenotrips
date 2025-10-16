package handlers

import (
	"api_projeto_plenotrips/models"
	"api_projeto_plenotrips/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

// Notify godoc
// @Summary Notifica nova atribuição de entrega
// @Description Cria uma nova atribuição após validar motorista, veículo e notas fiscais
// @Tags assignments
// @Accept json
// @Produce json
// @Param request body models.AssignmentRequest true "Dados da atribuição"
// @Success 202 {object} map[string]string "Atribuição criada com sucesso"
// @Failure 400 {object} map[string]string "JSON malformado"
// @Failure 401 {object} map[string]string "Token JWT inválido"
// @Failure 422 {object} map[string]string "Validação de negócio falhou"
// @Failure 500 {object} map[string]string "Erro interno do servidor"
// @Router /v1/assignments/notify [post]
// @Security BearerAuth

type AssignmentHandler struct {
	service *services.AssignmentService
}

func NewAssignmentHandler(service *services.AssignmentService) *AssignmentHandler {
	return &AssignmentHandler{service: service}
}

// ✅ Correção: Use *gin.Context em vez de http.ResponseWriter/Request
func (h *AssignmentHandler) Notify(c *gin.Context) {
	// Validação de JSON
	var req models.AssignmentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error":   "Bad Request",
			"message": "JSON malformado",
		})
		return
	}

	// Validações de negócio + persistência
	if err := h.service.ValidateAndCreate(req); err != nil {
		c.JSON(http.StatusUnprocessableEntity, gin.H{
			"error":   "Unprocessable Entity",
			"message": err.Error(),
		})
		return
	}

	// Sucesso
	c.JSON(http.StatusAccepted, gin.H{
		"message": "Atribuição criada com sucesso",
	})
}
