import express from "express";
import { AddressInfo } from "net";
import "dotenv/config";
import logger from "./config/logger";
import routes from "./routes/routes";
import { createEurekaClient } from "./config/eurekaClientConfig";
import connect from "./config/mongoDBConnect";
import parseNestedJSON from "./middleware/parseNestedJSON";
import AppConstants from "./constants/AppConstants";
import { setEurekaClient } from "./config/eurekaClientHolder";
const cors = require("cors");
const CLSContext = require('zipkin-context-cls');
import { Tracer, BatchRecorder, jsonEncoder } from 'zipkin';
import { HttpLogger } from 'zipkin-transport-http';
const app = express();

app.use(express.json());
app.use(cors());

//app.use(parseNestedJSON);

const PORT = Number(process.env.PORT || 8086);

const server = app.listen(PORT, async () => {
  const serverAddress = server.address() as AddressInfo;
  const serverPort = serverAddress.port;

  routes(app);

  // connect to mongoDB
  await connect();

  // register the service to eureka server using actual bound port
  const eurekaClient = createEurekaClient({
    appName: AppConstants.ROOM_SERVICE,
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

  logger.info(`room-service is running at http://localhost:${serverPort}`);
});
