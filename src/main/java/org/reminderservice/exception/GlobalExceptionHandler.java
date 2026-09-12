package org.reminderservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps database-outage failures on the REST API to a clean 503 instead of
 * a bare 500. Spring splits these into two unrelated exception
 * hierarchies: DataAccessException (a query itself failing) and
 * TransactionException (unable to even open a transaction/connection,
 * which is what a database being down actually throws --
 * CannotCreateTransactionException). Both need to be caught here; neither
 * is a subtype of the other.
 *
 * This does not cover the Kafka consumer path: a failure there is already
 * handled by the listener's retry + dead-letter mechanism (see
 * KafkaConsumerConfig / DeadLetterTopicListener), which is a separate,
 * already-proven concern from the synchronous REST endpoints this class
 * applies to.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(RuntimeException ex) {
        log.error("Database access failed: {}", ex.toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Service temporarily unavailable, please try again shortly"));
    }
}
