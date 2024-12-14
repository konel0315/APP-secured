package com.example.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Optional;

public class JwtT {

    private static final String SECRET_KEY = "202235440_JES"; // 서명에 사용할 키
    private static final long EXPIRATION_TIME = 1000 * 60 * 30; // 30분 (밀리초 단위)

    /**
     * JWT 토큰 생성
     *
     * @param username 사용자 이름
     * @return 생성된 JWT 토큰
     */
    public static String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return JWT.create()
                .withSubject(username)          // 토큰의 주체 (사용자 이름)
                .withIssuedAt(now)              // 발급 시간
                .withExpiresAt(expiryDate)      // 만료 시간
                .sign(Algorithm.HMAC256(SECRET_KEY)); // HMAC256 알고리즘으로 서명
    }

    /**
     * JWT 토큰 검증
     *
     * @param token 클라이언트가 제공한 JWT 토큰
     * @return 유효한 경우 사용자 이름, 유효하지 않은 경우 Optional.empty()
     */
    public static Optional<String> verifyToken(String token) {
        try {
            // JWT 생성시 사용한 비밀 키로 서명을 검증
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm).build();

            // 토큰 검증
            DecodedJWT decodedJWT = verifier.verify(token);

            // 검증 성공 시 사용자 이름 반환
            return Optional.of(decodedJWT.getSubject());
        } catch (JWTVerificationException exception) {
            // 서명 검증 실패 등
            System.out.println("Invalid token: " + exception.getMessage());
            return Optional.empty();  // 토큰이 유효하지 않음
        }
    }
}
