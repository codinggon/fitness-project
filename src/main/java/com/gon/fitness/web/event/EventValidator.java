package com.gon.fitness.web.event;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.Valid;
import java.time.LocalDateTime;

public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return EventValidator.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

    }



}
