package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Partneri_;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

/**
 * Fasada za rad s Partner entitetima
 * 
 * @author mbaranasi21
 */
@Stateless
public class PartneriFacade extends EntityManagerProducer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = getEntityManager().getCriteriaBuilder();
    }

    public void create(Partneri partner) {
        getEntityManager().persist(partner);
    }

    public void edit(Partneri partner) {
        getEntityManager().merge(partner);
    }

    public void remove(Partneri partner) {
        getEntityManager().remove(getEntityManager().merge(partner));
    }

    public Partneri find(Object id) {
        return getEntityManager().find(Partneri.class, id);
    }

    public List<Partneri> findAll() {
        CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
        cq.select(cq.from(Partneri.class));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public int count() {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(Partneri.class)));
        return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
    }

    /**
     * Pretvara JPA entitet u POJO objekt
     */
    public Partner pretvori(Partneri p) {
        if (p == null) {
            return null;
        }
        return new Partner(p.getId(), p.getNaziv(), p.getVrstakuhinje(), 
                          p.getAdresa(), p.getMreznavrata(), p.getMreznavratakraj(),
                          (float)p.getGpssirina(), (float)p.getGpsduzina(), 
                          p.getSigurnosnikod(), p.getAdminkod());
    }

    /**
     * Pretvara POJO objekt u JPA entitet
     */
    public Partneri pretvori(Partner p) {
        if (p == null) {
            return null;
        }
        var pE = new Partneri();
        pE.setId(p.id());
        pE.setNaziv(p.naziv());
        pE.setVrstakuhinje(p.vrstaKuhinje());
        pE.setAdresa(p.adresa());
        pE.setMreznavrata(p.mreznaVrata());
        pE.setMreznavratakraj(p.mreznaVrataKraj());
        pE.setGpssirina(p.gpsSirina());
        pE.setGpsduzina(p.gpsDuzina());
        pE.setSigurnosnikod(p.sigurnosniKod());
        pE.setAdminkod(p.adminKod());
        return pE;
    }

    /**
     * Pretvara listu JPA entiteta u listu POJO objekata
     */
    public List<Partner> pretvori(List<Partneri> partneriE) {
        List<Partner> partneri = new ArrayList<>();
        for (Partneri pEntitet : partneriE) {
            var pObjekt = pretvori(pEntitet);
            partneri.add(pObjekt);
        }
        return partneri;
    }
}