package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Korisnici;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.entiteti.Korisnici_;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 * Fasada za rad s Korisnici entitetima
 * 
 * @author mbaranasi21
 */
@Stateless
public class KorisniciFacade extends EntityManagerProducer implements Serializable {
  private static final long serialVersionUID = 3595041786540495885L;

  private CriteriaBuilder cb;

  @PostConstruct
  private void init() {
    cb = getEntityManager().getCriteriaBuilder();
  }

  /**
   * Stvara novi korisnik entitet u bazi
   * 
   * @param korisnici entitet korisnika za stvaranje
   */
  public void create(Korisnici korisnici) {
    getEntityManager().persist(korisnici);
  }

  /**
   * Ažurira postojeći korisnik entitet
   * 
   * @param korisnici entitet korisnika za ažuriranje
   */
  public void edit(Korisnici korisnici) {
    getEntityManager().merge(korisnici);
  }

  /**
   * Briše korisnik entitet iz baze
   * 
   * @param korisnici entitet korisnika za brisanje
   */
  public void remove(Korisnici korisnici) {
    getEntityManager().remove(getEntityManager().merge(korisnici));
  }

  /**
   * Pronalazi korisnik entitet prema ID-u
   * 
   * @param id identifikator korisnika
   * @return korisnik entitet ili null ako nije pronađen
   */
  public Korisnici find(Object id) {
    return getEntityManager().find(Korisnici.class, id);
  }

  /**
   * Dohvaća sve korisnik entitete
   * 
   * @return lista svih korisnik entiteta
   */
  public List<Korisnici> findAll() {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    cq.select(cq.from(Korisnici.class));
    return getEntityManager().createQuery(cq).getResultList();
  }

  /**
   * Dohvaća korisnik entitete u određenom rasponu
   * 
   * @param range raspon [početak, kraj]
   * @return lista korisnik entiteta u rasponu
   */
  public List<Korisnici> findRange(int[] range) {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    cq.select(cq.from(Korisnici.class));
    TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
    q.setMaxResults(range[1] - range[0]);
    q.setFirstResult(range[0]);
    return q.getResultList();
  }

  /**
   * Pronalazi korisnika prema korisničkom imenu i lozinki
   * 
   * @param korisnickoIme korisničko ime
   * @param lozinka lozinka korisnika
   * @return korisnik entitet ili null ako nije pronađen
   */
  public Korisnici find(String korisnickoIme, String lozinka) {
    try {
      CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
      Root<Korisnici> korisnici = cq.from(Korisnici.class);
      Expression<String> zaKorisnik = korisnici.get(Korisnici_.korisnik);
      Expression<String> zaLozinku = korisnici.get(Korisnici_.lozinka);
      cq.where(cb.and(cb.equal(zaKorisnik, korisnickoIme), cb.equal(zaLozinku, lozinka)));
      TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
      List<Korisnici> rezultat = q.getResultList();
      return rezultat.isEmpty() ? null : rezultat.get(0);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Pronalazi korisnika prema korisničkom imenu
   * 
   * @param korisnickoIme korisničko ime
   * @return korisnik entitet ili null ako nije pronađen
   */
  public Korisnici find(String korisnickoIme) {
    try {
      CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
      Root<Korisnici> korisnici = cq.from(Korisnici.class);
      Expression<String> zaKorisnik = korisnici.get(Korisnici_.korisnik);
      cq.where(cb.equal(zaKorisnik, korisnickoIme));
      TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
      List<Korisnici> rezultat = q.getResultList();
      return rezultat.isEmpty() ? null : rezultat.get(0);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Pronalazi korisnika prema kriterijima - kompatibilnost s postojećim kodovima
   * 
   * @param korisnickoIme korisničko ime
   * @param lozinka lozinka korisnika
   * @param provjeriLozinku treba li provjeriti lozinku
   * @return korisnik entitet ili null ako nije pronađen
   */
  public Korisnici dohvati(String korisnickoIme, String lozinka, boolean provjeriLozinku) {
    if (provjeriLozinku && lozinka != null) {
      return find(korisnickoIme, lozinka);
    } else {
      return find(korisnickoIme);
    }
  }

  /**
   * Pretražuje korisnike prema prezimenu i imenu
   * 
   * @param prezime prezime za pretraživanje
   * @param ime ime za pretraživanje
   * @return lista korisnik entiteta koji odgovaraju kriterijima
   */
  public List<Korisnici> findAll(String prezime, String ime) {
    CriteriaQuery<Korisnici> cq = cb.createQuery(Korisnici.class);
    Root<Korisnici> korisnici = cq.from(Korisnici.class);
    Expression<String> zaPrezime = korisnici.get(Korisnici_.prezime);
    Expression<String> zaIme = korisnici.get(Korisnici_.ime);
    
    String prezimePattern = prezime.contains("%") ? prezime : "%" + prezime + "%";
    String imePattern = ime.contains("%") ? ime : "%" + ime + "%";
    
    cq.where(cb.and(cb.like(zaPrezime, prezimePattern), cb.like(zaIme, imePattern)));
    TypedQuery<Korisnici> q = getEntityManager().createQuery(cq);
    return q.getResultList();
  }

  /**
   * Računa ukupan broj korisnik entiteta
   * 
   * @return broj korisnik entiteta
   */
  public int count() {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    cq.select(cb.count(cq.from(Korisnici.class)));
    return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
  }

  /**
   * Dodaje novog korisnika u bazu
   * 
   * @param korisnik POJO objekt korisnika
   * @return true ako je uspješno dodano, inače false
   */
  public boolean dodaj(Korisnik korisnik) {
    try {
      Korisnici korisnikEntitet = pretvori(korisnik);
      create(korisnikEntitet);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Dohvaća sve korisnike kao POJO listu
   * 
   * @return lista korisnik POJO objekata
   */
  public List<Korisnik> dohvatiSve() {
    List<Korisnici> entiteti = findAll();
    return pretvori(entiteti);
  }

  /**
   * Pretvara JPA entitet u POJO objekt
   * 
   * @param k korisnik entitet
   * @return korisnik POJO objekt
   */
  public Korisnik pretvori(Korisnici k) {
	    if (k == null) {
	        return null;
	    }
	    
	    String grupa = "";
	    if (k.getGrupes() != null && !k.getGrupes().isEmpty()) {
	        grupa = k.getGrupes().get(0).getGrupa();
	    }
	    
	    return new Korisnik(k.getKorisnik(), k.getLozinka(), k.getPrezime(), 
	                       k.getIme(), k.getEmail());
	}

  /**
   * Pretvara POJO objekt u JPA entitet
   * 
   * @param k korisnik POJO objekt
   * @return korisnik entitet
   */
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

  /**
   * Pretvara listu JPA entiteta u listu POJO objekata
   * 
   * @param korisniciE lista korisnik entiteta
   * @return lista korisnik POJO objekata
   */
  public List<Korisnik> pretvori(List<Korisnici> korisniciE) {
    List<Korisnik> korisnici = new ArrayList<>();
    for (Korisnici kEntitet : korisniciE) {
      var kObjekt = pretvori(kEntitet);
      korisnici.add(kObjekt);
    }
    return korisnici;
  }
}