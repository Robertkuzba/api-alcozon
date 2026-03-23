package com.alcoholfactory.api.modules.admin.api;

import com.alcoholfactory.api.modules.admin.dto.PatchUserRequest;
import com.alcoholfactory.api.modules.admin.dto.UserAdminResponse;
import com.alcoholfactory.api.modules.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Admin users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserAdminResponse> list() {
        return adminUserService.list();
    }

    @PutMapping("/{id}")
    public UserAdminResponse update(@PathVariable Long id, @Valid @RequestBody PatchUserRequest req) {
        return adminUserService.update(id, req);
    }

    @PostMapping("/{id}/hire")
    public UserAdminResponse hire(@PathVariable Long id) {
        return adminUserService.hire(id);
    }

    @PostMapping("/{id}/terminate")
    public UserAdminResponse terminate(@PathVariable Long id) {
        return adminUserService.terminate(id);
    }
}
