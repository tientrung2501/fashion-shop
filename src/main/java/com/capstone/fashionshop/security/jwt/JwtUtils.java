package com.capstone.fashionshop.security.jwt;

import com.capstone.fashionshop.models.entities.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;


import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String JWT_SECRET;
    @Value("${app.jwt.expired}")
    private long JWT_EXPIRATION;
    private final String JWT_HEADER = "Authorization";

    public String getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader(JWT_HEADER);
        if (header != null) return header.split(" ")[1].trim();
        return null;
    }

    public User getUserFromJWT(String token) {
        User user = new User();
        try  {
            String subject = Jwts.parser().setSigningKey(JWT_SECRET.getBytes()).parseClaimsJws(token).getBody().getSubject();
            String[] jwtSubject = subject.split(",");

            user.setId(jwtSubject[0]);
            user.setEmail(jwtSubject[1]);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }
        return user;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET.getBytes()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String generateTokenFromUserId(User user) {
        return Jwts.builder()
                .setSubject(String.format("%s,%s", user.getId(), user.getEmail()))
                .setIssuedAt(new Date())
                .setIssuer("FashionShop")
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET.getBytes())
                .compact();
    }

}
