package com.yaegar.yaegarrestservice.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneUtilTest {
    @Test
    public void givenInvalidPhoneNumberAndInvalidCountry_thenReturnFalse() {
        //given
        String phoneNumber = "123456789";
        String country = "UQ";

        //then
        assertFalse(PhoneUtil.isValidNumber(phoneNumber, country));
    }

    @Test
    public void givenInvalidPhoneNumberAndValidCountry_thenReturnFalse() {
        //given
        String phoneNumber = "123456789";
        String country = "UK";

        //then
        assertFalse(PhoneUtil.isValidNumber(phoneNumber, country));
    }

    @Test
    public void givenValidPhoneNumberAndValidCountry_thenReturnTrue() {
        //then
        assertTrue(PhoneUtil.isValidNumber("7780708394", "GB"));
        assertTrue(PhoneUtil.isValidNumber("07780708394", "GB"));
        assertTrue(PhoneUtil.isValidNumber("+447780708394", "GB"));
        assertTrue(PhoneUtil.isValidNumber("+4407780708394", "GB"));
    }
}