"use client"; // Quan trọng để dùng Ant Design
import React from 'react';
import Link from 'next/link';
import { Result, Button, Typography, Card, Steps } from 'antd';
import { ClockCircleOutlined, HomeOutlined, CheckCircleOutlined } from '@ant-design/icons';

const { Paragraph, Text } = Typography;

export default function PaymentPendingPage() {
  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '80vh', 
      backgroundColor: '#f5f5f5',
      padding: '20px'
    }}>
      <Card style={{ maxWidth: 600, width: '100%', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
        <Result
          icon={<ClockCircleOutlined style={{ color: '#faad14' }} />}
          status="warning"
          title="Thanh toán đang chờ xử lý"
          subTitle="Hệ thống thanh toán đang phản hồi chậm. Đừng lo lắng, yêu cầu đặt phòng của bạn đã được ghi nhận!"
          extra={[
            <Link href="/" key="home">
              <Button type="primary" icon={<HomeOutlined />} size="large">
                Về trang chủ
              </Button>
            </Link>,
          ]}
        >
          <div className="desc">
            <Paragraph>
              <Text strong style={{ fontSize: 16 }}>Trạng thái hiện tại của đơn hàng:</Text>
            </Paragraph>
            
            <Steps
              direction="vertical"
              size="small"
              current={1} 
              items={[
                {
                  title: 'Đặt phòng',
                  description: 'Bạn đã chọn phòng và điền thông tin.',
                  status: 'finish',
                  icon: <CheckCircleOutlined />,
                },
                {
                  title: 'Thanh toán',
                  description: 'Đang chờ xác nhận từ ngân hàng/Stripe.',
                  status: 'process',
                  icon: <ClockCircleOutlined style={{ color: '#faad14' }} />,
                },
                {
                  title: 'Hoàn tất',
                  description: 'Vé điện tử sẽ được gửi qua email.',
                  status: 'wait',
                },
              ]}
            />

            <Paragraph style={{ marginTop: 24 }}>
              <Text type="secondary">
                Lưu ý: Vui lòng <strong>không đặt lại</strong> để tránh bị trừ tiền 2 lần. 
                Chúng tôi sẽ cập nhật trạng thái ngay khi có kết quả.
              </Text>
            </Paragraph>
          </div>
        </Result>
      </Card>
    </div>
  );
}