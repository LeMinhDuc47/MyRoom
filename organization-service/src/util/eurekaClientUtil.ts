export function getInstance(client: any, serviceName: string): string {
  const instances: [any] = client.getInstancesByAppId(serviceName);
  let URI = "";
  if (instances && instances.length > 0) {
    const instance = instances[0];

    const protocol =
      instance.securePort["@enabled"] == "true" ? "https" : "http";
    const ipAddr = instance.ipAddr;
    const port = instance.port.$;

    URI = `${protocol}://${ipAddr}:${port}`;

    // OR
    // URI = instance["homePageUrl"];
  }
  return URI;
}
