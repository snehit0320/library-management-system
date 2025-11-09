package com.library.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("phoneValidator")
public class PhoneValidator implements Validator<String> {
    
    @Override
    public void validate(FacesContext context, UIComponent component, String value) 
            throws ValidatorException {
        
        if (value == null || value.trim().isEmpty()) {
            return; // Let required validator handle this
        }
        
        String cleaned = value.replaceAll("[\\s()-]", "");
        
        if (cleaned.length() < 10 || cleaned.length() > 15) {
            throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Phone Validation Error",
                "Phone number must be between 10 and 15 digits."));
        }
        
        if (!cleaned.matches("^\\d+$")) {
            throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Phone Validation Error",
                "Phone number must contain only digits."));
        }
    }
}

