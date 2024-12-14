package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final Encoder encoder;
    private final QuestionRepository questionRepository;

    private static final String FIXED_TOKEN = "your-fixed-secret-key-12345"; // 고정된 토큰

    @Autowired
    public QuestionController(QuestionRepository questionRepository, Encoder encoder) {
        this.questionRepository = questionRepository;
        this.encoder = encoder;
    }

    // 질문 추가 (POST)
    @PostMapping("/sign")
    public int createQuestion(
        @RequestBody Question question,
        @RequestHeader(value = "Authorization", required = false) String fixedToken
    ) {
        // 1. 고정된 토큰 검증
        if (fixedToken == null || !fixedToken.equals(FIXED_TOKEN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid fixed token");
        }

        // 2. 요청 받은 데이터 로그 출력
        System.out.println("Received Question: " + question.getUsername() + ", " + question.getPassword());

        // 3. 사용자 등록
        Question user = encoder.registerUser(question.getUsername(), question.getPassword());
        questionRepository.save(user); // DB에 저장
        return 0; // 저장 성공 반환
    }

    // 로그인 (POST)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Question question,
                                   @RequestHeader(value = "Authorization", required = false) String fixedToken) {
        // 1. 고정된 토큰 검증
        if (fixedToken == null || !fixedToken.equals(FIXED_TOKEN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid fixed token");
        }

        // 2. 사용자 인증 처리
        boolean isAuthenticated = encoder.authenticate(question.getUsername(), question.getPassword());

        if (isAuthenticated) {
            // 인증이 성공하면 JWT 토큰 생성
            String token = JwtT.generateToken(question.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("username", question.getUsername());
            response.put("token", token);

            return ResponseEntity.ok(response); // 성공 시 JSON 형식으로 반환
        } else {
            // 인증 실패 시 오류 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error");
        }
    }

    // 사용자 인증 후 질문 확인 (POST)
    @PostMapping("/check")
    public int check(
        @RequestBody Question question,
        @RequestHeader(value = "Authorization", required = false) String fixedToken
    ) {
        // 1. 고정된 토큰 검증
        if (fixedToken == null || !fixedToken.equals(FIXED_TOKEN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid fixed token");
        }

        // 3. 유효한 토큰인 경우 DB에서 사용자 확인
        Optional<Question> k = questionRepository.findByUsername(question.getUsername());
        if (k.isPresent()) {
            return 1; // 사용자 존재
        } else {
            return 0; // 사용자 없음
        }
    }

    // 토큰 검증 API (POST)
    @PostMapping("/token")
    public ResponseEntity<String> verifyToken(
            @RequestBody String token,
            @RequestHeader(value = "Authorization", required = false) String fixedToken) {

        // 1. 고정된 Authorization 토큰 검증
        if (fixedToken == null || !fixedToken.equals(FIXED_TOKEN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden"); // 인증 실패
        }

        // 2. 클라이언트로부터 받은 토큰 검증
        Optional<String> usernameOpt = JwtT.verifyToken(token);

        if (usernameOpt.isPresent()) {
            String username = usernameOpt.get(); // 유효한 토큰에서 username을 가져옴
            return ResponseEntity.ok(username); // 토큰 유효하고, username 반환
        } else {
            return ResponseEntity.ok("Invalid token"); // 토큰 무효
        }}}//pproject.duckdns.org
