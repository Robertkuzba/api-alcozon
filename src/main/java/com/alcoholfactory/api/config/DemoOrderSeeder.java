package com.alcoholfactory.api.config;

import com.alcoholfactory.api.common.domain.DeliveryStatus;
import com.alcoholfactory.api.common.domain.OrderStatus;
import com.alcoholfactory.api.common.domain.UserRole;
import com.alcoholfactory.api.modules.delivery.domain.Delivery;
import com.alcoholfactory.api.modules.delivery.repository.DeliveryRepository;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.order.domain.CustomerOrder;
import com.alcoholfactory.api.modules.order.domain.OrderDeliveryDetails;
import com.alcoholfactory.api.modules.order.domain.OrderItem;
import com.alcoholfactory.api.modules.order.repository.CustomerOrderRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
/**
 * Dane demo dla mobilki / desktopu (Michał, Bartek).
 * Idempotentne: pomija seed, gdy istnieje zamówienie {@code 701101}.
 */
@Component
@Profile("!test")
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DemoOrderSeeder implements CommandLineRunner {

    private static final String SEED_CHECK_NUMBER = "701101";
    private static final String MICHAL_SEED_CHECK_NUMBER = "701112";
    private static final String CUSTOMER_EMAIL = "customer@example.com";
    private static final String CUSTOMER_PASSWORD = "Customer123!";
    private static final String EMPLOYEE_EMAIL = "employee@example.com";
    private static final String MICHAL_EMAIL = "michal.nocun@studenci.collegiumwitelona.pl";

    private final CustomerOrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!orderRepository.existsByClientOrderNumber(SEED_CHECK_NUMBER)) {
            seedPrimaryDemoOrders();
        }
        if (!orderRepository.existsByClientOrderNumber(MICHAL_SEED_CHECK_NUMBER)) {
            seedMichalDemoOrders();
        }
    }

    private void seedPrimaryDemoOrders() {
        User customer = ensureCustomer();
        User employee = userRepository.findByEmail(EMPLOYEE_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Brak " + EMPLOYEE_EMAIL + " — uruchom DataInitializer (manager/employee)"));
        Product product = resolveSeedProduct();

        Instant base = Instant.now().minus(3, ChronoUnit.DAYS);

        createOrder(customer, product, "701101", OrderStatus.SUBMITTED, null, null,
                address("Anna Kowalska", "ul. Oławska 15", "Wrocław", "50-123"),
                base, null);

        createOrder(customer, product, "701102", OrderStatus.IN_PRODUCTION, null, null,
                address("Piotr Nowak", "ul. Kazimierza Wielkiego 27", "Wrocław", "50-077"),
                base.plus(2, ChronoUnit.HOURS), null);

        createOrder(customer, product, "701103", OrderStatus.IN_PACKING, null, null,
                address("Maria Wiśniewska", "ul. Hubska 52", "Wrocław", "50-502"),
                base.plus(5, ChronoUnit.HOURS), null);

        // IN_DELIVERY — przypisane do employee@example.com
        createOrder(customer, product, "701104", OrderStatus.IN_DELIVERY, employee, DeliveryStatus.ASSIGNED,
                address("Tomasz Zieliński", "Cesarzowicka 100", "Wrocław", "52-408",
                        "Dom jednorodzinny, dzwonek „Zieliński”"),
                base.plus(1, ChronoUnit.DAYS), null);

        // IN_DELIVERY — ten sam adres, bez kuriera (desktop: do przypisania)
        createOrder(customer, product, "701105", OrderStatus.IN_DELIVERY, null, DeliveryStatus.PENDING,
                address("Katarzyna Zielińska", "Cesarzowicka 100", "Wrocław", "52-408",
                        "Mieszkanie 2 — proszę dzwonić przed dostawą"),
                base.plus(1, ChronoUnit.DAYS).plus(15, ChronoUnit.MINUTES), null);

        createOrder(customer, product, "701106", OrderStatus.IN_DELIVERY, employee, DeliveryStatus.ASSIGNED,
                address("Jan Lewandowski", "ul. Świdnicka 12", "Wrocław", "50-068"),
                base.plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES), null);

        createOrder(customer, product, "701107", OrderStatus.IN_DELIVERY, null, DeliveryStatus.PENDING,
                address("Ewa Dąbrowska", "ul. Legnicka 58", "Wrocław", "54-204"),
                base.plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES), null);

        createOrder(customer, product, "701108", OrderStatus.IN_DELIVERY, employee, DeliveryStatus.ASSIGNED,
                address("Michał Wójcik", "ul. Borowska 11", "Wrocław", "50-558"),
                base.plus(2, ChronoUnit.DAYS), null);

        createOrder(customer, product, "701109", OrderStatus.IN_DELIVERY, null, DeliveryStatus.PENDING,
                address("Agnieszka Kamińska", "ul. Grabiszyńska 241", "Wrocław", "53-235"),
                base.plus(2, ChronoUnit.DAYS).plus(20, ChronoUnit.MINUTES), null);

        Instant deliveredAt = base.plus(3, ChronoUnit.DAYS);
        createOrder(customer, product, "701110", OrderStatus.DELIVERED, employee, DeliveryStatus.DELIVERED,
                address("Robert Szymański", "ul. Klecińska 4", "Wrocław", "52-315"),
                base.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS), deliveredAt);

        createOrder(customer, product, "701111", OrderStatus.CANCELLED, null, null,
                address("Klient Anulowany", "ul. Na Grobli 20", "Wrocław", "50-421"),
                base.plus(4, ChronoUnit.HOURS), null);

        log.info("""
                Seeded demo orders (client_order_number 701101–701111):
                  SUBMITTED=701101, IN_PRODUCTION=701102, IN_PACKING=701103,
                  IN_DELIVERY assigned (employee): 701104, 701106, 701108,
                  IN_DELIVERY unassigned: 701105, 701107, 701109 (701104+701105: Cesarzowicka 100),
                  DELIVERED=701110, CANCELLED=701111.
                Customer: {} / {}
                """, CUSTOMER_EMAIL, CUSTOMER_PASSWORD);
    }

    /** Zamówienia pod testy kuriera Michała (idempotentne po numerze 701112). */
    private void seedMichalDemoOrders() {
        User customer = ensureCustomer();
        User michal = userRepository.findByEmail(MICHAL_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Brak " + MICHAL_EMAIL + " — uruchom DataInitializer"));
        Product product = resolveSeedProduct();
        Instant now = Instant.now();

        createOrder(customer, product, "701112", OrderStatus.IN_DELIVERY, null, DeliveryStatus.PENDING,
                address("Nowe — bez kuriera", "ul. Piłsudskiego 74", "Wrocław", "50-371",
                        "Demo: IN_DELIVERY, courierId=null — desktop PATCH /api/deliveries/…/assign"),
                now.minus(30, ChronoUnit.MINUTES), null);

        createOrder(customer, product, "701113", OrderStatus.IN_DELIVERY, michal, DeliveryStatus.ASSIGNED,
                address("Michał Nocuń (kurier)", "ul. Powstańców Śląskich 95", "Wrocław", "53-332",
                        "Demo IN_DELIVERY — kurier: " + MICHAL_EMAIL),
                now.minus(15, ChronoUnit.MINUTES), null);

        log.info("""
                Seeded Michał demo orders: 701112=IN_DELIVERY (bez kuriera),
                701113=IN_DELIVERY assigned to {}.
                Kurier: GET /api/orders/for-courier/{{michalUserId}} → 701113.
                Desktop: GET /api/deliveries → 701112 (assign → Michał = push FCM).
                """, MICHAL_EMAIL);
    }

    private User ensureCustomer() {
        return userRepository.findByEmail(CUSTOMER_EMAIL).orElseGet(() -> userRepository.save(
                User.builder()
                        .email(CUSTOMER_EMAIL)
                        .passwordHash(passwordEncoder.encode(CUSTOMER_PASSWORD))
                        .role(UserRole.CUSTOMER)
                        .active(true)
                        .courier(false)
                        .ageConfirmedAt(Instant.now())
                        .build()
        ));
    }

    private Product resolveSeedProduct() {
        return productStockRepository.findAll().stream()
                .filter(s -> s.getQuantity() > 0)
                .findFirst()
                .map(ProductStock::getProduct)
                .orElseGet(() -> productRepository.findAll().stream()
                        .filter(Product::isActive)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Brak produktu do seedu zamówień")));
    }

    private void createOrder(
            User customer,
            Product product,
            String clientOrderNumber,
            OrderStatus status,
            User courier,
            DeliveryStatus deliveryStatus,
            OrderDeliveryDetails deliveryDetails,
            Instant createdAt,
            Instant deliveredAt
    ) {
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(2));
        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .status(status)
                .clientOrderNumber(clientOrderNumber)
                .deliveryDetails(deliveryDetails)
                .totalAmount(lineTotal)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .deliveredAt(deliveredAt)
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(product.getPrice())
                .build();
        order.getItems().add(item);
        orderRepository.save(order);

        if (status == OrderStatus.IN_DELIVERY || status == OrderStatus.DELIVERED) {
            Delivery delivery = Delivery.builder()
                    .order(order)
                    .clientOrderNumber(clientOrderNumber)
                    .courier(courier)
                    .status(deliveryStatus != null ? deliveryStatus : DeliveryStatus.PENDING)
                    .deliveryDetails(OrderDeliveryDetails.copyOf(deliveryDetails))
                    .startedAt(deliveryStatus == DeliveryStatus.IN_TRANSIT ? createdAt.plus(1, ChronoUnit.HOURS) : null)
                    .deliveredAt(deliveredAt)
                    .build();
            deliveryRepository.save(delivery);
        }
    }

    private static OrderDeliveryDetails address(
            String recipient,
            String street,
            String city,
            String postalCode
    ) {
        return address(recipient, street, city, postalCode, null);
    }

    private static OrderDeliveryDetails address(
            String recipient,
            String street,
            String city,
            String postalCode,
            String notes
    ) {
        return OrderDeliveryDetails.builder()
                .recipientName(recipient)
                .streetAddress(street)
                .city(city)
                .postalCode(postalCode)
                .country("Polska")
                .deliveryNotes(notes)
                .paymentMethod("Płatność przy odbiorze")
                .build();
    }
}
