package org.example.attendance.service;

import io.jsonwebtoken.Claims;
import org.example.attendance.dto.auth.*;
import org.example.attendance.exception.BadRequestException;
import org.example.attendance.exception.ConflictException;
import org.example.attendance.exception.NotFoundException;
import org.example.attendance.model.*;
import org.example.attendance.repository.*;
import org.example.attendance.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       TeacherRepository teacherRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public TokenResponse login(LoginRequest request) {
        log.info("Попытка входа: username='{}'", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

            log.info("Успешный вход: username='{}', role={}", user.getUsername(), user.getRole());

            return new TokenResponse(accessToken, refreshToken, user.getRole().name(), user.getId());
        } catch (AuthenticationException e) {
            log.warn("Ошибка аутентификации: username='{}', error={}", request.getUsername(), e.getMessage());
            throw new BadRequestException("Неверный логин или пароль");
        }
    }

    public TokenResponse register(RegisterRequest request) {
        log.info("Регистрация нового пользователя: username='{}', role='{}'", request.getUsername(), request.getRole());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Пользователь с логином '" + request.getUsername() + "' уже существует");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректная роль: '" + request.getRole() + "'. Допустимые: STUDENT, TEACHER, ADMIN");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        if (role == Role.STUDENT) {
            if (request.getStudentId() == null) {
                throw new BadRequestException("Для роли STUDENT необходимо указать studentId");
            }
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new NotFoundException("Студент с id=" + request.getStudentId() + " не найден"));
            if (userRepository.existsByStudentId(request.getStudentId())) {
                throw new ConflictException("Для данного студента уже создан аккаунт");
            }
            user.setStudent(student);
        } else if (role == Role.TEACHER) {
            if (request.getTeacherId() == null) {
                throw new BadRequestException("Для роли TEACHER необходимо указать teacherId");
            }
            Teacher teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Преподаватель с id=" + request.getTeacherId() + " не найден"));
            if (userRepository.existsByTeacherId(request.getTeacherId())) {
                throw new ConflictException("Для данного преподавателя уже создан аккаунт");
            }
            user.setTeacher(teacher);
        }

        User saved = userRepository.save(user);
        log.info("Пользователь зарегистрирован: userId={}, username='{}', role={}",
                saved.getId(), saved.getUsername(), saved.getRole());

        String accessToken = jwtTokenProvider.generateAccessToken(saved.getId(), saved.getUsername(), saved.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getId(), saved.getUsername());

        return new TokenResponse(accessToken, refreshToken, saved.getRole().name(), saved.getId());
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BadRequestException("Недействительный или истёкший refresh token");
        }

        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        String username = claims.getSubject();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        log.info("Токены обновлены: username='{}'", user.getUsername());

        return new TokenResponse(newAccessToken, newRefreshToken, user.getRole().name(), user.getId());
    }
}
