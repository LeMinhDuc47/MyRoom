import express from "express";
import { AddressInfo } from "net";
import "dotenv/config";
import logger from "./config/logger";
import routes from "./routes/routes";
import { createEurekaClient } from "./config/eurekaClientConfig";
import connect from "./config/mongoDBConnect";
import AppConstants from "./constants/AppConstants";
const CLSContext = require('zipkin-context-cls');
import { Tracer, BatchRecorder, jsonEncoder } from 'zipkin';
import { HttpLogger } from 'zipkin-transport-http';
const app = express();

app.use(express.json());

const PORT = Number(process.env.PORT || 8085);

const server = app.listen(PORT, async () => {
  const serverAddress = server.address() as AddressInfo;
  const serverPort = serverAddress.port;

  routes(app);

  // connect to mongoDB
  await connect();

  // register the service to eureka server with the actual bound port
  const eurekaClient = createEurekaClient({
    appName: AppConstants.ORGANIZATION_SERVICE,
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
  localServiceName: 'organization-service' 
});

app.use(zipkinMiddleware({tracer}))

  logger.info(
    `organization-service is running at http://localhost:${serverPort}`
  );
});
