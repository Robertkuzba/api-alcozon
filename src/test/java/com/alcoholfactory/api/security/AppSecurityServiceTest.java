package com.alcoholfactory.api.security;

import com.alcoholfactory.api.config.AppSecurityProperties;
import com.alcoholfactory.api.modules.security.dto.AppCheckRequest;
import com.alcoholfactory.api.modules.security.service.AppSecurityService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

class AppSecurityServiceTest {

    private static final String DEBUG_SHA =
            "e1b17830399a952b8ff905023d5dc98f0a202cbb18941beb06000717341ac7f6";

    @Test
    void isAllowed_whenAndroidConfigNull_usesDefaultsAndAcceptsDebugSha() {
        var service = new AppSecurityService(new AppSecurityProperties(null));
        var request = new AppCheckRequest(
                "android",
                "com.alkozon.app",
                "1.0.0",
                1,
                DEBUG_SHA
        );
        assertThatCode(() -> service.verify(request)).doesNotThrowAnyException();
    }

    @Test
    void verify_whenPackageNameNullInConfig_doesNotThrow() {
        var service = new AppSecurityService(new AppSecurityProperties(
                new AppSecurityProperties.Android(null, 1, List.of(DEBUG_SHA))
        ));
        var request = new AppCheckRequest("android", "com.alkozon.app", "1.0.0", 1, DEBUG_SHA);
        assertThatCode(() -> service.verify(request)).doesNotThrowAnyException();
    }
}
