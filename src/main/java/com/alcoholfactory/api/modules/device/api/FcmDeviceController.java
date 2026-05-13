package com.alcoholfactory.api.modules.device.api;

import com.alcoholfactory.api.modules.device.dto.RegisterFcmTokenRequest;
import com.alcoholfactory.api.modules.device.service.FcmTokenRegistrationService;
import com.alcoholfactory.api.security.AppUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class FcmDeviceController {

    private final FcmTokenRegistrationService fcmTokenRegistrationService;

    @PostMapping("/fcm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerFcm(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody RegisterFcmTokenRequest body
    ) {
        fcmTokenRegistrationService.register(principal.getId(), body.token(), body.platform());
    }
}
