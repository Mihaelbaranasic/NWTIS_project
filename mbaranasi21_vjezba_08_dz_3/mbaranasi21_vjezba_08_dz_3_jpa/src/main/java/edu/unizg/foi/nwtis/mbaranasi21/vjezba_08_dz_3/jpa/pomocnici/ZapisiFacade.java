package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Zapisi_;
import edu.unizg.foi.nwtis.podaci.Zapis;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.sql.Timestamp;

/**
 * Fasada za rad s Zapisi entitetima
 * 
 * @author mbaranasi21
 */
@Stateless
public class ZapisiFacade extends EntityManagerProducer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = getEntityManager().getCriteriaBuilder();
    }

    public void create(Zapisi zapis) {
        getEntityManager().persist(zapis);
    }

    public void edit(Zapisi zapis) {
        getEntityManager().merge(zapis);
    }

    public void remove(Zapisi zapis) {
        getEntityManager().remove(getEntityManager().merge(zapis));
    }

    public Zapisi find(Object id) {
        return getEntityManager().find(Zapisi.class, id);
    }

    public List<Zapisi> findAll() {
        CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
        cq.select(cq.from(Zapisi.class));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     * Pronalazi zapise za određenog korisnika u vremenskom rasponu
     */
    public List<Zapisi> findByUserAndTimeRange(String korisnickoIme, long vrijemeOd, long vrijemeDo) {
        CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
        Root<Zapisi> zapisi = cq.from(Zapisi.class);
        
        Expression<String> zaKorisnika = zapisi.get(Zapisi_.korisnickoime);
        Expression<Timestamp> zaVrijeme = zapisi.get(Zapisi_.vrijeme);
        
        cq.where(cb.and(
            cb.equal(zaKorisnika, korisnickoIme),
            cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo))
        ));
        
        TypedQuery<Zapisi> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Pronalazi sve zapise u vremenskom rasponu
     */
    public List<Zapisi> findByTimeRange(long vrijemeOd, long vrijemeDo) {
        CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
        Root<Zapisi> zapisi = cq.from(Zapisi.class);
        
        Expression<Timestamp> zaVrijeme = zapisi.get(Zapisi_.vrijeme);
        
        cq.where(cb.between(zaVrijeme, new Timestamp(vrijemeOd), new Timestamp(vrijemeDo)));
        
        TypedQuery<Zapisi> q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    public int count() {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(Zapisi.class)));
        return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
    }

    /**
     * Pretvara JPA entitet u POJO objekt
     */
    public Zapis pretvori(Zapisi z) {
        if (z == null) {
            return null;
        }
        return new Zapis(
            z.getId(),
            z.getVrijeme() != null ? z.getVrijeme().getTime() : 0L,
            z.getKorisnickoime(),
            z.getAdresaracunala(),
            z.getIpadresaracunala(),
            z.getOpisrada()
        );
    }

    /**
     * Pretvara POJO objekt u JPA entitet
     */
    public Zapisi pretvori(Zapis z) {
        if (z == null) {
            return null;
        }
        var zE = new Zapisi();
        zE.setId(z.id());
        zE.setVrijeme(new Timestamp(z.vrijeme()));
        zE.setKorisnickoime(z.korisnickoIme());
        zE.setAdresaracunala(z.adresaRacunala());
        zE.setIpadresaracunala(z.ipAdresaRacunala());
        zE.setOpisrada(z.opisRada());
        return zE;
    }

    /**
     * Pretvara listu JPA entiteta u listu POJO objekata
     */
    public List<Zapis> pretvori(List<Zapisi> zapisiE) {
        List<Zapis> zapisi = new ArrayList<>();
        for (Zapisi zEntitet : zapisiE) {
            var zObjekt = pretvori(zEntitet);
            zapisi.add(zObjekt);
        }
        return zapisi;
    }
}