package com.prgrms.be02slack.security;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

  public String createLoginToken(String email) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put("type", TokenType.Login.name());
    Date now = new Date();

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .setExpiration(new Date(now.getTime() + jwtConfig.getTokenExpirationMsec()))
        .signWith(SignatureAlgorithm.HS512, jwtConfig.getTokenSecret())
        .compact();
  }

  public String createMemberToken(String email, String encodedWorkspaceId) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put("type", TokenType.Member.name());
    claims.put("encodedWorkspaceId", encodedWorkspaceId);
    Date now = new Date();

    return Jwts.builder()
        .setClaims(claims)
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

  public String getTypeFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(jwtConfig.getTokenSecret())
        .parseClaimsJws(token)
        .getBody()
        .get("type").toString();
  }

  public String getEncodedWorkspaceIdFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(jwtConfig.getTokenSecret())
        .parseClaimsJws(token)
        .getBody()
        .get("encodedWorkspaceId").toString();
  }

  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
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
