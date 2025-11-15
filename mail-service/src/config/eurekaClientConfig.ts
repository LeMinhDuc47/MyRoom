import AppConstants from "../constants/AppConstants";

const { Eureka } = require("eureka-js-client");

type EurekaOptions = {
  appName?: string;
  port: number;
  instanceId?: string;
};

export function createEurekaClient(options: EurekaOptions) {
  const appName = options.appName || AppConstants.MAIL_SERVICE;
  const port = options.port;
  const instanceId =
    options.instanceId || `${appName}-${process.pid}-${port}`;

  const eurekaHost = process.env.EUREKA_HOST || "localhost";
  const eurekaPort = Number(process.env.EUREKA_PORT || 8761);
  const hostName = process.env.EUREKA_HOSTNAME || "localhost";
  const ipAddr = process.env.EUREKA_IP || "127.0.0.1";

  return new Eureka({
    instance: {
      app: appName,
      instanceId,
      hostName,
      ipAddr,
      status: "UP",
      vipAddress: appName,
      preferIpAddress: true,
      port: { $: port, "@enabled": true },
      dataCenterInfo: {
        "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
        name: "MyOwn",
      },
    },
    eureka: {
      host: eurekaHost,
      port: eurekaPort,
      servicePath: "/eureka/apps",
    },
  });
}

