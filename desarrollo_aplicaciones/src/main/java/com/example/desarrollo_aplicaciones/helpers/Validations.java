package com.example.desarrollo_aplicaciones.helpers;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class Validations {

    public static boolean isValidName(String name) {
        return name.matches("^[A-Z][a-zA-Z ]*$");
    }

    public static boolean isValidDni(Integer dni) {
        if (dni == null) return false;
        String dniStr = String.valueOf(dni);
        return dniStr.matches("^\\d{5,8}$");
    }

    public static boolean isValidPhoneNumber(String phoneNumber, String region) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, region);
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            System.err.println("Fomato de numero de telefono inválido.");
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        return email.contains("@");
    }

    public static boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*_+?\\-=]).{8,}$");
    }
}
