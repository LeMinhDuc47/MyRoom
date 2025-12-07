import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
  stages: [
    { duration: '30s', target: 10 },  
    { duration: '1m', target: 100 },  
    { duration: '30s', target: 300 }, 
    { duration: '10s', target: 0 },   
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], 
    http_req_failed: ['rate<0.01'],  
  },
};


export default function () {
  const res = http.get('http://localhost:8080/api/v1/room-service/rooms');
  if (res.status !== 200) {
    console.log(`Lỗi: ${res.status}`);
  }
  check(res, { 'status was 200': (r) => r.status == 200 });
  sleep(1);
}
