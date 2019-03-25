package com.yaegar.yaegarrestservice.resource;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneValidatorTest {

    private PhoneValidator phoneValidator = new PhoneValidator();

    @Test
    public void givenInvalidPhoneNumberAndInvalidCountry_thenReturnFalse() {
        //given
        String phoneNumber = "123456789";
        String country = "UQ";

        //then
        assertFalse(phoneValidator.isValidNumber(phoneNumber, country));
    }

    @Test
    public void givenInvalidPhoneNumberAndValidCountry_thenReturnFalse() {
        //given
        String phoneNumber = "123456789";
        String country = "UK";

        //then
        assertFalse(phoneValidator.isValidNumber(phoneNumber, country));
    }

    @Test
    public void givenValidPhoneNumberAndValidCountry_thenReturnTrue() {
        //then
        assertTrue(phoneValidator.isValidNumber("7780708394", "GB"));
        assertTrue(phoneValidator.isValidNumber("07780708394", "GB"));
        assertTrue(phoneValidator.isValidNumber("+447780708394", "GB"));
        assertTrue(phoneValidator.isValidNumber("+4407780708394", "GB"));
    }
}