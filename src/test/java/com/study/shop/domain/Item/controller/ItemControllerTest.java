package com.study.shop.domain.Item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.Item.dto.CreateItemRequestDto;
import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.ItemStatus;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL    = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testUser";

    private Member savedMember;

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

    private Item createTestItem(String name, int price, boolean used) {
        return itemRepository.save(Item.create(savedMember, name, "테스트 설명", 1, price, used));
    }

    // ─────────────────────────────────────────────
    // POST /api/items
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/items - 상품 생성")
    class CreateItem {

        @Test
        @DisplayName("정상 생성 성공 - DB 반영 및 초기 상태 ON_SALE 검증")
        void createItemSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            CreateItemRequestDto request = CreateItemRequestDto.builder()
                    .name("아이바네즈 기타")
                    .description("상태 양호")
                    .stock(1)
                    .price(350000)
                    .used(true)
                    .build();

            MvcResult result = mockMvc.perform(post("/api/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andReturn();

            Long itemId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Item saved = itemRepository.findById(itemId).orElseThrow();
            assertThat(saved.getName()).isEqualTo("아이바네즈 기타");
            assertThat(saved.getPrice()).isEqualTo(350000);
            assertThat(saved.isUsed()).isTrue();
            assertThat(saved.getItemStatus()).isEqualTo(ItemStatus.ON_SALE);
            assertThat(saved.getSeller().getId()).isEqualTo(savedMember.getId());
        }

        @Test
        @DisplayName("상품명 빈 값으로 생성 시 400 반환")
        void createItemFail_BlankName() throws Exception {
            String token = loginAndGetAccessToken();
            CreateItemRequestDto request = CreateItemRequestDto.builder()
                    .name("")
                    .price(10000)
                    .used(false)
                    .build();

            mockMvc.perform(post("/api/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("음수 가격으로 생성 시 400 반환")
        void createItemFail_NegativePrice() throws Exception {
            String token = loginAndGetAccessToken();
            CreateItemRequestDto request = CreateItemRequestDto.builder()
                    .name("기타")
                    .price(-1)
                    .used(false)
                    .build();

            mockMvc.perform(post("/api/items")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증 없이 생성 시 401 반환")
        void createItemFail_NoAuth() throws Exception {
            CreateItemRequestDto request = CreateItemRequestDto.builder()
                    .name("기타")
                    .price(10000)
                    .used(false)
                    .build();

            mockMvc.perform(post("/api/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/items/all
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/items/all - 전체 상품 조회")
    class GetAllItems {

        @Test
        @DisplayName("상품 없을 때 빈 배열 반환")
        void getAllItemsSuccess_Empty() throws Exception {
            mockMvc.perform(get("/api/items/all"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("전체 조회 성공 - 상품 수 검증")
        void getAllItemsSuccess() throws Exception {
            createTestItem("드럼", 500000, false);
            createTestItem("베이스 기타", 300000, true);

            mockMvc.perform(get("/api/items/all"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/items
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/items - 상품 목록 검색 (커서 기반 페이징)")
    class SearchItems {

        @Test
        @DisplayName("조건 없이 전체 조회 - Slice 응답 검증")
        void searchItemsSuccess_All() throws Exception {
            createTestItem("기타A", 100000, false);
            createTestItem("기타B", 200000, true);

            mockMvc.perform(get("/api/items"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.hasNext").value(false));
        }

        @Test
        @DisplayName("상품명 검색 조건으로 필터링 성공")
        void searchItemsSuccess_ByContent() throws Exception {
            createTestItem("펜더 스트라토캐스터", 800000, false);
            createTestItem("마샬 앰프", 400000, false);

            mockMvc.perform(get("/api/items")
                            .param("content", "펜더"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value("펜더 스트라토캐스터"));
        }

        @Test
        @DisplayName("가격 범위 검색 필터링 성공")
        void searchItemsSuccess_ByPriceRange() throws Exception {
            createTestItem("저가 상품", 50000, false);
            createTestItem("중가 상품", 200000, false);
            createTestItem("고가 상품", 900000, false);

            mockMvc.perform(get("/api/items")
                            .param("minPrice", "100000")
                            .param("maxPrice", "500000"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].name").value("중가 상품"));
        }

        @Test
        @DisplayName("중고 여부 조건으로 필터링 성공")
        void searchItemsSuccess_ByUsed() throws Exception {
            createTestItem("새 상품", 100000, false);
            createTestItem("중고 상품A", 50000, true);
            createTestItem("중고 상품B", 80000, true);

            mockMvc.perform(get("/api/items")
                            .param("used", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/items/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/items/{id} - 상품 단건 조회")
    class GetItem {

        @Test
        @DisplayName("정상 조회 성공 - 응답 필드 검증 (seller = email)")
        void getItemSuccess() throws Exception {
            Item item = createTestItem("일렉 기타", 250000, true);

            mockMvc.perform(get("/api/items/{id}", item.getId()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(item.getId()))
                    .andExpect(jsonPath("$.data.name").value("일렉 기타"))
                    .andExpect(jsonPath("$.data.price").value(250000))
                    .andExpect(jsonPath("$.data.used").value(true))
                    .andExpect(jsonPath("$.data.status").value("ON_SALE"))
                    .andExpect(jsonPath("$.data.seller").value(TEST_EMAIL));
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void getItemFail_NotFound() throws Exception {
            mockMvc.perform(get("/api/items/{id}", 99999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/items/me
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/items/me - 내 상품 조회")
    class GetMyItems {

        @Test
        @DisplayName("정상 조회 성공 - 본인 상품만 반환")
        void getMyItemsSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            createTestItem("내 상품A", 100000, false);
            createTestItem("내 상품B", 200000, true);

            // 다른 회원의 상품도 등록
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            itemRepository.save(Item.create(otherMember, "타인의 상품", "설명", 1, 300000, false));

            mockMvc.perform(get("/api/items/me")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("등록 상품 없을 때 빈 배열 반환")
        void getMyItemsSuccess_Empty() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/items/me")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void getMyItemsFail_NoAuth() throws Exception {
            mockMvc.perform(get("/api/items/me"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // PUT /api/items/{itemId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /api/items/{itemId} - 상품 수정")
    class UpdateItem {

        @Test
        @DisplayName("정상 수정 성공 - DB 반영 검증")
        void updateItemSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Item item = createTestItem("수정 전 이름", 100000, false);
            UpdateItemRequestDto request = new UpdateItemRequestDto("수정 후 이름", "수정된 설명", 200000, true, ItemStatus.ON_SALE);

            mockMvc.perform(put("/api/items/{itemId}", item.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("상품이 수정되었습니다."));

            Item updated = itemRepository.findById(item.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("수정 후 이름");
            assertThat(updated.getPrice()).isEqualTo(200000);
            assertThat(updated.isUsed()).isTrue();
        }

        @Test
        @DisplayName("타인의 상품 수정 시 403 반환")
        void updateItemFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Item otherItem = itemRepository.save(Item.create(otherMember, "타인의 상품", "설명", 1, 100000, false));

            String token = loginAndGetAccessToken();
            UpdateItemRequestDto request = new UpdateItemRequestDto("탈취", "탈취", 1, false, ItemStatus.ON_SALE);

            mockMvc.perform(put("/api/items/{itemId}", otherItem.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ON_SALE 아닌 상품 수정 시 403 반환")
        void updateItemFail_NotOnSale() throws Exception {
            String token = loginAndGetAccessToken();
            Item item = createTestItem("기타", 100000, false);

            // 먼저 RESERVED로 상태 변경
            UpdateItemRequestDto reserveRequest = new UpdateItemRequestDto("기타", "설명", 100000, false, ItemStatus.RESERVED);
            mockMvc.perform(put("/api/items/{itemId}", item.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveRequest)))
                    .andExpect(status().isOk());

            // RESERVED 상태에서 수정 재시도 → 403
            mockMvc.perform(put("/api/items/{itemId}", item.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveRequest)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 상품 수정 시 404 반환")
        void updateItemFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();
            UpdateItemRequestDto request = new UpdateItemRequestDto("수정", "설명", 10000, false, ItemStatus.ON_SALE);

            mockMvc.perform(put("/api/items/{itemId}", 99999L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 수정 시 401 반환")
        void updateItemFail_NoAuth() throws Exception {
            Item item = createTestItem("기타", 100000, false);
            UpdateItemRequestDto request = new UpdateItemRequestDto("수정", "설명", 10000, false, ItemStatus.ON_SALE);

            mockMvc.perform(put("/api/items/{itemId}", item.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/items/{itemId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/items/{itemId} - 상품 삭제")
    class DeleteItem {

        @Test
        @DisplayName("정상 삭제 성공 - soft delete로 조회 불가 검증")
        void deleteItemSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Item item = createTestItem("삭제 대상 상품", 100000, false);
            Long itemId = item.getId();

            mockMvc.perform(delete("/api/items/{itemId}", itemId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."));

            // @SQLRestriction("deleted_at is NULL") 적용 → 삭제 후 조회 불가
            assertThat(itemRepository.findById(itemId)).isEmpty();
        }

        @Test
        @DisplayName("타인의 상품 삭제 시 403 반환")
        void deleteItemFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Item otherItem = itemRepository.save(Item.create(otherMember, "타인의 상품", "설명", 1, 100000, false));

            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/items/{itemId}", otherItem.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ON_SALE 아닌 상품 삭제 시 403 반환")
        void deleteItemFail_NotOnSale() throws Exception {
            String token = loginAndGetAccessToken();
            Item item = createTestItem("기타", 100000, false);

            // RESERVED로 상태 변경 후 삭제 시도
            UpdateItemRequestDto reserveRequest = new UpdateItemRequestDto("기타", "설명", 100000, false, ItemStatus.RESERVED);
            mockMvc.perform(put("/api/items/{itemId}", item.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/items/{itemId}", item.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 상품 삭제 시 404 반환")
        void deleteItemFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/items/{itemId}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 삭제 시 401 반환")
        void deleteItemFail_NoAuth() throws Exception {
            Item item = createTestItem("기타", 100000, false);

            mockMvc.perform(delete("/api/items/{itemId}", item.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}