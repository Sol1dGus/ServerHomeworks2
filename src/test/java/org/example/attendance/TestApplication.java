package org.example.attendance;

import org.example.AttendanceApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Тестовый конфиг: подтягивает основной @SpringBootApplication, не создавая второй.
 */
@Configuration
@Import(AttendanceApplication.class)
public class TestApplication {
}
