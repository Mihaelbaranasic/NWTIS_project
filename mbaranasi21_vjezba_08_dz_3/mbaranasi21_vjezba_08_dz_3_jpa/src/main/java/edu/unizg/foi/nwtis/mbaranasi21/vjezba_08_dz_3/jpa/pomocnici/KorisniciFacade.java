/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Korisnici_;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author Dragutin Kermek
 */
@Stateless
public class KorisniciFacade extends EntityManagerProducer implements Serializable {
  private static final long serialVersionUID = 3595041786540495885L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  public void create(Korisnici korisnici) {
    getEntityManager().persist(korisnici);
  }

  public void edit(Korisnici korisnici) {
    getEntityManager().merge(korisnici);
  }

  public void remove(Korisnici korisnici) {
    getEntityManager().remove(getEntityManager().merge(korisnici));
  }

  public Korisnici find(Object id) {
    return getEntityManager().find(Korisnici.class, id);
  }

  public List<Korisnici> findAll() {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    cq.select(cq.from(Korisnici.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  public List<Korisnici> findRange(int[] range) {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    cq.select(cq.from(Korisnici.class));
    TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
    q.setMaxResults(range[1] - range[0]);
    q.setFirstResult(range[0]);
    return q.getResultList();
  }

  public Korisnici find(String korisnickoIme, String lozinka) {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    Root<Korisnici> korisnici = cq.from(Korisnici.class);
    Expression<String> zaKorisnik = korisnici.get(Korisnici_.korisnik);
    Expression<String> zaLozinku = korisnici.get(Korisnici_.lozinka);
    cq.where(cb.and(cb.equal(zaKorisnik, korisnickoIme), cb.equal(zaLozinku, lozinka)));
    TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
    return q.getResultList().getFirst();
  }

  public List<Korisnici> findAll(String prezime, String ime) {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    Root<Korisnici> korisnici = cq.from(Korisnici.class);
    Expression<String> zaPrezime = korisnici.get(Korisnici_.prezime);
    Expression<String> zaIme = korisnici.get(Korisnici_.ime);
    cq.where(cb.and(cb.like(zaPrezime, prezime), cb.like(zaIme, ime)));
    TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  public int count() {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    cq.select(cb.count(cq.from(Korisnici.class)));
    return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
  }

  public Korisnik pretvori(Korisnici k) {
    if (k == null) {
      return null;
    }
    var kObjekt =
        new Korisnik(k.getKorisnik(), k.getLozinka(), k.getPrezime(), k.getIme(), k.getEmail());

    return kObjekt;
  }

  public Korisnici pretvori(Korisnik k) {
    if (k == null) {
      return null;
    }
    var kE = new Korisnici();
    kE.setKorisnik(k.korisnik());
    kE.setLozinka(k.lozinka());
    kE.setPrezime(k.prezime());
    kE.setIme(k.ime());
    kE.setEmail(k.email());

    return kE;
  }

  public List<Korisnik> pretvori(List<Korisnici> korisniciE) {
    List<Korisnik> korisnici = new ArrayList<>();
    for (Korisnici kEntitet : korisniciE) {
      var kObjekt = pretvori(kEntitet);

      korisnici.add(kObjekt);
    }

    return korisnici;
  }

}
