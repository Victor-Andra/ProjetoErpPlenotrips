package com.plenotrip.nucleo.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "e37460ef8bb6eefcf36827cf0305836a";
   
    // Caso precise alterar issuer.
    public static String gerarToken(String issuer, long expirationMillis) {
        return Jwts.builder()
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    
    public static String gerarToken() {
	    try {
	        return Jwts.builder()
	            .setIssuer("erp-legacy-system")
	            .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
	            .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes(StandardCharsets.UTF_8))//Se não converter em base64 da erro devido ao tipo e tamanho da chave.
	            .compact();
	
	    } catch (Exception exception) {
	        System.err.println("Erro ao gerar o token JWT: " + exception.getMessage());
	        return null;
	    }
    }
}