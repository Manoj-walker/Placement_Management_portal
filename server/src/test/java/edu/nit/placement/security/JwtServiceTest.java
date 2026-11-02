package edu.nit.placement.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import edu.nit.placement.model.user.Student;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "test-secret-test-secret-test-secret-test-secret-123456", 1);

    @Test
    void generatesAndParsesToken() {
        Student student = new Student();
        student.setId(42L);
        student.setEmail("student@nit.edu");
        student.setCreatedAt(LocalDateTime.now());

        String token = jwtService.generate(student);
        Claims claims = jwtService.parse(token);

        assertEquals("42", claims.getSubject());
        assertEquals("STUDENT", claims.get("role"));
        assertEquals("student@nit.edu", claims.get("email"));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        Student student = new Student();
        student.setId(1L);
        student.setEmail("a@nit.edu");
        student.setCreatedAt(LocalDateTime.now());
        String token = jwtService.generate(student);

        JwtService other = new JwtService(
                "another-secret-another-secret-another-secret-abcdef", 1);

        assertThrows(JwtException.class, () -> other.parse(token));
    }
}
