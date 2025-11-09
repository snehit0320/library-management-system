package com.library.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named(value = "navigationBean")
@RequestScoped
public class NavigationBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String goToHome() {
        return "home?faces-redirect=true";
    }
    
    public String goToAddBook() {
        return "addBook?faces-redirect=true";
    }
    
    public String goToIssueBook() {
        return "issueBook?faces-redirect=true";
    }
    
    public String goToReturnBook() {
        return "returnBook?faces-redirect=true";
    }
    
    public String goToReports() {
        return "reports?faces-redirect=true";
    }
    
    public String goToLogin() {
        return "login?faces-redirect=true";
    }
    
    public String goToSignup() {
        return "signup?faces-redirect=true";
    }
}

