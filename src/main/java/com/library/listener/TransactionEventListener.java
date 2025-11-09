package com.library.listener;

import com.library.dao.TransactionDAO;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.context.FacesContext;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@Named(value = "transactionEventListener")
@RequestScoped
public class TransactionEventListener implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TransactionEventListener.class.getName());
    private TransactionDAO transactionDAO = new TransactionDAO();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Handles actions when a book is borrowed/issued
     * Performs logging, validation, and audit trail creation
     */
    public void handleBorrowAction(ActionEvent event) {
        try {
            // Get transaction bean from EL context
            Transaction transaction = getTransactionFromContext();
            Book book = getBookFromContext();
            Member member = getMemberFromContext();
            
            if (transaction != null && book != null && member != null) {
                // Log the transaction
                logBorrowTransaction(transaction, book, member);
                
                // Check if member has overdue books
                checkMemberOverdueBooks(member);
                
                // Validate borrowing limits
                validateBorrowingLimits(member);
                
                // Create audit message
                String auditMessage = String.format(
                    "BOOK ISSUED - Transaction ID: %d | Book: %s (ISBN: %s) | Member: %s (%s) | Issue Date: %s | Due Date: %s",
                    transaction.getId() != null ? transaction.getId() : 0,
                    book.getTitle(),
                    book.getIsbn(),
                    member.getFullName(),
                    member.getMemberId(),
                    transaction.getIssueDate() != null ? transaction.getIssueDate().format(DATE_FORMATTER) : "N/A",
                    transaction.getDueDate() != null ? transaction.getDueDate().format(DATE_FORMATTER) : "N/A"
                );
                
                logger.info(auditMessage);
                System.out.println("=== TRANSACTION AUDIT ===");
                System.out.println(auditMessage);
                System.out.println("=========================");
                
                // Check and update overdue status for all transactions
                updateOverdueStatus();
                
            } else {
                logger.warning("Borrow action triggered but transaction data not available");
                System.out.println("Book borrow action triggered - Transaction data not yet available");
            }
        } catch (Exception e) {
            logger.severe("Error in handleBorrowAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles actions when a book is returned
     * Performs logging, fine calculation, and audit trail creation
     */
    public void handleReturnAction(ActionEvent event) {
        try {
            // Get transaction bean from EL context
            Transaction transaction = getTransactionFromContext();
            
            if (transaction != null && transaction.getId() != null) {
                // Fetch the full transaction from database
                Transaction fullTransaction = transactionDAO.findById(transaction.getId());
                
                if (fullTransaction != null) {
                    Book book = fullTransaction.getBook();
                    Member member = fullTransaction.getMember();
                    
                    // Calculate fine if overdue
                    double fineAmount = calculateFine(fullTransaction);
                    
                    // Log the return transaction
                    logReturnTransaction(fullTransaction, book, member, fineAmount);
                    
                    // Create audit message
                    String statusMessage = fullTransaction.getStatus() == Transaction.TransactionStatus.OVERDUE 
                        ? "OVERDUE RETURN" : "ON-TIME RETURN";
                    
                    String auditMessage = String.format(
                        "BOOK RETURNED - Transaction ID: %d | Book: %s (ISBN: %s) | Member: %s (%s) | Return Date: %s | Fine: ₹%.2f | Status: %s",
                        fullTransaction.getId(),
                        book.getTitle(),
                        book.getIsbn(),
                        member.getFullName(),
                        member.getMemberId(),
                        LocalDate.now().format(DATE_FORMATTER),
                        fineAmount,
                        statusMessage
                    );
                    
                    logger.info(auditMessage);
                    System.out.println("=== TRANSACTION AUDIT ===");
                    System.out.println(auditMessage);
                    System.out.println("=========================");
                    
                    // Update overdue status for remaining transactions
                    updateOverdueStatus();
                    
                    // Log member's current borrowing status
                    logMemberBorrowingStatus(member);
                } else {
                    logger.warning("Return action triggered but transaction not found in database");
                    System.out.println("Book return action triggered - Transaction not found");
                }
            } else {
                logger.warning("Return action triggered but transaction data not available");
                System.out.println("Book return action triggered - Transaction data not yet available");
            }
        } catch (Exception e) {
            logger.severe("Error in handleReturnAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets transaction from FacesContext EL
     */
    private Transaction getTransactionFromContext() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                ELContext elContext = facesContext.getELContext();
                ExpressionFactory factory = facesContext.getApplication().getExpressionFactory();
                ValueExpression ve = factory.createValueExpression(elContext, 
                    "#{transactionBean.transaction}", Transaction.class);
                return (Transaction) ve.getValue(elContext);
            }
        } catch (Exception e) {
            logger.warning("Could not get transaction from context: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Gets book from transaction or context
     */
    private Book getBookFromContext() {
        try {
            Transaction transaction = getTransactionFromContext();
            if (transaction != null && transaction.getBook() != null) {
                return transaction.getBook();
            }
            
            // Try to get from selected book ID
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                ELContext elContext = facesContext.getELContext();
                ExpressionFactory factory = facesContext.getApplication().getExpressionFactory();
                ValueExpression ve = factory.createValueExpression(elContext, 
                    "#{bookBean.selectedBook}", Book.class);
                Book book = (Book) ve.getValue(elContext);
                if (book == null) {
                    // Try getting from selectedBookId
                    ValueExpression ve2 = factory.createValueExpression(elContext, 
                        "#{transactionBean.selectedBookId}", String.class);
                    String bookId = (String) ve2.getValue(elContext);
                    if (bookId != null && !bookId.isEmpty()) {
                        // We would need BookDAO here, but for now return null
                        return null;
                    }
                }
                return book;
            }
        } catch (Exception e) {
            logger.warning("Could not get book from context: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Gets member from transaction or context
     */
    private Member getMemberFromContext() {
        try {
            Transaction transaction = getTransactionFromContext();
            if (transaction != null && transaction.getMember() != null) {
                return transaction.getMember();
            }
            
            // Try to get logged in member
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                ELContext elContext = facesContext.getELContext();
                ExpressionFactory factory = facesContext.getApplication().getExpressionFactory();
                ValueExpression ve = factory.createValueExpression(elContext, 
                    "#{memberBean.loggedInMember}", Member.class);
                return (Member) ve.getValue(elContext);
            }
        } catch (Exception e) {
            logger.warning("Could not get member from context: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Logs borrow transaction details
     */
    private void logBorrowTransaction(Transaction transaction, Book book, Member member) {
        String logMessage = String.format(
            "BORROW EVENT: Member %s (%s) borrowed book '%s' (ISBN: %s). Available copies: %d/%d",
            member.getFullName(),
            member.getMemberId(),
            book.getTitle(),
            book.getIsbn(),
            book.getAvailableCopies() != null ? book.getAvailableCopies() : 0,
            book.getQuantity() != null ? book.getQuantity() : 0
        );
        logger.info(logMessage);
    }
    
    /**
     * Logs return transaction details
     */
    private void logReturnTransaction(Transaction transaction, Book book, Member member, double fine) {
        long daysOverdue = 0;
        if (transaction.getDueDate() != null && LocalDate.now().isAfter(transaction.getDueDate())) {
            daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                transaction.getDueDate(), LocalDate.now());
        }
        
        String logMessage = String.format(
            "RETURN EVENT: Member %s (%s) returned book '%s' (ISBN: %s). Days overdue: %d, Fine: ₹%.2f. Available copies: %d/%d",
            member.getFullName(),
            member.getMemberId(),
            book.getTitle(),
            book.getIsbn(),
            daysOverdue,
            fine,
            book.getAvailableCopies() != null ? book.getAvailableCopies() : 0,
            book.getQuantity() != null ? book.getQuantity() : 0
        );
        logger.info(logMessage);
    }
    
    /**
     * Checks if member has overdue books and logs warning
     */
    private void checkMemberOverdueBooks(Member member) {
        try {
            List<Transaction> allTransactions = transactionDAO.findAll();
            long overdueCount = allTransactions.stream()
                .filter(t -> t.getMember() != null && 
                            t.getMember().getId() != null &&
                            t.getMember().getId().equals(member.getId()) &&
                            t.getStatus() != Transaction.TransactionStatus.RETURNED &&
                            t.getDueDate() != null &&
                            LocalDate.now().isAfter(t.getDueDate()))
                .count();
            
            if (overdueCount > 0) {
                String warning = String.format(
                    "WARNING: Member %s (%s) has %d overdue book(s). Please return them before borrowing new books.",
                    member.getFullName(),
                    member.getMemberId(),
                    overdueCount
                );
                logger.warning(warning);
                System.out.println("⚠️ " + warning);
            }
        } catch (Exception e) {
            logger.warning("Error checking member overdue books: " + e.getMessage());
        }
    }
    
    /**
     * Validates borrowing limits (e.g., max books per member)
     */
    private void validateBorrowingLimits(Member member) {
        try {
            List<Transaction> allTransactions = transactionDAO.findAll();
            long activeBorrows = allTransactions.stream()
                .filter(t -> t.getMember() != null && 
                            t.getMember().getId() != null &&
                            t.getMember().getId().equals(member.getId()) &&
                            t.getStatus() != Transaction.TransactionStatus.RETURNED)
                .count();
            
            // Assuming max 5 books per member (can be configured)
            int maxBooks = 5;
            if (activeBorrows >= maxBooks) {
                String warning = String.format(
                    "WARNING: Member %s (%s) has reached borrowing limit (%d books). Current active borrows: %d",
                    member.getFullName(),
                    member.getMemberId(),
                    maxBooks,
                    activeBorrows
                );
                logger.warning(warning);
                System.out.println("⚠️ " + warning);
            } else {
                logger.info(String.format(
                    "Member %s (%s) has %d active borrow(s) out of %d allowed",
                    member.getFullName(),
                    member.getMemberId(),
                    activeBorrows,
                    maxBooks
                ));
            }
        } catch (Exception e) {
            logger.warning("Error validating borrowing limits: " + e.getMessage());
        }
    }
    
    /**
     * Calculates fine for overdue transaction
     */
    private double calculateFine(Transaction transaction) {
        if (transaction.getDueDate() == null) {
            return 0.0;
        }
        
        LocalDate returnDate = transaction.getReturnDate() != null 
            ? transaction.getReturnDate() 
            : LocalDate.now();
        
        if (returnDate.isAfter(transaction.getDueDate())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                transaction.getDueDate(), returnDate);
            return daysOverdue * 5.0; // 5 rupees per day
        }
        return 0.0;
    }
    
    /**
     * Updates overdue status for all transactions
     */
    private void updateOverdueStatus() {
        try {
            List<Transaction> allTransactions = transactionDAO.findAll();
            LocalDate today = LocalDate.now();
            int updatedCount = 0;
            
            for (Transaction trans : allTransactions) {
                if (trans.getStatus() != Transaction.TransactionStatus.RETURNED &&
                    trans.getDueDate() != null &&
                    today.isAfter(trans.getDueDate()) &&
                    trans.getStatus() != Transaction.TransactionStatus.OVERDUE) {
                    
                    trans.setStatus(Transaction.TransactionStatus.OVERDUE);
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                        trans.getDueDate(), today);
                    trans.setFineAmount(daysOverdue * 5.0);
                    transactionDAO.update(trans);
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                logger.info(String.format("Updated %d transaction(s) to OVERDUE status", updatedCount));
            }
        } catch (Exception e) {
            logger.warning("Error updating overdue status: " + e.getMessage());
        }
    }
    
    /**
     * Logs member's current borrowing status
     */
    private void logMemberBorrowingStatus(Member member) {
        try {
            List<Transaction> allTransactions = transactionDAO.findAll();
            long activeBorrows = allTransactions.stream()
                .filter(t -> t.getMember() != null && 
                            t.getMember().getId() != null &&
                            t.getMember().getId().equals(member.getId()) &&
                            t.getStatus() != Transaction.TransactionStatus.RETURNED)
                .count();
            
            long overdueBorrows = allTransactions.stream()
                .filter(t -> t.getMember() != null && 
                            t.getMember().getId() != null &&
                            t.getMember().getId().equals(member.getId()) &&
                            t.getStatus() == Transaction.TransactionStatus.OVERDUE)
                .count();
            
            logger.info(String.format(
                "Member %s (%s) currently has %d active borrow(s), %d overdue",
                member.getFullName(),
                member.getMemberId(),
                activeBorrows,
                overdueBorrows
            ));
        } catch (Exception e) {
            logger.warning("Error logging member borrowing status: " + e.getMessage());
        }
    }
}

