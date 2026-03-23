package com.alcoholfactory.api.modules.order.service;

import com.alcoholfactory.api.common.domain.CustomOrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.order.domain.CustomOrder;
import com.alcoholfactory.api.modules.order.dto.CreateCustomOrderRequest;
import com.alcoholfactory.api.modules.order.dto.CustomOrderResponse;
import com.alcoholfactory.api.modules.order.repository.CustomOrderRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomOrderService {

    private final CustomOrderRepository customOrderRepository;
    private final UserRepository userRepository;

    @Transactional
    public CustomOrderResponse create(Long userId, CreateCustomOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != UserRole.CUSTOMER || user.getAgeConfirmedAt() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Wymagane CUSTOMER z potwierdzonym wiekiem");
        }
        CustomOrder co = CustomOrder.builder()
                .customer(user)
                .description(req.description())
                .preferences(req.preferences())
                .status(CustomOrderStatus.PENDING)
                .build();
        customOrderRepository.save(co);
        return toResponse(co);
    }

    @Transactional(readOnly = true)
    public List<CustomOrderResponse> my(Long userId) {
        return customOrderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomOrderResponse> listForStaff() {
        return customOrderRepository.findAllWithUsers().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomOrderResponse get(Long id, Long currentUserId, boolean staff) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        if (!staff && !co.getCustomer().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Brak dostępu");
        }
        return toResponse(co);
    }

    @Transactional
    public CustomOrderResponse patchStatus(Long id, CustomOrderStatus status) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        co.setStatus(status);
        return toResponse(customOrderRepository.save(co));
    }

    @Transactional
    public CustomOrderResponse assign(Long id, Long assigneeId) {
        CustomOrder co = customOrderRepository.findFetchedById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid assignee"));
        co.setAssignedTo(assignee);
        return toResponse(customOrderRepository.save(co));
    }

    private CustomOrderResponse toResponse(CustomOrder co) {
        return new CustomOrderResponse(
                co.getId(),
                co.getCustomer().getId(),
                co.getDescription(),
                co.getPreferences(),
                co.getStatus(),
                co.getAssignedTo() != null ? co.getAssignedTo().getId() : null,
                co.getCreatedAt(),
                co.getUpdatedAt()
        );
    }
}
