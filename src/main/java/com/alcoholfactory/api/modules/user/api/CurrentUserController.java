package com.alcoholfactory.api.modules.user.api;

import com.alcoholfactory.api.modules.user.dto.UserMeResponse;
import com.alcoholfactory.api.modules.user.service.UserProfileService;
import com.alcoholfactory.api.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class CurrentUserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public UserMeResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return userProfileService.getFor(principal.getId());
    }
}
