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

package ru.razornd.dialogflow.worktime.service;

import com.google.actions.api.*;
import com.google.actions.api.response.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class WorkTimeService extends DialogflowApp {

    private static final String START_WORK = "startWork";
    private static final String WORK_DURATION = "workDuration";
    private static final String DEFAULT_WORK_DURATION = "PT9H";

    @ForIntent("time-left")
    public ActionResponse timeLeft(ActionRequest request) {
        OffsetDateTime startWork = getStartWorkFromUserStorage(request);

        if (startWork == null || !startWork.toLocalDate().equals(LocalDate.now())) {
            return getResponseBuilder(request)
                    .add("А когда ты пришел на работу?")
                    .add(new ActionContext("time-left-save-start-work", 5))
                    .build();
        }

        return prepareTimeLeftAnswer(calcTimeLeft(startWork, getWorkDuration(request)), getResponseBuilder(request));
    }

    @ForIntent("repeat")
    public ActionResponse repeat(ActionRequest request) {
        return timeLeft(request);
    }

    @ForIntent("set-work-duration")
    public ActionResponse setWorkDuration(ActionRequest request) {
        final Map workDurationObject = Optional.ofNullable(request.getParameter("workDuration"))
                .map(Map.class::cast)
                .orElseThrow(NullPointerException::new);

//        TODO: сохранять продолжительность рабочего дня
//        final String timeUnit = (String) workDurationObject.get("unit");
//        final String value = (String) workDurationObject.get("amount");

        return getResponseBuilder(request)
                .add("Я запомнила")
                .build();
    }

    @ForIntent("time-left-set-start-work")
    public ActionResponse setStartWorkTimeLeft(ActionRequest request) {
        final OffsetDateTime startWork = getStartWorkFromRequest(request);

        saveStartWorkToUserStorage(request, startWork);

        return prepareTimeLeftAnswer(
                calcTimeLeft(startWork, getWorkDuration(request)),
                getResponseBuilder(request)
                        .add("Я запомнила что ты пришел в: " + startWork.toLocalTime())
        );
    }

    @ForIntent("set-start-work")
    public ActionResponse setStartWork(ActionRequest request) {
        final OffsetDateTime startWork = getStartWorkFromRequest(request);
        saveStartWorkToUserStorage(request, startWork);
        return getResponseBuilder(request)
                .add("Я запомнила что ты пришел в: " + startWork.toLocalTime())
                .build();
    }


    @NotNull
    private Duration getWorkDuration(ActionRequest request) {
        final String workDuration = (String) request
                .getUserStorage()
                .getOrDefault(WORK_DURATION, DEFAULT_WORK_DURATION);
        return Duration.parse(workDuration);
    }

    private OffsetDateTime getStartWorkFromRequest(ActionRequest request) {
        return Optional.ofNullable(request.getParameter(START_WORK))
                .map(String.class::cast)
                .filter(startWork -> !startWork.isEmpty())
                .map(startWork -> OffsetTime.parse(startWork, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .map(startWork -> OffsetDateTime.of(LocalDate.now(), startWork.toLocalTime(), startWork.getOffset()))
                .orElseThrow(NullPointerException::new);
    }

    private void saveStartWorkToUserStorage(ActionRequest request, OffsetDateTime startWork) {
        request.getUserStorage().put(START_WORK, startWork.toString());
    }

    @Nullable
    private OffsetDateTime getStartWorkFromUserStorage(ActionRequest request) {
        return Optional.ofNullable(request.getUserStorage().get(START_WORK))
                .map(String.class::cast)
                .map(OffsetDateTime::parse)
                .orElse(null);
    }

    @NotNull
    private ActionResponse prepareTimeLeftAnswer(Duration between, ResponseBuilder responseBuilder) {
        long hours = between.toHours();
        long minutes = between.toMinutes() % 60;

        responseBuilder.add(new ActionContext("time-left", 1));

        if (hours >= 0 && minutes >= 0) {
            return responseBuilder
                    .add(String.format("Осталось работать %02d:%02d", hours, minutes))
                    .build();
        }

        return responseBuilder
                .add("Уже можно идти домой")
                .build();
    }

    @NotNull
    private Duration calcTimeLeft(OffsetDateTime startWork, Duration workDuration) {
        return Duration.between(OffsetDateTime.now(), startWork.plus(workDuration));
    }
}
