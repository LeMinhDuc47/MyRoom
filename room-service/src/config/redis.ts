import { createClient } from "redis";
import logger from "./logger";

const redisUrl = "redis://localhost:6379";

const redisClient = createClient({ url: redisUrl });

redisClient.on("error", (err) => {
  logger.error(`Redis client error: ${err instanceof Error ? err.message : String(err)}`);
});

redisClient.on("connect", () => {
  logger.info(`Redis client connected to ${redisUrl}`);
});

(async () => {
  try {
    if (!redisClient.isOpen) {
      await redisClient.connect();
    }
  } catch (err) {
    logger.error(`Failed to connect to Redis: ${err instanceof Error ? err.message : String(err)}`);
  }
})();

export default redisClient;
