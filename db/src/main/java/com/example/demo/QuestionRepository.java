package com.example.demo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Integer>{
	Optional<Question> findByUsername(String username);
	//@Query("SELECT u.username FROM Question u WHERE u.username != :username ORDER BY FUNCTION('RAND') LIMIT 1")
	@Query("SELECT u.username FROM Question u ORDER BY FUNCTION('RAND') LIMIT 1")
    String findRandomUsername(@Param("username") String username);
}
