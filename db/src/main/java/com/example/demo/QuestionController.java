package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question") // 기본 URL
public class QuestionController {
	private final Encoder encoder;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionController(QuestionRepository questionRepository,Encoder encoder) {
        this.questionRepository = questionRepository;
        this.encoder = encoder;
    }

    // 질문 추가 (POST)
    @PostMapping("/sign")
    public int createQuestion(@RequestBody Question question) {
        // 요청 받은 데이터 로그 출력
        System.out.println("Received Question: " + question.getUsername() + ", " + question.getPassword());
         Question user=encoder.registerUser(question.getUsername(), question.getPassword());
        
        questionRepository.save(user); // DB에 저장
        return 0; // 저장된 데이터 반환
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Question question) {
        boolean isAuthenticated = encoder.authenticate(question.getUsername(), question.getPassword());

        if (isAuthenticated) {
            String token = JwtT.generateToken(question.getUsername());
            
            Map<String, String> response = new HashMap<>();
            response.put("username", question.getUsername());
            response.put("token", token);

            return ResponseEntity.ok(response); // 성공 시 JSON 형식으로 반환
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error");
        }
    }

    @PostMapping("/check")  // "/hello" URL로 접근 시 이 메소드가 실행됩니다.
    public int check(@RequestBody Question question) {
    	Optional<Question> k=questionRepository.findByUsername(question.getUsername());
    	if (k.isPresent()) {
            return 1;  
        } else {
            return 0; 
        } 
    }
    
}
