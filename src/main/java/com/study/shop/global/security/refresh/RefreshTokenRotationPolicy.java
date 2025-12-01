package com.study.shop.global.security.refresh;

public class RefreshTokenRotationPolicy {

    /**
     * 간단 정책:
     * 1) Refresh 토큰 재발급(rotate) 시 기존 토큰과 일치해야 한다.
     * 2) 불일치(누군가가 이전 토큰을 사용하면) -> 저장된 토큰 삭제하고
     *    해당 계정의 모든 세션/토큰을 무효화(또는 관리자 알림).
     *
     * 추가 옵션(고급):
     * - 재발급 시 "이전 토큰 목록"을 유지해서 짧은 시간 동안 재사용 허용(Replay window)
     * - 재사용 탐지 시 알림/로그 기록, 계정 잠금 등
     */
}
