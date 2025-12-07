

# 🛠️ High Level Design
![Screenshot 2024-08-02 122824](https://github.com/user-attachments/assets/26f7e20a-773f-4907-a274-400195f6f520)

https://github.com/pawanpk87/MyRoom/assets/87040096/ad4b4ebe-26a0-4051-b3dc-e6080c69d7d5

<!-- Screenshots -->

### :camera: Screenshots(MyRoom dashboard)
<img width="1511" alt="Screenshot 2024-02-27 at 11 49 04 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/375a3071-8442-41c1-8a00-2a71d588a1c7">
<img width="1511" alt="Screenshot 2024-02-28 at 10 01 13 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/a61b1b8f-6094-4a22-9575-ed62245530db">
<img width="1511" alt="Screenshot 2024-02-28 at 10 01 27 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/4f2f425d-1066-45ec-a559-c5ef49adab63">

### :camera: Screenshots(MyRoom client)
<img width="1511" alt="Screenshot 2024-02-28 at 10 03 28 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/26c4a823-de24-4b9d-87d7-1a60cfe49fe1">
<img width="1511" alt="Screenshot 2024-02-28 at 10 03 52 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/86d495ef-3f3e-4fd4-b066-8ed29ae8b80c">
<img width="1511" alt="Screenshot 2024-02-28 at 10 04 53 AM" src="https://github.com/pawanpk87/MyRoom/assets/87040096/f29d2314-1a1d-4dab-8b3d-168db1d5a7a6">


### :space_invader: Tech Stack

<div align="center">
    <table>
        <tr>
            <td>Spring Boot<img width="40" src="https://user-images.githubusercontent.com/25181517/183891303-41f257f8-6b3d-487c-aa56-c497b880d0fb.png" alt="Spring Boot" title="Spring Boot"/></td>
            <td>Node.js<img width="40" src="https://user-images.githubusercontent.com/25181517/183568594-85e280a7-0d7e-4d1a-9028-c8c2209e073c.png" alt="Node.js" title="Node.js"/></td>
            <td>Express<img width="40" src="https://user-images.githubusercontent.com/25181517/183859966-a3462d8d-1bc7-4880-b353-e2cbed900ed6.png" alt="Express" title="Express"/></td>
            <td>React<img width="40" src="https://user-images.githubusercontent.com/25181517/183897015-94a058a6-b86e-4e42-a37f-bf92061753e5.png" alt="React" title="React"/></td>
            <td>Next.js<img width="40" src="https://github.com/marwin1991/profile-technology-icons/assets/136815194/5f8c622c-c217-4649-b0a9-7e0ee24bd704" alt="Next.js" title="Next.js"/></td>
            <td>Firebase<img width="40" src="https://user-images.githubusercontent.com/25181517/189716855-2c69ca7a-5149-4647-936d-780610911353.png" alt="Firebase" title="Firebase"/></td>
            <td>TypeScript<img width="40" src="https://user-images.githubusercontent.com/25181517/183890598-19a0ac2d-e88a-4005-a8df-1ee36782fde1.png" alt="TypeScript" title="TypeScript"/></td>
        </tr>
        <tr>
            <td>MySQL<img width="40" src="https://user-images.githubusercontent.com/25181517/183896128-ec99105a-ec1a-4d85-b08b-1aa1620b2046.png" alt="MySQL" title="MySQL"/></td>
            <td>MongoDB<img width="40" src="https://user-images.githubusercontent.com/25181517/182884177-d48a8579-2cd0-447a-b9a6-ffc7cb02560e.png" alt="mongoDB" title="mongoDB"/></td>
            <td>Kafka<img width="40" src="https://user-images.githubusercontent.com/25181517/192107004-2d2fff80-d207-4916-8a3e-130fee5ee495.png" alt="kafka" title="kafka"/></td>
            <td>Swagger<img width="40" src="https://user-images.githubusercontent.com/25181517/186711335-a3729606-5a78-4496-9a36-06efcc74f800.png" alt="Swagger" title="Swagger"/></td>
        </tr>
    </table>
</div>
# MYROOM - HỆ THỐNG QUẢN LÝ KHÁCH SẠN

## Tổng Quan

MyRoom là một hệ thống quản lý khách sạn, được xây dựng theo kiến trúc microservice. Dự án này cung cấp một nền tảng linh hoạt, có khả năng mở rộng để xử lý các nghiệp vụ cốt lõi của việc quản lý và đặt phòng khách sạn, từ việc tìm kiếm, thanh toán đến quản lý sau khi đặt phòng.

## Mục tiêu cải tiến
Mục tiêu của việc cải tiến MyRoom là xây dựng một hệ thống có khả năng chịu tải cao, phản hồi nhanh và dễ bảo trì, tách biệt các chức năng nghiệp vụ phức tạp thành các dịch vụ độc lập.

## Các cải tiến đã thực hiện

### Rate Limiting

Để nâng cao tính ổn định, bảo mật và khả năng phục hồi của hệ thống microservice MyRoom, một cơ chế giới hạn yêu cầu Rate Limiting đã được thiết kế và triển khai. Giải pháp này sử dụng Spring Cloud Gateway (myroom-gateway) làm cổng kiểm soát duy nhất, tích hợp với Redis để quản lý trạng thái giới hạn một cách nhất quán.

### Scalability

Em đã tạo thêm nhiều instance cho các service, cho hệ thống microservice hiện tại nhằm đạt được hai mục tiêu chính:
- Tính Sẵn sàng cao (High Availability): Đảm bảo hệ thống không bị gián đoạn hay sập khi một service đơn lẻ gặp lỗi.
- Mở rộng Hiệu năng (Performance Scaling): Phân bổ tải (load balancing) cho các service có lưu lượng truy cập cao.

### Load balance

Hệ thống MyRoom sử dụng cơ chế cân bằng tải với thuật toán Round Robin được tích hợp sẵn thông qua sự kết hợp của hai thành phần cốt lõi: Spring Cloud Gateway (đóng vai trò API Gateway) và Netflix Eureka (đóng vai trò Service Discovery)

### Retry topic và Dead Letter Queue topic

Hệ thống MyRoom sử dụng Kafka để liên lạc bất đồng bộ giữa các service. Tuy nhiên, ở cấu hình ban đầu, nếu một service "tiêu thụ" (consumer) gặp lỗi khi xử lý tin nhắn (ví dụ: mail-service không gửi được email do SMTP server tạm thời bị sập), tin nhắn đó sẽ bị bỏ qua và mất vĩnh viễn.
Để giải quyết rủi ro này, chúng tôi đã triển khai một cơ chế xử lý lỗi mạnh mẽ bao gồm hai phần: Cơ chế Retry và Dead Letter Queue.

### Distributed Transaction - SAGA pattern
Saga Pattern được áp dụng để giải quyết chính xác vấn đề tính nhất quán. Nó là một cơ chế để quản lý tính nhất quán của dữ liệu qua nhiều service mà không cần blocking giao dịch. Một Saga đảm bảo rằng một chuỗi nghiệp vụ hoặc là thành công trọn vẹn (Happy Path), hoặc là quay lui trọn vẹn (Sad Path).

### Circuit Breaker

Trong kiến trúc Microservices của dự án MyRoom, các services liên tục giao tiếp với nhau thông qua mạng. Điều này tiềm ẩn rủi ro lớn về Cascading Failures:
- Phụ thuộc nội bộ: MyRoom Gateway gọi xuống Booking Service, Booking Service lại gọi sang Payment Service. Nếu Payment Service gặp sự cố (chết hoặc phản hồi chậm), các Thread tại Booking Service sẽ bị treo (block) để chờ đợi. Khi tài nguyên cạn kiệt, Booking Service sẽ chết theo, kéo theo Gateway bị tê liệt.
- Phụ thuộc bên thứ 3: Payment Service phụ thuộc vào API của Stripe. Nếu mạng của Stripe bị lag hoặc bảo trì, hệ thống của chúng ta không nên bị treo theo.
- Trải nghiệm người dùng (UX): Khi hệ thống lỗi, người dùng thường phải chờ rất lâu (timeout) mới nhận được thông báo lỗi 500 khó hiểu hoặc màn hình trắng xóa.

Vì vậy hệ thống cần cơ chế ngắt mạch để ngăn chặn lỗi lan truyền và tự phục hồi khi dịch vụ ổn định trở lại.


### Zipkin (Distributed Tracing)
Trong Microservices, một hành động của người dùng (ví dụ: "Đặt phòng") không chỉ được xử lý bởi một server, mà nó nhảy qua rất nhiều nơi. Nếu không có Zipkin, logs của các service này nằm rời rạc. Chúng ta sẽ không biết dòng log này ở Booking Service có liên quan gì đến dòng log kia ở Payment Service. Ngoài ra Zipkin còn giúp chúng ta phát hiện bottleneck.

### ELK (Elasticsearch - Logstash - Kibana)


### Cache data

### Health enpoint monitoring


### Prometheus và Grafana




### Distributed lock

### Pipe and filter



### Competing consumer









