package auth

import (
	"errors"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

const (
	SecretKey = "e37460ef8bb6eefcf36827cf0305836a" // 32 chars = 128 bits (compatível com Java)
	Issuer    = "erp-legacy-system"
)

// Claims personalizados (opcional, mas recomendado)
type Claims struct {
	jwt.RegisteredClaims
}

// ValidateToken valida o token JWT recebido
func ValidateToken(tokenString string) error {
	if tokenString == "" {
		return errors.New("token ausente")
	}

	// Remove "Bearer " do início
	tokenString = strings.TrimPrefix(tokenString, "Bearer ")

	// Parse e valida o token
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		// Verifica se o algoritmo é HS256
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("algoritmo de assinatura inválido")
		}
		return []byte(SecretKey), nil
	})

	if err != nil {
		return err
	}

	if !token.Valid {
		return errors.New("token inválido")
	}

	// Verifica o issuer
	if claims, ok := token.Claims.(*Claims); ok {
		if claims.Issuer != Issuer {
			return errors.New("issuer inválido")
		}
	} else {
		return errors.New("claims inválidos")
	}

	return nil
}

// GenerateToken gera um token JWT válido (para testes)
func GenerateToken() (string, error) {
	claims := Claims{
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    Issuer,
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(24 * time.Hour)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(SecretKey))
}

/*
// auth/jwt.go
package auth

import (
	"errors"
	"github.com/dgrijalva/jwt-go"
	"strconv"
	"strings"
)

const SecretKeyHex = "e37460ef8bb6eefcf36827cf0305836a"

// Converte hex para bytes
func hexToBytes(hexStr string) []byte {
	bytes := make([]byte, len(hexStr)/2)
	for i := 0; i < len(hexStr); i += 2 {
		byteValue, _ := strconv.ParseInt(hexStr[i:i+2], 16, 64)
		bytes[i/2] = byte(byteValue)
	}
	return bytes
}

func ValidateToken(tokenString string) error {
	if tokenString == "" {
		return errors.New("token ausente")
	}

	tokenString = strings.TrimPrefix(tokenString, "Bearer ")

	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("algoritmo inválido")
		}
		return hexToBytes(SecretKeyHex), nil // ← Usa 128 bits diretamente
	})

	if err != nil {
		return err
	}

	if !token.Valid {
		return errors.New("token inválido")
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok {
		if iss, exists := claims["iss"]; !exists || iss != "erp-legacy-system" {
			return errors.New("issuer inválido")
		}
	} else {
		return errors.New("claims inválidos")
	}

	return nil
}
*/
