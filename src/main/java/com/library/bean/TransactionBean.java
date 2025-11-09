package com.library.bean;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named(value = "transactionBean")
@SessionScoped
public class TransactionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Transaction transaction = new Transaction();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private BookDAO bookDAO = new BookDAO();
    private MemberDAO memberDAO = new MemberDAO();
    
    private String selectedBookId;
    private String selectedMemberId;
    private LocalDate issueDate = LocalDate.now();
    private LocalDate dueDate = LocalDate.now().plusDays(14);
    
    private List<Transaction> issuedBooks;
    private List<Transaction> overdueBooks;
    
    public String issueBook() {
        try {
            Book book = bookDAO.findById(Long.parseLong(selectedBookId));
            
            if (selectedMemberId == null || selectedMemberId.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Member ID is required! Please login first."));
                return null;
            }
            
            Member member = memberDAO.findByMemberId(selectedMemberId);
            
            if (book == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Book not found!"));
                return null;
            }
            
            if (book.getAvailableCopies() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "No copies available for this book!"));
                return null;
            }
            
            transaction = new Transaction(book, member, issueDate, dueDate);
            transactionDAO.save(transaction);
            
            // Update available copies
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookDAO.update(book);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Success", "Book issued successfully!"));
            
            resetForm();
            return "home?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Failed to issue book: " + e.getMessage()));
            return null;
        }
    }
    
    public String returnBook() {
        try {
            Transaction trans = transactionDAO.findById(transaction.getId());
            
            if (trans == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Transaction not found!"));
                return null;
            }
            
            if (trans.getStatus() == Transaction.TransactionStatus.RETURNED) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Error", "Book already returned!"));
                return null;
            }
            
            LocalDate returnDate = LocalDate.now();
            trans.setReturnDate(returnDate);
            trans.setStatus(Transaction.TransactionStatus.RETURNED);
            
            // Calculate fine if overdue: 5 rupees per day
            if (returnDate.isAfter(trans.getDueDate())) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    trans.getDueDate(), returnDate);
                trans.setFineAmount(daysOverdue * 5.0); // 5 rupees per day
            } else {
                trans.setFineAmount(0.0);
            }
            
            transactionDAO.update(trans);
            
            // Update available copies
            Book book = trans.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookDAO.update(book);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Success", "Book returned successfully!"));
            
            resetForm();
            loadReports();
            return "reports?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Failed to return book: " + e.getMessage()));
            return null;
        }
    }
    
    public void loadReports() {
        issuedBooks = transactionDAO.findIssuedBooks();
        overdueBooks = transactionDAO.findOverdueBooks();
        
        // Update status and calculate fine for overdue books
        for (Transaction trans : overdueBooks) {
            if (trans.getStatus() != Transaction.TransactionStatus.OVERDUE) {
                trans.setStatus(Transaction.TransactionStatus.OVERDUE);
            }
            // Calculate fine amount: 5 rupees per day overdue
            if (trans.getDueDate() != null && LocalDate.now().isAfter(trans.getDueDate())) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    trans.getDueDate(), LocalDate.now());
                trans.setFineAmount(daysOverdue * 5.0); // 5 rupees per day
                transactionDAO.update(trans);
            }
        }
        
        // Also update issued books that are overdue but status hasn't been updated yet
        for (Transaction trans : issuedBooks) {
            if (trans.getDueDate() != null && LocalDate.now().isAfter(trans.getDueDate())) {
                if (trans.getStatus() != Transaction.TransactionStatus.OVERDUE) {
                    trans.setStatus(Transaction.TransactionStatus.OVERDUE);
                }
                // Calculate fine amount: 5 rupees per day overdue
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                    trans.getDueDate(), LocalDate.now());
                trans.setFineAmount(daysOverdue * 5.0); // 5 rupees per day
                transactionDAO.update(trans);
            }
        }
    }
    
    public void onPageLoad(ComponentSystemEvent event) {
        loadReports();
    }
    
    public void setLoggedInMemberId(ComponentSystemEvent event) {
        // Get logged-in member from session
        jakarta.faces.context.FacesContext facesContext = FacesContext.getCurrentInstance();
        jakarta.el.ELContext elContext = facesContext.getELContext();
        jakarta.el.ExpressionFactory factory = facesContext.getApplication().getExpressionFactory();
        jakarta.el.ValueExpression ve = factory.createValueExpression(elContext, "#{memberBean.loggedInMember}", com.library.model.Member.class);
        com.library.model.Member loggedInMember = (com.library.model.Member) ve.getValue(elContext);
        
        if (loggedInMember != null) {
            selectedMemberId = loggedInMember.getMemberId();
        }
    }
    
    public List<Transaction> getIssuedBooks() {
        if (issuedBooks == null) {
            loadReports();
        }
        return issuedBooks;
    }
    
    public List<Transaction> getOverdueBooks() {
        if (overdueBooks == null) {
            loadReports();
        }
        return overdueBooks;
    }
    
    // Get all books that can be returned (issued + overdue) - no duplicates
    public List<Transaction> getBooksToReturn() {
        loadReports();
        // Use a Map to remove duplicates by transaction ID
        java.util.Map<Long, Transaction> uniqueBooks = new java.util.HashMap<>();
        if (issuedBooks != null) {
            for (Transaction trans : issuedBooks) {
                if (trans != null && trans.getId() != null) {
                    uniqueBooks.put(trans.getId(), trans);
                }
            }
        }
        if (overdueBooks != null) {
            for (Transaction trans : overdueBooks) {
                if (trans != null && trans.getId() != null) {
                    uniqueBooks.put(trans.getId(), trans);
                }
            }
        }
        return new java.util.ArrayList<>(uniqueBooks.values());
    }
    
    // Calculate fine for a transaction
    public double calculateFine(Transaction trans) {
        if (trans != null && trans.getDueDate() != null && LocalDate.now().isAfter(trans.getDueDate())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                trans.getDueDate(), LocalDate.now());
            return daysOverdue * 5.0; // 5 rupees per day
        }
        return 0.0;
    }
    
    // Check if transaction is overdue (safe null check)
    public boolean isOverdue(Transaction trans) {
        if (trans == null) {
            return false;
        }
        if (trans.getStatus() == Transaction.TransactionStatus.OVERDUE) {
            return true;
        }
        if (trans.getDueDate() == null) {
            return false;
        }
        return LocalDate.now().isAfter(trans.getDueDate());
    }
    
    // Get fine amount safely
    public double getFineAmount(Transaction trans) {
        if (trans == null) {
            return 0.0;
        }
        if (trans.getFineAmount() != null && trans.getFineAmount() > 0) {
            return trans.getFineAmount();
        }
        return calculateFine(trans);
    }
    
    // Get days overdue safely
    public long getDaysOverdue(Transaction trans) {
        if (trans == null || trans.getDueDate() == null) {
            return 0;
        }
        if (LocalDate.now().isAfter(trans.getDueDate())) {
            return java.time.temporal.ChronoUnit.DAYS.between(
                trans.getDueDate(), LocalDate.now());
        }
        return 0;
    }
    
    public void resetForm() {
        transaction = new Transaction();
        selectedBookId = null;
        selectedMemberId = null;
        issueDate = LocalDate.now();
        dueDate = LocalDate.now().plusDays(14);
    }
    
    public Transaction getTransaction() {
        return transaction;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    
    public String getSelectedBookId() {
        return selectedBookId;
    }
    
    public void setSelectedBookId(String selectedBookId) {
        this.selectedBookId = selectedBookId;
    }
    
    public String getSelectedMemberId() {
        return selectedMemberId;
    }
    
    public void setSelectedMemberId(String selectedMemberId) {
        this.selectedMemberId = selectedMemberId;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}

