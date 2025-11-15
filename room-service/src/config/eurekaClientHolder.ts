let eurekaClientRef: any = null;

export function setEurekaClient(client: any) {
    eurekaClientRef = client;
}

export function getEurekaClient() {
    return eurekaClientRef;
}
