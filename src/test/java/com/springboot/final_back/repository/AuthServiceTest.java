package com.springboot.final_back.repository;

import com.springboot.final_back.dto.SignupDto;
import com.springboot.final_back.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // 클래스 레벨에 적용
public class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("회원가입(성공) 테스트")
    public void signUp() {
        for (int i = 0; i <= 10; i++) {
            SignupDto signupDto = new SignupDto();
            signupDto.setUserId("user" + i);
            signupDto.setPassword("asdf" + i);
            signupDto.setEmail("test" + i + "@email.com");
            signupDto.setName("실험" + i);
            signupDto.setNickname("nickName" + i);
            authService.signUp(signupDto);
        }
    }

    @Test
    @DisplayName("회원가입(실패) 테스트")
    public void signUpFailed() {
        SignupDto signupDto = new SignupDto();
        signupDto.setUserId("user" + 1);
        signupDto.setPassword("asdf" + 1);
        signupDto.setEmail("test" + 1 + "@email.com");
        signupDto.setName("실험" + 1);
        signupDto.setNickname("nickName" + 1);
        authService.signUp(signupDto);

        SignupDto signupDtoForFail = new SignupDto();
        signupDtoForFail.setUserId("user" + 1);
        signupDtoForFail.setPassword("asdf" + 1);
        signupDtoForFail.setEmail("test" + 1 + "@email.com");
        signupDtoForFail.setName("실험" + 1);
        signupDtoForFail.setNickname("nickName" + 1);
        assertThrows(ResponseStatusException.class, () -> authService.signUp(signupDtoForFail));
    }


}
