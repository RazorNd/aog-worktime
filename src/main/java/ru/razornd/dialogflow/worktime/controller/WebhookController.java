package ru.razornd.dialogflow.worktime.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.razornd.dialogflow.worktime.service.WorkTimeService;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping("/api/work-time/")
@Slf4j
public class WebhookController {

    private final WorkTimeService service;

    public WebhookController(WorkTimeService service) {
        this.service = service;
    }

    @PostMapping(
            value = "webhook",
            consumes = APPLICATION_JSON_UTF8_VALUE,
            produces = APPLICATION_JSON_UTF8_VALUE
    )
    public Mono<String> webhook(@RequestBody String body, @RequestHeader HttpHeaders httpHeaders) {
        log.info("Request: {}", body);
        return Mono.fromFuture(service.handleRequest(body, httpHeaders.toSingleValueMap()));
    }
}
