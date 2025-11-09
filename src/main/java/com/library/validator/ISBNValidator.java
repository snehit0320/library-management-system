package com.library.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("isbnValidator")
public class ISBNValidator implements Validator<String> {
    
    @Override
    public void validate(FacesContext context, UIComponent component, String value) 
            throws ValidatorException {
        
        if (value == null || value.trim().isEmpty()) {
            return; // Let required validator handle this
        }
        
        String cleaned = value.replaceAll("[\\s-]", "");
        
        // Check length
        if (cleaned.length() != 10 && cleaned.length() != 13) {
            throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "ISBN Validation Error",
                "ISBN must be either 10 or 13 digits long."));
        }
        
        // Check if all digits
        if (!cleaned.matches("^\\d+$")) {
            throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "ISBN Validation Error",
                "ISBN must contain only digits."));
        }
        
        // Validate ISBN-10 checksum
        if (cleaned.length() == 10) {
            if (!isValidISBN10(cleaned)) {
                throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "ISBN Validation Error",
                    "Invalid ISBN-10 checksum."));
            }
        }
        
        // Validate ISBN-13 checksum
        if (cleaned.length() == 13) {
            if (!isValidISBN13(cleaned)) {
                throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "ISBN Validation Error",
                    "Invalid ISBN-13 checksum."));
            }
        }
    }
    
    private boolean isValidISBN10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(isbn.charAt(i)) * (10 - i);
        }
        int checkDigit = Character.getNumericValue(isbn.charAt(9));
        if (checkDigit == 'X' - '0') {
            checkDigit = 10;
        }
        return (sum + checkDigit) % 11 == 0;
    }
    
    private boolean isValidISBN13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = Character.getNumericValue(isbn.charAt(12));
        return (10 - (sum % 10)) % 10 == checkDigit;
    }
}

