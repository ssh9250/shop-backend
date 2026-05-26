package com.study.shop.admin.controller;

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
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderAdminControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL     = "admin@example.com";
    private static final String ADMIN_PASSWORD  = "adminpass123";
    private static final String BUYER_EMAIL     = "buyer@example.com";
    private static final String BUYER_PASSWORD  = "buyerpass123";
    private static final String SELLER_EMAIL    = "seller@example.com";
    private static final String SELLER_PASSWORD = "sellerpass123";

    private Member buyerMember;
    private Item savedItem;

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .nickname("adminUser")
                .address("서울시 강남구")
                .role(RoleType.ADMIN)
                .build());

        buyerMember = memberRepository.save(Member.builder()
                .email(BUYER_EMAIL)
                .password(passwordEncoder.encode(BUYER_PASSWORD))
                .nickname("buyerUser")
                .phone("010-1111-2222")
                .address("서울시 강남구")
                .role(RoleType.USER)
                .build());

        Member sellerMember = memberRepository.save(Member.builder()
                .email(SELLER_EMAIL)
                .password(passwordEncoder.encode(SELLER_PASSWORD))
                .nickname("sellerUser")
                .phone("010-3333-4444")
                .address("서울시 마포구")
                .role(RoleType.USER)
                .build());

        savedItem = itemRepository.save(Item.create(sellerMember, "테스트 상품", "설명", 10, 50000, false));
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private Long createTestOrder(String buyerToken) throws Exception {
        CreateOrderRequestDto req = CreateOrderRequestDto.builder()
                .orderItem(CreateOrderItemRequestDto.builder()
                        .itemId(savedItem.getId())
                        .quantity(1)
                        .build())
                .address("테스트 주소")
                .build();
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data.orderId")).longValue();
    }

    // ─────────────────────────────────────────────
    // GET /admin/orders
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/orders - 전체 주문 조회")
    class GetAllOrders {

        @Test
        @DisplayName("관리자 - 전체 주문 조회 성공")
        void getAllOrdersSuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("주문 없을 때 빈 배열 반환")
        void getAllOrders_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getAllOrdersFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);

            mockMvc.perform(get("/admin/orders")
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 시 401 반환")
        void getAllOrdersFail_NoAuth() throws Exception {
            mockMvc.perform(get("/admin/orders"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/orders/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/orders/{id} - 주문 단건 조회")
    class GetOrderById {

        @Test
        @DisplayName("관리자 - 주문 단건 조회 성공")
        void getOrderByIdSuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andExpect(jsonPath("$.data.orderStatus").value("PENDING"));
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 404 반환")
        void getOrderByIdFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/{id}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getOrderByIdFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);

            mockMvc.perform(get("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/orders/status/{status}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/orders/status/{status} - 상태별 주문 조회")
    class GetOrdersByStatus {

        @Test
        @DisplayName("PENDING 상태 주문 조회 성공")
        void getOrdersByStatusSuccess_Pending() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/status/{status}", "PENDING")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING"));
        }

        @Test
        @DisplayName("해당 상태 주문 없을 때 빈 배열 반환")
        void getOrdersByStatus_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/status/{status}", "COMPLETED")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getOrdersByStatusFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);

            mockMvc.perform(get("/admin/orders/status/{status}", "PENDING")
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/orders/member/{memberId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/orders/member/{memberId} - 회원별 주문 조회")
    class GetOrdersByMember {

        @Test
        @DisplayName("관리자 - 회원별 주문 조회 성공")
        void getOrdersByMemberSuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/member/{memberId}", buyerMember.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("주문 없는 회원 조회 시 빈 배열 반환")
        void getOrdersByMember_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/orders/member/{memberId}", buyerMember.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getOrdersByMemberFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);

            mockMvc.perform(get("/admin/orders/member/{memberId}", buyerMember.getId())
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /admin/orders/{id}/accept|delivery|complete
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH - 관리자 주문 상태 전환")
    class AdminOrderStatusFlow {

        @Test
        @DisplayName("PENDING → ORDERED 수락 성공")
        void acceptOrderSuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("주문이 수락되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("ORDERED"));
        }

        @Test
        @DisplayName("ORDERED → IN_DELIVERY 배송 시작 성공")
        void startDeliverySuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/admin/orders/{id}/delivery", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("배송이 시작되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("IN_DELIVERY"));
        }

        @Test
        @DisplayName("IN_DELIVERY → COMPLETED 배송 완료 성공")
        void completeOrderSuccess() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
            mockMvc.perform(patch("/admin/orders/{id}/delivery", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/admin/orders/{id}/complete", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("주문이 완료되었습니다."))
                    .andExpect(jsonPath("$.data.orderStatus").value("COMPLETED"));
        }

        @Test
        @DisplayName("CANCELLED 상태 주문 수락 시도 시 400 반환")
        void acceptOrderFail_InvalidState() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            // 취소 처리
            mockMvc.perform(delete("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 취소된 주문을 수락 시도 → CANCELLED.next() 호출 → 400
            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 주문 수락 시 404 반환")
        void acceptOrderFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(patch("/admin/orders/{id}/accept", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 상태 전환 시도 시 403 반환")
        void acceptOrderFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);

            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /admin/orders/{id} - 관리자 강제 취소
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /admin/orders/{id} - 관리자 주문 강제 취소")
    class AdminCancelOrder {

        @Test
        @DisplayName("관리자 - PENDING 주문 강제 취소 성공")
        void cancelOrderSuccess_Pending() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."))
                    .andExpect(jsonPath("$.data").value(orderId));
        }

        @Test
        @DisplayName("관리자 - ORDERED 상태 주문 강제 취소 성공")
        void cancelOrderSuccess_Ordered() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(patch("/admin/orders/{id}/accept", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(orderId));
        }

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 404 반환")
        void cancelOrderFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/orders/{id}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void cancelOrderFail_NotAdmin() throws Exception {
            String buyerToken = loginAndGetToken(BUYER_EMAIL, BUYER_PASSWORD);
            Long orderId = createTestOrder(buyerToken);

            mockMvc.perform(delete("/admin/orders/{id}", orderId)
                            .header("Authorization", "Bearer " + buyerToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
