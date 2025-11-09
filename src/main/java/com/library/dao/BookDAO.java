package com.library.dao;

import com.library.model.Book;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class BookDAO {
    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    
    public void save(Book book) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    public Book findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Book.class, id);
        } finally {
            session.close();
        }
    }
    
    public Book findByIsbn(String isbn) {
        Session session = sessionFactory.openSession();
        try {
            Query<Book> query = session.createQuery("FROM Book WHERE isbn = :isbn", Book.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
    
    public List<Book> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM Book", Book.class).list();
        } finally {
            session.close();
        }
    }
    
    public void update(Book book) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    public void delete(Book book) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.remove(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}

