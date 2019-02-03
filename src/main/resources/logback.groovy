import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender("FILE", FileAppender) {
    file = "LeechUrl2.log"
    encoder(PatternLayoutEncoder) {
        pattern = "%date %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}
appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}
root(INFO, ["FILE", "STDOUT"])
