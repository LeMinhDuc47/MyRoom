import winston from "winston";
import path from "path";
const logger = winston.createLogger({
  level: "info",
  format: winston.format.json(),
  defaultMeta: {
    service: "mail-service",
  },
  transports: [
    /**
     * Write all logs with importance level of `error` or less to `error.log`
     * Write all logs with importance level of `info` or less to `combined.log`
     */
    new winston.transports.File({
      filename: path.join("logs", "error.log"),
      level: "error",
    }),
    new winston.transports.File({
      filename: path.join("logs", "combined.log"),
    }),
  ],
});

/**
 * If we're not in production then log to the `console` with the format:
 * `${info.level}: ${info.message} JSON.stringify({ ...res })`
 */
if (process.env.NODE_ENV !== "prod") {
  logger.add(
    new winston.transports.Console({
      format: winston.format.simple(),
    })
  );
}

export default logger;
