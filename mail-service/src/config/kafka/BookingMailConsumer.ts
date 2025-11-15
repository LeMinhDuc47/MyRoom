import {
  Consumer,
  ConsumerSubscribeTopics,
  Kafka,
  EachMessagePayload,
  Producer,
} from "kafkajs";
import logger from "../logger";
import { sendBookingMailService } from "../../service";

export default class BookingMailConsumer {
  private kafkaConsumer: Consumer;
  private kafkaProducer: Producer;
  private messageProcessor: any;

  public constructor(messageProcessor: any) {
    this.messageProcessor = messageProcessor;
    this.kafkaConsumer = this.createKafkaConsumer();
    this.kafkaProducer = this.createKafkaProducer();
  }

  public async startConsumer(): Promise<void> {
    const topic: ConsumerSubscribeTopics = {
      topics: ["booking.mail"],
      fromBeginning: false,
    };
    try {
      await this.kafkaConsumer.connect();
      await this.kafkaProducer.connect();
      await this.kafkaConsumer.subscribe(topic);
      await this.kafkaConsumer.run({
        eachMessage: async (messagePayload: EachMessagePayload) => {
          logger.info(
            `Received booking mail event (kafka topic: 'booking.mail') with payload: `,
            messagePayload
          );

          const { topic, partition, message } = messagePayload;
          await this.processWithRetry(message);
        },
      });
    } catch (error) {
      logger.error("Error: ", error);
    }
  }

  public async shutdown(): Promise<void> {
    await this.kafkaConsumer.disconnect();
    await this.kafkaProducer.disconnect();
  }

  private createKafkaConsumer(): Consumer {
    const kafka = new Kafka({
      clientId: "booking.mail",
      brokers: ["localhost:9092"],
    });
    const consumer = kafka.consumer({ groupId: "booking.mail-group" });
    return consumer;
  }

  private createKafkaProducer(): Producer {
    const kafka = new Kafka({
      clientId: "booking.mail.producer",
      brokers: ["localhost:9092"],
    });
    return kafka.producer();
  }

  private async processWithRetry(message: any) {
    const MAX_RETRIES = 3;
    const baseDelayMs = 1000;
    const headers = (message.headers || {}) as Record<string, Buffer>;
    const currentRetry = headers["x-retry"]
      ? parseInt(headers["x-retry"].toString())
      : 0;

    try {
      await sendBookingMailService(message);
    } catch (err) {
      const nextRetry = currentRetry + 1;
      if (nextRetry <= MAX_RETRIES) {
        const delay = baseDelayMs * Math.pow(2, currentRetry);
        logger.error(
          `booking.mail processing failed, retry ${nextRetry}/${MAX_RETRIES} in ${delay}ms`,
          err
        );
        await new Promise((res) => setTimeout(res, delay));
        await this.kafkaProducer.send({
          topic: "booking.mail",
          messages: [
            {
              key: message.key?.toString(),
              value: message.value?.toString(),
              headers: { ...headers, "x-retry": Buffer.from(String(nextRetry)) },
            },
          ],
        });
      } else {
        logger.error(`booking.mail processing failed, sending to DLT`, err);
        await this.kafkaProducer.send({
          topic: "booking.mail.DLT",
          messages: [
            {
              key: message.key?.toString(),
              value: message.value?.toString(),
              headers: { ...headers, "x-error": Buffer.from(String(err)) },
            },
          ],
        });
      }
    }
  }
}
