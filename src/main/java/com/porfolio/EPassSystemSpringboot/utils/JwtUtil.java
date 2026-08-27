package com.porfolio.EPassSystemSpringboot.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;


    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private final long expirationTime = 1000 * 60 * 60; //1 hour

    public String generateToken(String username) {

        return Jwts
                .builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    //get details from jwt token
    private Claims getPayload(String token) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsernameFromToken(String token) {
        return getPayload(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return getPayload(token).getExpiration().before(new Date());
    }


    public boolean validateToken(String token, String usernameFromToken, UserDetails userDetails) {
        //TODO - check if username is same as username in user details
        //TODO - check if token is not expired

        return usernameFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }


}













