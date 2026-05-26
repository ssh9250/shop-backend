import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// ========== 설정값 ==========
const BASE_URL = 'http://localhost:8080/api';
const TOKEN    = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHJpbmciLCJpYXQiOjE3Nzk3NTY5ODAsImV4cCI6MTc3OTc1ODc4MH0.fy-q20TsOH1v5f20YFktemBnOtp_qkYvGVmXSVI3jIw';
const ITEM_ID  = 1;
// ============================

// 커스텀 카운터
const successCount          = new Counter('order_success');
const optimisticFailCount   = new Counter('optimistic_lock_fail');  // 재시도 소진 409 (낙관적 락 충돌)
const pessimisticFailCount   = new Counter('pessimistic_lock_fail');  // 비관적 락 충돌
const stockFailCount        = new Counter('stock_fail');            // 재고 부족 400
const otherFailCount        = new Counter('other_fail');

export const options = {
    vus: 50,
    duration: '10s',
};

const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
};

const payload = JSON.stringify({
    orderItem: { itemId: ITEM_ID, quantity: 1 },
    address: '서울시 테스트구 테스트동',
});

export default function () {
    const res = http.post(`${BASE_URL}/orders`, payload, { headers });

    const is201 = res.status === 201;
    const is409 = res.status === 409;
    const is400 = res.status === 400;

    check(res, {
        '201 주문 성공':           (r) => r.status === 201,
        '409 비관적 락 충돌':      (r) => r.status === 409,
        '400 재고 부족':           (r) => r.status === 400,
    });

    if (is201) successCount.add(1);
    else if (is409) optimisticFailCount.add(1);
    else if (is400) stockFailCount.add(1);
    else otherFailCount.add(1);

    // 상태별 로그 (너무 많으면 주석 처리)
    if (!is201) {
        console.log(`[${res.status}] ${res.body}`);
    }
}