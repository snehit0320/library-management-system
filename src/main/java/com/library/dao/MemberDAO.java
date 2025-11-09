package com.library.dao;

import com.library.model.Member;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class MemberDAO {
    private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    
    public void save(Member member) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(member);
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
    
    public Member findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Member.class, id);
        } finally {
            session.close();
        }
    }
    
    public Member findByMemberId(String memberId) {
        Session session = sessionFactory.openSession();
        try {
            Query<Member> query = session.createQuery("FROM Member WHERE memberId = :memberId", Member.class);
            query.setParameter("memberId", memberId);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
    
    public Member findByEmail(String email) {
        Session session = sessionFactory.openSession();
        try {
            Query<Member> query = session.createQuery("FROM Member WHERE email = :email", Member.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
    
    public List<Member> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM Member", Member.class).list();
        } finally {
            session.close();
        }
    }
    
    public void update(Member member) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(member);
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

