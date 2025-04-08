

package com.MobilePrepaidRecharge.app.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;

public class JWTUtils {

    private static final String SECRET_KEY = "YourSuperSecretKeyYourSuperSecretKey"; // Must be at least 256 bits
    private static final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    public static String generateToken(String identifier, String role) {
        return Jwts.builder()
                   .setSubject(identifier)
                   .claim("role", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                   .signWith(KEY, SignatureAlgorithm.HS256)
                   .compact();
    }
    
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(KEY)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
}

