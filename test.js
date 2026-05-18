import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 100,        // 동시 사용자 100명
    duration: '30s', // 30초 동안 실행
};

export default function () {
    const headers = {   //테스트 시 헤더에 추가할 부분
        'X-User-Id': '41a4106e-2057-49f7-ae9c-6c2ce5e0b995',
        'X-User-Username': 'test',
        'X-User-Roles': 'ROLE_USER',
        'X-User-Enabled': 'true',
    };

    const res = http.get('http://34.64.205.199:8082/api/v1/products', { headers }); //요청 주소

    check(res, {
        'status is 200': (r) => r.status === 200,   //응답 성공 여부에 대한 판단 기준
    });
}