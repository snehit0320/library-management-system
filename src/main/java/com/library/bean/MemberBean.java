package com.library.bean;

import com.library.dao.MemberDAO;
import com.library.model.Member;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named(value = "memberBean")
@SessionScoped
public class MemberBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Member member = new Member();
    private Member loggedInMember;
    private MemberDAO memberDAO = new MemberDAO();
    private List<Member> members;
    private String loginMemberId;
    
    public MemberBean() {
        loadMembers();
    }
    
    public void loadMembers() {
        members = memberDAO.findAll();
    }
    
    public void onPageLoad(ComponentSystemEvent event) {
        loadMembers();
    }
    
    public String signup() {
        try {
            // Check if member ID already exists
            Member existingMember = memberDAO.findByMemberId(member.getMemberId());
            if (existingMember != null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Member ID already exists! Please choose a different one."));
                return null;
            }
            
            // Check if email already exists
            Member existingEmail = memberDAO.findByEmail(member.getEmail());
            if (existingEmail != null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Email already registered! Please use a different email."));
                return null;
            }
            
            // Set registration date
            if (member.getRegistrationDate() == null) {
                member.setRegistrationDate(java.time.LocalDate.now());
            }
            
            memberDAO.save(member);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Success", "Registration successful! Please login with your Member ID."));
            
            // Clear the form
            member = new Member();
            loadMembers();
            return "login?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Failed to register: " + e.getMessage()));
            return null;
        }
    }
    
    public String login() {
        try {
            if (loginMemberId == null || loginMemberId.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Please enter your Member ID"));
                return null;
            }
            
            Member foundMember = memberDAO.findByMemberId(loginMemberId.trim());
            if (foundMember == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Member ID not found! Please check your Member ID or sign up."));
                return null;
            }
            
            // Login successful
            loggedInMember = foundMember;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Success", "Welcome, " + loggedInMember.getFullName() + "!"));
            
            loginMemberId = null;
            return "home?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Login failed: " + e.getMessage()));
            return null;
        }
    }
    
    public String logout() {
        loggedInMember = null;
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Info", "You have been logged out successfully."));
        return "home?faces-redirect=true";
    }
    
    public boolean isLoggedIn() {
        return loggedInMember != null;
    }
    
    public List<Member> getMembers() {
        if (members == null) {
            loadMembers();
        }
        return members;
    }
    
    public Member getMember() {
        return member;
    }
    
    public void setMember(Member member) {
        this.member = member;
    }
    
    public Member getLoggedInMember() {
        return loggedInMember;
    }
    
    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }
    
    public String getLoginMemberId() {
        return loginMemberId;
    }
    
    public void setLoginMemberId(String loginMemberId) {
        this.loginMemberId = loginMemberId;
    }
    
    public Member findMemberById(String memberId) {
        return memberDAO.findByMemberId(memberId);
    }
}

