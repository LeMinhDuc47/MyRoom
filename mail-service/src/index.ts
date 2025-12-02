import express from "express";
import { AddressInfo } from "net";
import "dotenv/config";
import logger from "./config/logger";
import { createEurekaClient } from "./config/eurekaClientConfig";
import BookingMailConsumer from "./config/kafka/BookingMailConsumer";
import { transporter } from "./config/mail";
import AppConstants from "./constants/AppConstants";
import { setEurekaClient } from "./config/eurekaClientHolder";

import client from 'prom-client';
import { Tracer, BatchRecorder, jsonEncoder } from 'zipkin';
import { HttpLogger } from 'zipkin-transport-http';
const CLSContext = require('zipkin-context-cls');
const zipkinMiddleware = require('zipkin-instrumentation-express').expressMiddleware;
const cors = require("cors");

const app = express();

const collectDefaultMetrics = client.collectDefaultMetrics;
collectDefaultMetrics({ register: client.register });

app.get('/metrics', async (req, res) => {
  res.setHeader('Content-Type', client.register.contentType);
  const metrics = await client.register.metrics();
  res.send(metrics);
});

const tracer = new Tracer({
  ctxImpl: new CLSContext('zipkin'),
  recorder: new BatchRecorder({
    logger: new HttpLogger({
      endpoint: process.env.ZIPKIN_ENDPOINT || 'http://localhost:9411/api/v2/spans',
      jsonEncoder: jsonEncoder.JSON_V2
    })
  }),
  localServiceName: 'mail-service'
});

app.use(zipkinMiddleware({ tracer }));

app.use(express.json());
app.use(cors());

const PORT = Number(process.env.PORT || 8089);

const server = app.listen(PORT, async () => {
  const serverAddress = server.address() as AddressInfo;
  const serverPort = serverAddress.port;
  
  const bookingMailConsumer = new BookingMailConsumer({});
  try {
    await bookingMailConsumer.startConsumer();
    logger.info(`Kafka consumer started successfully`);
  } catch (error: any) {
    logger.error(`Error occured during starting Kafka consumer: ${error}`);
  }

  transporter.verify(function (error: any, success: any) {
    if (error) {
      logger.error(`Error occurred while starting the mail server: `, error);
    } else {
      logger.info("Mail Server is ready to take our messages!");
    }
  });

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
  
  setEurekaClient(eurekaClient);

  logger.info(`mail-service is running at http://localhost:${serverPort}`);
});