package org.abrahamalarcon.datastream;

import org.springframework.validation.Validator;
import org.springmodules.validation.valang.ValangValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AbrahamAlarcon on 12/26/2016.
 */
public class InputValidators {

    public ValangValidator createValidator(String valang) throws Exception {
        ValangValidator valangValidator = new ValangValidator();
        valangValidator.setValang(valang);
        valangValidator.afterPropertiesSet();
        return valangValidator;
    }

    public Map<String, Validator> validators() throws Exception {
        Map<String, Validator> validators = new HashMap<>();
        validators.put("country", createValidator(
                "{country: ? IS NULL OR ? IS BLANK OR (length(?) <= 100 AND matches('^[A-Za-z\\'\\\\s]+$',?) IS TRUE) : '' : 'field.badFormat'}"
        ));
        validators.put("city", createValidator(
                "{city: length(?) <= 100 AND matches('^[A-Za-z\\'\\\\s]+$',?) IS TRUE : '' : 'field.badFormat'}"
        ));

        validators.put("event", createValidator(
                "{event: ? IN 'geolookup', 'conditions', 'forecast' : '' : 'field.badFormat'}"
        ));

        return validators;
    }
}
