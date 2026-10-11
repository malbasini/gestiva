package com.gestiva.support;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractMySqlIntegrationTest {
}