import http from 'k6/http';
import { check } from 'k6';

// ========== 설정값 ==========
const BASE_URL = 'http://localhost:8080/api';
const TOKEN    = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHJpbmciLCJpYXQiOjE3NzYxNjQ2NDgsImV4cCI6MTc3NjE2NjQ0OH0.Of6zY30aJAHKsM-V3R2Ed-dxPjIDvQqvC8zWr4HcyC8';
const ITEM_ID  = 1;
// ============================

export const options = {
    vus: 50,          // 가상 유저 50명
    duration: '10s',  // 10초간 지속
};

const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
};

const payload = JSON.stringify({
    orderItem: {
        itemId: ITEM_ID,
        quantity: 1,
    },
    address: '서울시 테스트구 테스트동',
});

export default function () {
    const res = http.post(`${BASE_URL}/orders`, payload, { headers });

    console.log(`status: ${res.status}, body: ${res.body}`);

    check(res, {
        '201 SUCCESS': (r) => r.status === 201,
            '409 낙관적 락 충돌': (r) => r.status === 409,
            '400 재고 부족': (r) => r.status === 400,
    });
}