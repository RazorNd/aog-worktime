/*
 * Copyright 2019 Daniil <razornd> Razorenov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
