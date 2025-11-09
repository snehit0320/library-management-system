package com.library.bean;

import com.library.dao.BookDAO;
import com.library.model.Book;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named(value = "bookBean")
@SessionScoped
public class BookBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Book book = new Book();
    private BookDAO bookDAO = new BookDAO();
    private List<Book> books;
    
    public BookBean() {
        loadBooks();
    }
    
    public String addBook() {
        try {
            if (book.getAvailableCopies() == null) {
                book.setAvailableCopies(book.getQuantity());
            }
            bookDAO.save(book);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Success", "Book added successfully!"));
            book = new Book();
            loadBooks();
            return "home?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error", "Failed to add book: " + e.getMessage()));
            return null;
        }
    }
    
    public void loadBooks() {
        books = bookDAO.findAll();
    }
    
    public void onPageLoad(ComponentSystemEvent event) {
        loadBooks();
    }
    
    public List<Book> getBooks() {
        if (books == null) {
            loadBooks();
        }
        return books;
    }
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public String navigateToAddBook() {
        book = new Book();
        return "addBook?faces-redirect=true";
    }
}

