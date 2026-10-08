package com.nbsh.commerceapi.support;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc
public abstract class ApiIntegrationTest
        extends PostgresIntegrationTest {
}