import express from "express";
import { AddressInfo } from "net";
import "dotenv/config";
import logger from "./config/logger";
import { createEurekaClient } from "./config/eurekaClientConfig";
import BookingMailConsumer from "./config/kafka/BookingMailConsumer";
import { transporter } from "./config/mail";
const cors = require("cors");
import AppConstants from "./constants/AppConstants";
import { setEurekaClient } from "./config/eurekaClientHolder";
const CLSContext = require('zipkin-context-cls');
import { Tracer, BatchRecorder, jsonEncoder } from 'zipkin';
import { HttpLogger } from 'zipkin-transport-http';
const app = express();

app.use(express.json());
app.use(cors());

const PORT = Number(process.env.PORT || 8089);

const server = app.listen(PORT, async () => {
  const serverAddress = server.address() as AddressInfo;
  const serverPort = serverAddress.port;

  // Instantiate the Kafka consumer
  const bookingMailConsumer = new BookingMailConsumer({});
  // start the Kafka consumer
  try {
    await bookingMailConsumer.startConsumer();
    logger.info(`Kafka consumer started successfully`);
  } catch (error: any) {
    logger.error(`Error occured during starting Kafka consumer: ${error}`);
  }

  // Start the mail server
  transporter.verify(function (error: any, success: any) {
    if (error) {
      logger.error(`Error occurred while starting the mail server: `, error);
    } else {
      logger.info("Server is ready to take our messages!");
    }
  });

  // register the service to eureka server using actual bound port
  const eurekaClient = createEurekaClient({
    appName: AppConstants.MAIL_SERVICE,
    port: serverPort,
    instanceId: process.env.INSTANCE_ID,
  });

  eurekaClient.start((error: any) => {
    if (error) {
      logger.error(`Error occured during starting the eureka client: ${error}`);
    }
  });
        const {Tracer, BatchRecorder, jsonEncoder: {JSON_V2}} = require('zipkin');
        const {HttpLogger} = require('zipkin-transport-http');
        const zipkinMiddleware = require('zipkin-instrumentation-express').expressMiddleware;

        const tracer = new Tracer({
          ctxImpl: new CLSContext('zipkin'),
          recorder: new BatchRecorder({
            logger: new HttpLogger({
              endpoint: 'http://localhost:9411/api/v2/spans',
              jsonEncoder: JSON_V2
            })
          }),
          localServiceName: 'mail-service' 
        });

        app.use(zipkinMiddleware({tracer}));
  setEurekaClient(eurekaClient);

  logger.info(`mail-service is running at http://localhost:${serverPort}`);
});
