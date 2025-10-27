package edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.podaci.Korisnik;


/**
 * DAO klasa za rad s korisnicima u bazi podataka.
 * 
 * @author Dragutin Kermek
 */
public class KorisnikDAO {
	/** Veza s bazom podataka. */
  private Connection vezaBP;
  /**
   * Konstruktor koji prima vezu s bazom podataka.
   * 
   * @param vezaBP veza s bazom podataka
   */
  public KorisnikDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  /**
   * Dohvaća korisnika iz baze podataka prema korisničkom imenu i lozinki.
   * 
   * @param korisnik korisničko ime
   * @param lozinka lozinka korisnika
   * @param prijava true ako se provjerava lozinka, false inače
   * @return objekt korisnika ili null ako ne postoji
   */
  public Korisnik dohvati(String korisnik, String lozinka, Boolean prijava) {
    String upit = "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ?";

    if (prijava) {
      upit += " and lozinka = ?";
    }

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      if (prijava) {
        s.setString(2, lozinka);
      }
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");

        Korisnik k = new Korisnik(korisnik, "******", prezime, ime, email);
        return k;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvaća sve korisnike iz baze podataka.
   * 
   * @return lista svih korisnika ili null u slučaju greške
   */
  public List<Korisnik> dohvatiSve() {
    String upit = "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici";

    List<Korisnik> korisnici = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvaća korisnike prema prezimenu i imenu koristeći LIKE pretraživanje.
   * 
   * @param pPrezime uzorak za pretraživanje prezimena
   * @param pIme uzorak za pretraživanje imena
   * @return lista korisnika koji odgovaraju uzorku ili null u slučaju greške
   */
  public List<Korisnik> dohvatiPrezimeIme(String pPrezime, String pIme) {
    String upit =
        "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici WHERE prezime LIKE ? AND ime LIKE ?";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);) {

      s.setString(1, pPrezime);
      s.setString(2, pIme);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      rs.close();
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Ažurira podatke korisnika u bazi podataka.
   * 
   * @param k objekt korisnika s novim podacima
   * @param lozinka nova lozinka korisnika
   * @return true ako je ažuriranje uspješno, inače false
   */
  public boolean azuriraj(Korisnik k, String lozinka) {
    String upit = "UPDATE korisnici SET ime = ?, prezime = ?, email = ?, lozinka = ? "
        + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, lozinka);
      s.setString(5, k.korisnik());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  /**
   * Dodaje novog korisnika u bazu podataka.
   * 
   * @param k objekt korisnika za dodavanje
   * @return true ako je dodavanje uspješno, inače false
   */
  public boolean dodaj(Korisnik k) {
    String upit = "INSERT INTO korisnici (ime, prezime, email, korisnik, lozinka) "
        + "VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, k.korisnik());
      s.setString(5, k.lozinka());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
