package com.library.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("isbnConverter")
public class ISBNConverter implements Converter<String> {
    
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Remove hyphens and spaces
        String cleaned = value.replaceAll("[\\s-]", "");
        
        // Validate ISBN format (10 or 13 digits)
        if (!cleaned.matches("^\\d{10}(\\d{3})?$")) {
            throw new ConverterException("Invalid ISBN format. ISBN must be 10 or 13 digits.");
        }
        
        return cleaned;
    }
    
    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return "";
        }
        
        // Format ISBN with hyphens for display
        String cleaned = value.replaceAll("[\\s-]", "");
        if (cleaned.length() == 13) {
            return cleaned.substring(0, 3) + "-" + 
                   cleaned.substring(3, 4) + "-" + 
                   cleaned.substring(4, 7) + "-" + 
                   cleaned.substring(7, 12) + "-" + 
                   cleaned.substring(12);
        } else if (cleaned.length() == 10) {
            return cleaned.substring(0, 1) + "-" + 
                   cleaned.substring(1, 4) + "-" + 
                   cleaned.substring(4, 9) + "-" + 
                   cleaned.substring(9);
        }
        
        return value;
    }
}

