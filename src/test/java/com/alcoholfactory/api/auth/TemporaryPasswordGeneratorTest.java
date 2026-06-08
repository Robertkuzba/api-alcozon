package com.alcoholfactory.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.alcoholfactory.api.modules.auth.service.TemporaryPasswordGenerator;
import org.junit.jupiter.api.RepeatedTest;

class TemporaryPasswordGeneratorTest {

  @RepeatedTest(20)
  void generate_meetsMinimumLength() {
    assertThat(TemporaryPasswordGenerator.generate()).hasSizeGreaterThanOrEqualTo(8);
    assertThat(TemporaryPasswordGenerator.generate(8)).hasSize(8);
  }
}
