// handlers/assignment_handler.go
package handlers

import (
	"api_projeto_plenotrips/models"
	"api_projeto_plenotrips/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

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
