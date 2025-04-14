package edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik.podaci.DnevnikRada;

public class BazaPodatakaDnevnik implements ServisDnevnikRada {
  private String korisnickoImeBazaPodataka;
  private String lozinkaBazaPodataka;
  private String urlBazaPodataka;
  private String upravljacBazaPodataka;
  private Connection vezaBazaPodataka;
  private PreparedStatement naredbaDodavanje;
  private PreparedStatement naredbaPretrazivanje;

  private String sqlDodavanje = "INSERT INTO dnevnik_Rada "
      + "(vrijeme, korisnickoIme, adresaRacunala, ipAdresaRacunala, nazivOS, verzijaVM, opisRada) "
      + "VALUES (?, ?, ?, ?, ?, ?, ?)";

  private String sqlPretrazivanje =
      "SELECT vrijeme, korisnickoIme, adresaRacunala, ipAdresaRacunala, nazivOS, verzijaVM, opisRada FROM dnevnik_Rada "
          + "WHERE korisnickoIme = ? AND vrijeme >= ? AND vrijeme <= ?";
  
  public BazaPodatakaDnevnik() {}

  public BazaPodatakaDnevnik(String korisnickoImeBazaPodataka, String lozinkaBazaPodataka,
      String urlBazaPodataka, String upravljacBazaPodataka) {
    super();
    this.korisnickoImeBazaPodataka = korisnickoImeBazaPodataka;
    this.lozinkaBazaPodataka = lozinkaBazaPodataka;
    this.urlBazaPodataka = urlBazaPodataka;
    this.upravljacBazaPodataka = upravljacBazaPodataka;
  }

  @Override
  public boolean pripremiResurs() throws Exception {

    Class.forName(this.upravljacBazaPodataka);

    this.vezaBazaPodataka = DriverManager.getConnection(this.urlBazaPodataka,
        this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka);

    this.naredbaDodavanje = this.vezaBazaPodataka.prepareStatement(this.sqlDodavanje);
    this.naredbaPretrazivanje = this.vezaBazaPodataka.prepareStatement(this.sqlPretrazivanje);

    return true;
  }

  @Override
  public boolean otpustiResurs() throws Exception {
    this.vezaBazaPodataka.close();
    return true;
  }

  @Override
  public boolean upisiDnevnik(DnevnikRada dnevnikRada) throws Exception {
    this.naredbaDodavanje.setTimestamp(1, new Timestamp(dnevnikRada.vrijeme()));
    this.naredbaDodavanje.setString(2, dnevnikRada.korisnickoIme());
    this.naredbaDodavanje.setString(3, dnevnikRada.adresaRacunala());
    this.naredbaDodavanje.setString(4, dnevnikRada.ipAdresaRacunala());
    this.naredbaDodavanje.setString(5, dnevnikRada.nazivOS());
    this.naredbaDodavanje.setString(6, dnevnikRada.verzijaVM());
    this.naredbaDodavanje.setString(7, dnevnikRada.opisRada());

    var brojZapisa = this.naredbaDodavanje.executeUpdate();

    if (brojZapisa != 1) {
      return false;
    }
    return true;
  }

  @Override
  public List<DnevnikRada> dohvatiDnevnik(long vrijemeOd, long vrijemeDo, String korisnickoIme)
      throws Exception {

    List<DnevnikRada> zapisiDnevnikaRada = new ArrayList<DnevnikRada>();

    this.naredbaPretrazivanje.setString(1, korisnickoIme);
    this.naredbaPretrazivanje.setTimestamp(2, new Timestamp(vrijemeOd));
    this.naredbaPretrazivanje.setTimestamp(3, new Timestamp(vrijemeDo));

    var odgovor = this.naredbaPretrazivanje.executeQuery();

    while (odgovor.next()) {
      var dnevnikRada = new DnevnikRada(odgovor.getTimestamp(1).getTime(), odgovor.getString(2),
          odgovor.getString(3), odgovor.getString(4), odgovor.getString(5), odgovor.getString(6),
          odgovor.getString(7));

      zapisiDnevnikaRada.add(dnevnikRada);
    }

    return zapisiDnevnikaRada;
  }

  public boolean koristiDatoteku() {
    return false;
  }
  
  public boolean koristiBazuPodataka() {
    return true;
  }

  @Override
  public boolean trebaPodatkeBazaPodataka() {
    // TODO Auto-generated method stub
    return false;
  }
}
