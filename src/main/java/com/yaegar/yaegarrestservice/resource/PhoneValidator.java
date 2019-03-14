package com.yaegar.yaegarrestservice.resource;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PhoneValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhoneValidator.class);

    public boolean isValidNumber(String numberToParse, String defaultRegion) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneNumberUtil.parseAndKeepRawInput(numberToParse, defaultRegion);
        } catch (NumberParseException e) {
            LOGGER.warn("Invalid number or number not for this region", e);
            return false;
        }
        return phoneNumberUtil.isValidNumberForRegion(phoneNumber, defaultRegion);
    }
}
