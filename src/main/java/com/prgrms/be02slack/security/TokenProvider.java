package com.prgrms.be02slack.security;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.prgrms.be02slack.common.configuration.security.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Service
public class TokenProvider {

  private final JwtConfig jwtConfig;

  public TokenProvider(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  public String createToken(String email) {
    Date now = new Date();

    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(now.getTime() + jwtConfig.getTokenExpirationMsec()))
        .signWith(SignatureAlgorithm.HS512, jwtConfig.getTokenSecret())
        .compact();
  }

  public String getEmailFromToken(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtConfig.getTokenSecret())
        .parseClaimsJws(token)
        .getBody();

    return claims.getSubject();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtConfig.getTokenSecret()).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      return false;
    } catch (MalformedJwtException ex) {
      return false;
    } catch (ExpiredJwtException ex) {
      return false;
    } catch (UnsupportedJwtException ex) {
      return false;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}
