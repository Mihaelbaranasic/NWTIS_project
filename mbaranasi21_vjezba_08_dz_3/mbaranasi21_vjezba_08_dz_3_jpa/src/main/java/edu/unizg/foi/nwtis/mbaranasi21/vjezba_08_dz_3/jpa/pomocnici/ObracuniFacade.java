package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Obracuni_;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.podaci.Obracun;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import java.sql.Timestamp;

/**
 * Fasada za rad s Obracun entitetima
 * 
 * @author mbaranasi21
 */
@Stateless
public class ObracuniFacade extends EntityManagerProducer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = getEntityManager().getCriteriaBuilder();
    }

    public void create(Obracuni obracun) {
        getEntityManager().persist(obracun);
    }

    public void edit(Obracuni obracun) {
        getEntityManager().merge(obracun);
    }

    public void remove(Obracuni obracun) {
        getEntityManager().remove(getEntityManager().merge(obracun));
    }

    public Obracuni find(Object id) {
        return getEntityManager().find(Obracuni.class, id);
    }

    public List<Obracuni> findAll() {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        cq.select(cq.from(Obracuni.class));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Pronalazi obračune za određenog partnera u vremenskom rasponu
     */
    public List<Obracuni> findByPartnerAndTimeRange(int partnerId, long vrijemeOd, long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        Join<Obracuni, Partneri> partnerJoin = obracuni.join(Obracuni_.partneri);
        
        Expression<Integer> zaPartner = partnerJoin.get("id");
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        
        cq.where(cb.and(
            cb.equal(zaPartner, partnerId),
            cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo))
        ));
        
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Pronalazi sve obračune u vremenskom rasponu
     */
    public List<Obracuni> findByTimeRange(long vrijemeOd, long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        
        cq.where(cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo)));
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    public int count() {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(Obracuni.class)));
        return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
    }

    /**
     * Pretvara JPA entitet u POJO objekt
     */
    public Obracun pretvori(Obracuni o) {
        if (o == null) {
            return null;
        }
        return new Obracun(
            o.getPartneri() != null ? o.getPartneri().getId() : 0,
            o.getId(), 
            o.getJelo(), 
            (float)o.getKolicina(), 
            (float)o.getCijena(), 
            o.getVrijeme() != null ? o.getVrijeme().getTime() : 0L
        );
    }

    /**
     * Pretvara POJO objekt u JPA entitet
     */
    public Obracuni pretvori(Obracun o) {
        if (o == null) {
            return null;
        }
        var oE = new Obracuni();
        oE.setId(o.id());
        oE.setJelo(o.jelo());
        oE.setKolicina(o.kolicina());
        oE.setCijena(o.cijena());
        oE.setVrijeme(new Timestamp(o.vrijeme()));
        return oE;
    }

    /**
     * Pretvara POJO objekt u JPA entitet s postavljenim partnerom
     */
    public Obracuni pretvori(Obracun o, Partneri partner) {
        if (o == null) {
            return null;
        }
        var oE = pretvori(o);
        oE.setPartneri(partner);
        return oE;
    }

    /**
     * Pretvara listu JPA entiteta u listu POJO objekata
     */
    public List<Obracun> pretvori(List<Obracuni> obracuniE) {
        List<Obracun> obracuni = new ArrayList<>();
        for (Obracuni oEntitet : obracuniE) {
            var oObjekt = pretvori(oEntitet);
            obracuni.add(oObjekt);
        }
        return obracuni;
    }
    
    /**
     * Pronalazi obračune samo za jela u vremenskom rasponu
     * 
     * @param vrijemeOd početno vrijeme filtera (unix timestamp)
     * @param vrijemeDo završno vrijeme filtera (unix timestamp)
     * @return lista obračun entiteta za jela
     */
    public List<Obracuni> findJelaByTimeRange(Long vrijemeOd, Long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        Expression<Boolean> zaJelo = obracuni.get(Obracuni_.jelo);
        
        if (vrijemeOd != null && vrijemeDo != null) {
            cq.where(cb.and(
                cb.equal(zaJelo, true),
                cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo))
            ));
        } else {
            cq.where(cb.equal(zaJelo, true));
        }
        
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Pronalazi obračune samo za pića u vremenskom rasponu
     * 
     * @param vrijemeOd početno vrijeme filtera (unix timestamp)
     * @param vrijemeDo završno vrijeme filtera (unix timestamp)
     * @return lista obračun entiteta za pića
     */
    public List<Obracuni> findPicaByTimeRange(Long vrijemeOd, Long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        Expression<Boolean> zaJelo = obracuni.get(Obracuni_.jelo);
        
        if (vrijemeOd != null && vrijemeDo != null) {
            cq.where(cb.and(
                cb.equal(zaJelo, false),
                cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo))
            ));
        } else {
            cq.where(cb.equal(zaJelo, false));
        }
        
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Pronalazi obračune za određenog partnera u vremenskom rasponu
     * 
     * @param partnerId identifikator partnera
     * @param vrijemeOd početno vrijeme filtera (unix timestamp)
     * @param vrijemeDo završno vrijeme filtera (unix timestamp)
     * @return lista obračun entiteta za partnera
     */
    public List<Obracuni> findByPartnerAndTimeRange(int partnerId, Long vrijemeOd, Long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        Join<Obracuni, Partneri> partnerJoin = obracuni.join(Obracuni_.partneri);
        
        Expression<Integer> zaPartner = partnerJoin.get("id");
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        
        if (vrijemeOd != null && vrijemeDo != null) {
            cq.where(cb.and(
                cb.equal(zaPartner, partnerId),
                cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo))
            ));
        } else {
            cq.where(cb.equal(zaPartner, partnerId));
        }
        
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Pronalazi sve obračune u vremenskom rasponu
     * 
     * @param vrijemeOd početno vrijeme filtera (unix timestamp)
     * @param vrijemeDo završno vrijeme filtera (unix timestamp)
     * @return lista obračun entiteta
     */
    public List<Obracuni> findByTimeRange(Long vrijemeOd, Long vrijemeDo) {
        CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
        Root<Obracuni> obracuni = cq.from(Obracuni.class);
        
        Expression<Timestamp> zaVrijeme = obracuni.get(Obracuni_.vrijeme);
        
        if (vrijemeOd != null && vrijemeDo != null) {
            cq.where(cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo)));
        }
        
        cq.orderBy(cb.desc(zaVrijeme));
        
        TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }
}