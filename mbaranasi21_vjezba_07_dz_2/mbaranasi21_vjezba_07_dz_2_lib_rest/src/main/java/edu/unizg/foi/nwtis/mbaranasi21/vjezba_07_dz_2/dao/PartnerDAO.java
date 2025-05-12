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
import edu.unizg.foi.nwtis.podaci.Partner;


/**
 *
 * @author Dragutin Kermek
 */
public class PartnerDAO {
  private Connection vezaBP;

  public PartnerDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  public Partner dohvati(int id, boolean sakriKodove) {
    String upit =
        "SELECT naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod FROM partneri WHERE id = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, id);

      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String naziv = rs.getString("naziv");
        String vrstaKuhinje = rs.getString("vrstaKuhinje");
        String adresa = rs.getString("adresa");
        int mreznaVrata = rs.getInt("mreznaVrata");
        int mreznaVrataKraj = rs.getInt("mreznaVrataKraj");    
        float gpsSirina = rs.getFloat("gpsSirina");
        float gpsDuzina = rs.getFloat("gpsDuzina");
        String sigurnosniKod = rs.getString("sigurnosniKod");
        String adminKod = rs.getString("adminKod");

        Partner p = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina,
            sigurnosniKod, adminKod);
        if(sakriKodove) {
          p = p.partnerBezKodova();
        }
        return p;
      }

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public List<Partner> dohvatiSve(boolean sakriKodove) {
    String upit =
        "SELECT id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod FROM partneri ORDER BY id";

    List<Partner> partneri = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int id = rs.getInt("id");
        String naziv = rs.getString("naziv");
        String vrstaKuhinje = rs.getString("vrstaKuhinje");
        String adresa = rs.getString("adresa");
        int mreznaVrata = rs.getInt("mreznaVrata");
        int mreznaVrataKraj = rs.getInt("mreznaVrataKraj");
        float gpsSirina = rs.getFloat("gpsSirina");
        float gpsDuzina = rs.getFloat("gpsDuzina");
        String sigurnosniKod = rs.getString("sigurnosniKod");
        String adminKod = rs.getString("adminKod");

        Partner p = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina,
            sigurnosniKod, adminKod);
        if(sakriKodove) {
          p = p.partnerBezKodova();
        }
        partneri.add(p);
      }
      return partneri;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  public boolean azuriraj(Partner p) {
    String upit = "UPDATE partneri SET naziv = ?, adresa = ?, mreznaVrata = ?, mreznaVrataKraj = ?, gpsSirina = ?, gpsDuzina = ?, sigurnosniKod = ?, adminKod = ? "
        + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, p.naziv());
      s.setString(2, p.adresa());
      s.setInt(3, p.mreznaVrata());
      s.setInt(4, p.mreznaVrataKraj());
      s.setFloat(5, p.gpsSirina());
      s.setFloat(6, p.gpsDuzina());
      s.setString(7, p.sigurnosniKod());
      s.setString(8, p.adminKod());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public boolean dodaj(Partner p) {
    String upit = "INSERT INTO partneri (id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, p.id());
      s.setString(2, p.naziv());
      s.setString(3, p.vrstaKuhinje());
      s.setString(4, p.adresa());
      s.setInt(5, p.mreznaVrata());
      s.setInt(6, p.mreznaVrataKraj());
      s.setFloat(7, p.gpsSirina());
      s.setFloat(8, p.gpsDuzina());
      s.setString(9, p.sigurnosniKod());
      s.setString(10, p.adminKod());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
