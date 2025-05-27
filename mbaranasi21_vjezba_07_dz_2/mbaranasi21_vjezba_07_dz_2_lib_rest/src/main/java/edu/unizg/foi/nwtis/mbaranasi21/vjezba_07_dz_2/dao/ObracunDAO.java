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

import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * DAO klasa za rad s obračunima u bazi podataka.
 */
public class ObracunDAO {
  private Connection vezaBP;

  public ObracunDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  /**
   * Dohvaća sve obračune.
   */
  public List<Obracun> dohvatiSve() {
	  String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni ORDER BY vrijeme";

	  List<Obracun> obracuni = new ArrayList<>();

	  try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

	    while (rs.next()) {
	      int partner = rs.getInt("partner");
	      String id = rs.getString("id");
	      boolean jelo = rs.getBoolean("jelo");
	      float kolicina = rs.getFloat("kolicina");
	      float cijena = rs.getFloat("cijena");
	      
	      java.sql.Timestamp timestamp = rs.getTimestamp("vrijeme");
	      long vrijeme = timestamp.getTime();

	      Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
	      obracuni.add(o);
	    }
	    return obracuni;

	  } catch (SQLException ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return null;
	}

  /**
   * Dohvaća obračune s vremenskim filterom.
   */
  public List<Obracun> dohvatiSVremenskomFilterom(Long vrijemeOd, Long vrijemeDo) {
	  StringBuilder upit = new StringBuilder("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE 1=1");
	  
	  if (vrijemeOd != null) {
	    upit.append(" AND vrijeme >= ?");
	  }
	  if (vrijemeDo != null) {
	    upit.append(" AND vrijeme <= ?");
	  }
	  upit.append(" ORDER BY vrijeme");

	  List<Obracun> obracuni = new ArrayList<>();

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit.toString())) {
	    
	    int paramIndex = 1;
	    if (vrijemeOd != null) {
	      s.setTimestamp(paramIndex++, new java.sql.Timestamp(vrijemeOd));
	    }
	    if (vrijemeDo != null) {
	      s.setTimestamp(paramIndex, new java.sql.Timestamp(vrijemeDo));
	    }

	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	      int partner = rs.getInt("partner");
	      String id = rs.getString("id");
	      boolean jelo = rs.getBoolean("jelo");
	      float kolicina = rs.getFloat("kolicina");
	      float cijena = rs.getFloat("cijena");
	      
	      java.sql.Timestamp timestamp = rs.getTimestamp("vrijeme");
	      long vrijeme = timestamp.getTime();

	      Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
	      obracuni.add(o);
	    }
	    return obracuni;

	  } catch (SQLException ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return null;
	}

  /**
   * Dohvaća obračune samo za jela s vremenskim filterom.
   */
  public List<Obracun> dohvatiJelaSVremenskomFilterom(Long vrijemeOd, Long vrijemeDo) {
	  StringBuilder upit = new StringBuilder("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = true");
	  
	  if (vrijemeOd != null) {
	    upit.append(" AND vrijeme >= ?");
	  }
	  if (vrijemeDo != null) {
	    upit.append(" AND vrijeme <= ?");
	  }
	  upit.append(" ORDER BY vrijeme");

	  List<Obracun> obracuni = new ArrayList<>();

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit.toString())) {
	    
	    int paramIndex = 1;
	    if (vrijemeOd != null) {
	      s.setTimestamp(paramIndex++, new java.sql.Timestamp(vrijemeOd));
	    }
	    if (vrijemeDo != null) {
	      s.setTimestamp(paramIndex, new java.sql.Timestamp(vrijemeDo));
	    }

	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	      int partner = rs.getInt("partner");
	      String id = rs.getString("id");
	      boolean jelo = rs.getBoolean("jelo");
	      float kolicina = rs.getFloat("kolicina");
	      float cijena = rs.getFloat("cijena");
	      
	      java.sql.Timestamp timestamp = rs.getTimestamp("vrijeme");
	      long vrijeme = timestamp.getTime();

	      Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
	      obracuni.add(o);
	    }
	    return obracuni;

	  } catch (SQLException ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return null;
	}

  /**
   * Dohvaća obračune samo za piće s vremenskim filterom.
   */
  public List<Obracun> dohvatiPiceSVremenskomFilterom(Long vrijemeOd, Long vrijemeDo) {
	  StringBuilder upit = new StringBuilder("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = false");
	  
	  if (vrijemeOd != null) {
	    upit.append(" AND vrijeme >= ?");
	  }
	  if (vrijemeDo != null) {
	    upit.append(" AND vrijeme <= ?");
	  }
	  upit.append(" ORDER BY vrijeme");

	  List<Obracun> obracuni = new ArrayList<>();

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit.toString())) {
	    
	    int paramIndex = 1;
	    if (vrijemeOd != null) {
	      s.setTimestamp(paramIndex++, new java.sql.Timestamp(vrijemeOd));
	    }
	    if (vrijemeDo != null) {
	      s.setTimestamp(paramIndex, new java.sql.Timestamp(vrijemeDo));
	    }

	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	      int partner = rs.getInt("partner");
	      String id = rs.getString("id");
	      boolean jelo = rs.getBoolean("jelo");
	      float kolicina = rs.getFloat("kolicina");
	      float cijena = rs.getFloat("cijena");
	      
	      java.sql.Timestamp timestamp = rs.getTimestamp("vrijeme");
	      long vrijeme = timestamp.getTime();

	      Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
	      obracuni.add(o);
	    }
	    return obracuni;

	  } catch (SQLException ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return null;
	}

  /**
   * Dohvaća obračune za određenog partnera s vremenskim filterom.
   */
  public List<Obracun> dohvatiZaPartneraSVremenskomFilterom(int partnerId, Long vrijemeOd, Long vrijemeDo) {
	  StringBuilder upit = new StringBuilder("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE partner = ?");
	  
	  if (vrijemeOd != null) {
	    upit.append(" AND vrijeme >= ?");
	  }
	  if (vrijemeDo != null) {
	    upit.append(" AND vrijeme <= ?");
	  }
	  upit.append(" ORDER BY vrijeme");

	  List<Obracun> obracuni = new ArrayList<>();

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit.toString())) {
	    
	    s.setInt(1, partnerId);
	    
	    int paramIndex = 2;
	    if (vrijemeOd != null) {
	      s.setTimestamp(paramIndex++, new java.sql.Timestamp(vrijemeOd));
	    }
	    if (vrijemeDo != null) {
	      s.setTimestamp(paramIndex, new java.sql.Timestamp(vrijemeDo));
	    }

	    ResultSet rs = s.executeQuery();

	    while (rs.next()) {
	      int partner = rs.getInt("partner");
	      String id = rs.getString("id");
	      boolean jelo = rs.getBoolean("jelo");
	      float kolicina = rs.getFloat("kolicina");
	      float cijena = rs.getFloat("cijena");
	      
	      java.sql.Timestamp timestamp = rs.getTimestamp("vrijeme");
	      long vrijeme = timestamp.getTime();

	      Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
	      obracuni.add(o);
	    }
	    return obracuni;

	  } catch (SQLException ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return null;
	}

  /**
   * Dodaje obračun u bazu podataka.
   */
  public boolean dodaj(Obracun obracun) {
	  String upit = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) VALUES (?, ?, ?, ?, ?, ?)";

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

	    s.setInt(1, obracun.partner());
	    s.setString(2, obracun.id());
	    s.setBoolean(3, obracun.jelo());
	    s.setFloat(4, obracun.kolicina());
	    s.setFloat(5, obracun.cijena());
	    
	    s.setTimestamp(6, new java.sql.Timestamp(obracun.vrijeme()));

	    int brojAzuriranja = s.executeUpdate();

	    return brojAzuriranja == 1;

	  } catch (Exception ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return false;
	}

  /**
   * Dodaje više obračuna u bazu podataka.
   */
  public boolean dodajVise(List<Obracun> obracuni) {
	  String upit = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) VALUES (?, ?, ?, ?, ?, ?)";

	  try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
	    
	    for (Obracun obracun : obracuni) {
	      s.setInt(1, obracun.partner());
	      s.setString(2, obracun.id());
	      s.setBoolean(3, obracun.jelo());
	      s.setFloat(4, obracun.kolicina());
	      s.setFloat(5, obracun.cijena());
	      
	      s.setTimestamp(6, new java.sql.Timestamp(obracun.vrijeme()));
	      s.addBatch();
	    }

	    int[] rezultati = s.executeBatch();
	    
	    for (int rezultat : rezultati) {
	      if (rezultat == PreparedStatement.EXECUTE_FAILED) {
	        return false;
	      }
	    }
	    
	    return true;

	  } catch (Exception ex) {
	    Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
	  }
	  return false;
	}
}