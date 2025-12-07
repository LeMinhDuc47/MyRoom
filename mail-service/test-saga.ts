import { Kafka } from "kafkajs";

const KAFKA_BROKER = "localhost:9092";
const PAYMENT_TOPIC = "booking.payment"; 

const kafka = new Kafka({
  clientId: "saga-tester",
  brokers: [KAFKA_BROKER],
});

const producer = kafka.producer();

const run = async () => {
  await producer.connect();
  console.log("✅ Connected to Kafka");

  
  const type = process.argv[2];
  const bookingId = process.argv[3]; 

  if (!type || !bookingId) {
    console.error("❌ Cách dùng: npm run test-saga <success|fail> <bookingId>");
    process.exit(1);
  }

  const status = type === "success" ? "complete" : "failed";
  const payload = {
    bookingId: bookingId,
    status: status,
    amount: 100000,
    currency: "inr",
    paymentId: "pay_test_123"
  };

  console.log(`🚀 Sending [${status}] event for booking [${bookingId}]...`);

  await producer.send({
    topic: PAYMENT_TOPIC,
    messages: [{ value: JSON.stringify(payload) }],
  });

  console.log("✅ Message sent! Check your Booking Service logs.");
  await producer.disconnect();
};

run().catch(console.error);