package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Encoder {

    @Autowired
    private QuestionRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 암호화 인스턴스 생성

    // 회원가입 처리 메서드
    public Question registerUser(String username, String rawPassword) {
        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // 사용자 객체 생성 및 저장
        Question user = new Question();
        user.setUsername(username);
        user.setPassword(hashedPassword); // 해싱된 비밀번호 저장
        return user;
    }//

    // 비밀번호 검증 메서드
    public boolean authenticate(String username, String rawPassword) {
        // 데이터베이스에서 사용자 조회
        Question user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // 입력된 비밀번호와 저장된 비밀번호 해시 검증
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
