package com.study.shop.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.order.dto.CreateOrderItemRequestDto;
import com.study.shop.domain.order.dto.CreateOrderRequestDto;
import com.study.shop.domain.order.repository.OrderRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL    = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testUser";

    private Member savedMember;
    private Item savedItem;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(Member.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .nickname(TEST_NICKNAME)
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .role(RoleType.USER)
                .build());

        savedItem = itemRepository.save(Item.create(savedMember, "테스트 상품", "테스트 설명", 10, 100000, false));
    }

    private String loginAndGetAccessToken() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(TEST_EMAIL, TEST_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private CreateOrderRequestDto buildOrderRequest(Long itemId, int quantity, String address) {
        return CreateOrderRequestDto.builder()
                .orderItems(List.of(CreateOrderItemRequestDto.builder()
                        .itemId(itemId)
                        .quantity(quantity)
                        .build()))
                .address(address)
                .build();
    }

    private Long createTestOrder(String token, Long itemId, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrderRequest(itemId, quantity, "테스트 배송지"))))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderId")).longValue();
    }

    // ─────────────────────────────────────────────
    // POST /api/order
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/order - 주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("정상 생성 성공 - 응답 필드 및 재고 감소 검증")
        void createOrderSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            int orderQuantity = 3;

            mockMvc.perform(post("/api/order")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOrderRequest(savedItem.getId(), orderQuantity, "서울시 강남구"))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").isNumber())
                    .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                    .andExpect(jsonPath("$.data.address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.memberEmail").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.orderItemDtoList.length()").value(1));

            // 재고 감소 검증
            Item refreshed = itemRepository.findById(savedItem.getId()).orElseThrow();
            assertThat(refreshed.getStock()).isEqualTo(10 - orderQuantity);
        }

        @Test
        @DisplayName("다수 상품 주문 성공 - orderItems 개수 검증")
        void createOrderSuccess_MultipleItems() throws Exception {
            String token = loginAndGetAccessToken();
            Item anotherItem = itemRepository.save(Item.create(savedMember, "두 번째 상품", "설명", 5, 50000, true));

            CreateOrderRequestDto request = CreateOrderRequestDto.builder()
                    .orderItems(List.of(
                            CreateOrderItemRequestDto.builder().itemId(savedItem.getId()).quantity(2).build(),
                            CreateOrderItemRequestDto.builder().itemId(anotherItem.getId()).quantity(1).build()
                    ))
                    .address("부산시 해운대구")
                    .build();

            mockMvc.perform(post("/api/order")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderItemDtoList.length()").value(2))
                    .andExpect(jsonPath("$.data.address").value("부산시 해운대구"));
        }

        @Test
        @DisplayName("재고 부족 시 409 반환")
        void createOrderFail_StockNotEnough() throws Exception {
            String token = loginAndGetAccessToken();
            // 재고(10)보다 많은 수량 주문
            mockMvc.perform(post("/api/order")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOrderRequest(savedItem.getId(), 99, "주소"))))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("존재하지 않는 상품 주문 시 404 반환")
        void createOrderFail_ItemNotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(post("/api/order")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOrderRequest(99999L, 1, "주소"))))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 생성 시 401 반환")
        void createOrderFail_NoAuth() throws Exception {
            mockMvc.perform(post("/api/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildOrderRequest(savedItem.getId(), 1, "주소"))))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/order/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/order/{id} - 주문 단건 조회")
    class GetOrderById {

        @Test
        @DisplayName("정상 단건 조회 성공 - 응답 필드 검증")
        void getOrderByIdSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 2);

            mockMvc.perform(get("/api/order/{id}", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                    .andExpect(jsonPath("$.data.memberEmail").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.orderItemDtoList.length()").value(1));
        }

        @Test
        @DisplayName("타인의 주문 조회 시 403 반환")
        void getOrderByIdFail_NotOwner() throws Exception {
            // 다른 회원의 주문 생성
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Item otherItem = itemRepository.save(Item.create(otherMember, "타인 상품", "설명", 5, 50000, false));

            LoginRequestDto otherLogin = new LoginRequestDto("other@example.com", "password123");
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otherLogin)))
                    .andExpect(status().isOk()).andReturn();
            String otherToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");

            Long otherOrderId = createTestOrder(otherToken, otherItem.getId(), 1);

            // 본인(TEST_EMAIL) 토큰으로 타인의 주문 조회
            String token = loginAndGetAccessToken();
            mockMvc.perform(get("/api/order/{id}", otherOrderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 반환")
        void getOrderByIdFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/order/{id}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/order/status/{status}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/order/status/{status} - 상태별 주문 조회")
    class GetOrdersByStatus {

        @Test
        @DisplayName("PENDING 상태 주문 조회 성공 - 본인 주문만 반환")
        void getOrdersByStatusSuccess_Pending() throws Exception {
            String token = loginAndGetAccessToken();
            createTestOrder(token, savedItem.getId(), 1);
            createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(get("/api/order/status/{status}", "PENDING")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING"));
        }

        @Test
        @DisplayName("다른 상태의 주문은 결과에서 제외됨")
        void getOrdersByStatusSuccess_OnlyMatchingStatus() throws Exception {
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            // 주문 수락 → ORDERED 상태로 전환
            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // PENDING으로 조회 시 ORDERED로 전환된 주문은 제외됨
            mockMvc.perform(get("/api/order/status/{status}", "PENDING")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void getOrdersByStatusFail_NoAuth() throws Exception {
            mockMvc.perform(get("/api/order/status/{status}", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/order/{id}/accept|delivery|complete
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH - 주문 상태 전환")
    class OrderStatusFlow {

        @Test
        @DisplayName("PENDING → ORDERED 수락 성공")
        void acceptOrder_Success() throws Exception {
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("주문이 수락되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("ORDERED"));
        }

        @Test
        @DisplayName("ORDERED → IN_DELIVERY 배송 시작 성공")
        void startDelivery_Success() throws Exception {
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/api/order/{id}/delivery", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("배송이 시작되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("IN_DELIVERY"));
        }

        @Test
        @DisplayName("IN_DELIVERY → COMPLETED 배송 완료 성공")
        void completeOrder_Success() throws Exception {
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mockMvc.perform(patch("/api/order/{id}/delivery", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/api/order/{id}/complete", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("주문이 완료되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("잘못된 상태에서 수락 시 500 반환 (IllegalStateException - GlobalExceptionHandler 미등록)")
        void acceptOrder_Fail_InvalidState() throws Exception {
            // ORDERED 상태에서 재수락 시도 → Order.accept()가 IllegalStateException 발생
            // GlobalExceptionHandler에 IllegalStateException 핸들러가 없으므로 500 반환
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("타인의 주문 수락 시 403 반환")
        void acceptOrder_Fail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Item otherItem = itemRepository.save(Item.create(otherMember, "타인 상품", "설명", 5, 50000, false));

            LoginRequestDto otherLogin = new LoginRequestDto("other@example.com", "password123");
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otherLogin)))
                    .andExpect(status().isOk()).andReturn();
            String otherToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
            Long otherOrderId = createTestOrder(otherToken, otherItem.getId(), 1);

            String token = loginAndGetAccessToken();
            mockMvc.perform(patch("/api/order/{id}/accept", otherOrderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 주문 수락 시 404 반환")
        void acceptOrder_Fail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(patch("/api/order/{id}/accept", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/order/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/order/{id} - 주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("정상 취소 성공 - 재고 복원 검증")
        void cancelOrderSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            int orderQuantity = 3;
            Long orderId = createTestOrder(token, savedItem.getId(), orderQuantity);

            // 주문 생성 후 재고 감소 확인 (10 - 3 = 7)
            assertThat(itemRepository.findById(savedItem.getId()).orElseThrow().getStock())
                    .isEqualTo(10 - orderQuantity);

            mockMvc.perform(delete("/api/order/{id}", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."))
                    .andExpect(jsonPath("$.data").value(orderId));

            // 취소 후 재고 복원 확인 (7 + 3 = 10)
            assertThat(itemRepository.findById(savedItem.getId()).orElseThrow().getStock())
                    .isEqualTo(10);
        }

        @Test
        @DisplayName("PENDING 아닌 상태에서 취소 시 500 반환 (IllegalStateException - GlobalExceptionHandler 미등록)")
        void cancelOrderFail_NotPending() throws Exception {
            // ORDERED 상태에서 취소 시도 → Order.cancel()이 IllegalStateException 발생
            // GlobalExceptionHandler에 IllegalStateException 핸들러가 없으므로 500 반환
            String token = loginAndGetAccessToken();
            Long orderId = createTestOrder(token, savedItem.getId(), 1);

            mockMvc.perform(patch("/api/order/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/order/{id}", orderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("타인의 주문 취소 시 403 반환")
        void cancelOrderFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Item otherItem = itemRepository.save(Item.create(otherMember, "타인 상품", "설명", 5, 50000, false));

            LoginRequestDto otherLogin = new LoginRequestDto("other@example.com", "password123");
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(otherLogin)))
                    .andExpect(status().isOk()).andReturn();
            String otherToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
            Long otherOrderId = createTestOrder(otherToken, otherItem.getId(), 1);

            String token = loginAndGetAccessToken();
            mockMvc.perform(delete("/api/order/{id}", otherOrderId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 404 반환")
        void cancelOrderFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/order/{id}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
