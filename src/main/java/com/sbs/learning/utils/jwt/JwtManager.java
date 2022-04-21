package com.sbs.learning.utils.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;

public class JwtManager {
	private static long rememberMe = 24;
	public static final long JWT_TOKEN_VALIDITY = rememberMe * 60 * 60;
	private String secretKey = "Q+QNEFrpG4wxVX08A5R6NUz1UQq9C0vr6Zoy877RUog=";
	private final Key KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	
	public static void setRemember(long days) {
		rememberMe = days;
	}
	
	public Payload getJwt(String token) {
		return getAllClaims(token).get("user", Payload.class);
	}
	
	public String getSubject(String token) {
		return getClaim(token, Claims::getSubject);
	}
	
	//retorna os dados do token(payload)
	private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	//retorna tudo relacionado ao token.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Claims getAllClaims(String token) {
		Jws<Claims> jws;
		
		try {
			jws = Jwts.parserBuilder()
			.deserializeJsonWith(new JacksonDeserializer(Maps.of("user", Payload.class).build()))
			.setSigningKey(KEY)
			.build()
			.parseClaimsJws(token);
			return jws.getBody();
		} catch (JwtException e) {
				throw new RuntimeException(e);
		}
		
	}
	
	//gera uma key
	public String generateKey() {
		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		return Encoders.BASE64.encode(key.getEncoded());
	}
	
	//filtro para geração de token.
	public String generateToken(Map<String, Object> claims, String subject) {
		return this.doToken(claims, subject);
	}
	
	//gerá o token
	private String doToken(Map<String, Object> claims, String subject) {
		
		return Jwts.builder()
				.setClaims(claims)
				.setIssuer("http://localhost:8080")
				.setSubject(subject)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(KEY)
				.compact();
	}
	
	//verifica se já expirou
	public Boolean isExpired(String token) {
		final Date expiration = this.getClaim(token, Claims::getExpiration);
		return expiration.before(new Date());
	}
	
	//verifica se base com a base de dados ou dado estatico.
	public Boolean validateToken(String token, String dbUsername) {
		final Payload user = this.getJwt(token);
		if(user.getUsername().equals(dbUsername)) {
			return true;
		}else {
			return false;
		}
	}
}