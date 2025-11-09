package com.library.dao;

import com.library.model.Transaction;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;

public class TransactionDAO {
    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    
    public void save(Transaction transaction) {
        Session session = sessionFactory.openSession();
        org.hibernate.Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    public Transaction findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Transaction.class, id);
        } finally {
            session.close();
        }
    }
    
    public List<Transaction> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM Transaction", Transaction.class).list();
        } finally {
            session.close();
        }
    }
    
    public List<Transaction> findIssuedBooks() {
        Session session = sessionFactory.openSession();
        try {
            Query<Transaction> query = session.createQuery(
                "FROM Transaction WHERE status IN (:status1, :status2)", Transaction.class);
            query.setParameter("status1", Transaction.TransactionStatus.ISSUED);
            query.setParameter("status2", Transaction.TransactionStatus.OVERDUE);
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public List<Transaction> findOverdueBooks() {
        Session session = sessionFactory.openSession();
        try {
            Query<Transaction> query = session.createQuery(
                "FROM Transaction WHERE status IN (:status1, :status2) AND dueDate < :today", Transaction.class);
            query.setParameter("status1", Transaction.TransactionStatus.ISSUED);
            query.setParameter("status2", Transaction.TransactionStatus.OVERDUE);
            query.setParameter("today", LocalDate.now());
            return query.list();
        } finally {
            session.close();
        }
    }
    
    public void update(Transaction transaction) {
        Session session = sessionFactory.openSession();
        org.hibernate.Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}

