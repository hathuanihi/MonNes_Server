package com.SE104.quan_ly_so_tiet_kiem.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; 
import io.jsonwebtoken.security.Keys; 

import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class JwtService {
    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs; 

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username) { 
        NguoiDung user = nguoiDungRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found for token generation: " + username));
        
        String role = user.getVaiTro() == 0 ? "ADMIN" : "USER"; 
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return buildToken(claims, username, jwtExpirationMs);
    }
    
    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact();
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String usernameInToken = extractUsername(token);
        return (usernameInToken.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
               .setSigningKey(getSigningKey())
               .build()
               .parseClaimsJws(token)
               .getBody();
    }
}