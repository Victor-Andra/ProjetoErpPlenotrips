package main

import (
	"api_projeto_plenotrips/auth"
	"api_projeto_plenotrips/db"
	"api_projeto_plenotrips/handlers"
	"api_projeto_plenotrips/services"

	"log"
	"net/http"

	"github.com/gin-gonic/gin"
)

// @title PlenoTrip ERP Webhook API
// @version 1.0
// @description Serviço para notificação de novas atribuições de entrega com validação JWT e transações atômicas.
// @host localhost:8081
// @BasePath /v1
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description Token JWT no formato "Bearer {token}"

func main() {
	// Conexão com PostgreSQL
	db, err := db.ConnectDB()
	if err != nil {
		log.Fatal("Erro ao conectar ao banco: ", err)
	}
	defer db.Close()

	// Serviços e handlers
	assignmentService := services.NewAssignmentService(db)
	assignmentHandler := handlers.NewAssignmentHandler(assignmentService)

	r := gin.Default()

	// ✅ Servir Swagger UI estático
	//r.Static("/swagger", "./swagger")
	r.Static("/swagger", "./docs/swagger")
	r.StaticFile("/swagger/openapi.yaml", "./docs/openapi.yaml")

	// ✅ Servir sua especificação OpenAPI
	r.Static("/docs", "./docs")

	api := r.Group("/v1/assignments")
	api.Use(authMiddleware())
	{
		api.POST("/notify", assignmentHandler.Notify)
	}

	log.Println("✅ Projeto iniciado na porta 8081!")
	r.Run(":8081")
}

func authMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if err := auth.ValidateToken(authHeader); err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{
				"error":   "Unauthorized",
				"message": "Token JWT inválido ou ausente",
			})
			c.Abort()
			return
		}
		c.Next()
	}
}
