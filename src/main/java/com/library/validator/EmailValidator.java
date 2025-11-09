package com.library.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.util.regex.Pattern;

@FacesValidator("emailValidator")
public class EmailValidator implements Validator<String> {
    
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    @Override
    public void validate(FacesContext context, UIComponent component, String value) 
            throws ValidatorException {
        
        if (value == null || value.trim().isEmpty()) {
            return; // Let required validator handle this
        }
        
        if (!pattern.matcher(value).matches()) {
            throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Email Validation Error",
                "Please enter a valid email address."));
        }
    }
}

