package com.alcoholfactory.api.auth;

import com.alcoholfactory.api.modules.auth.service.TemporaryPasswordGenerator;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class TemporaryPasswordGeneratorTest {

    @RepeatedTest(20)
    void generate_meetsMinimumLength() {
        assertThat(TemporaryPasswordGenerator.generate()).hasSizeGreaterThanOrEqualTo(8);
        assertThat(TemporaryPasswordGenerator.generate(8)).hasSize(8);
    }
}
