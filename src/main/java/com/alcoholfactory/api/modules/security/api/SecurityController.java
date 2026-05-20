package com.alcoholfactory.api.modules.security.api;

import com.alcoholfactory.api.modules.security.dto.AppCheckRequest;
import com.alcoholfactory.api.modules.security.service.AppSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
@RequiredArgsConstructor
@Tag(name = "Security")
public class SecurityController {

    private final AppSecurityService appSecurityService;

    @PostMapping("/app-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Weryfikacja podpisu / pakietu aplikacji mobilnej (Android)")
    public void appCheck(@Valid @RequestBody AppCheckRequest request) {
        appSecurityService.verify(request);
    }
}
