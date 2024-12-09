package com.example.demo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtT {

    private static final String SECRET_KEY = "202235440_JES"; // 서명에 사용할 키
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간 (밀리초 단위)

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
    public static String verifyToken(String token) {
        try {
            // JWT 생성시 사용한 비밀 키로 서명을 검증
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();  // JWT 검증기 생성

            // 토큰 검증
            DecodedJWT decodedJWT = verifier.verify(token);

            // 토큰이 유효하면 디코딩된 JWT 객체를 반환합니다
            String k=decodedJWT.getSubject();

            return k;  // 토큰이 유효함
        } catch (JWTVerificationException exception) {
            // 서명 검증 실패 등
            System.out.println("Invalid token: " + exception.getMessage());
            return exception.getMessage();  // 토큰이 유효하지 않음
        }
    }
}
