package edu.unizg.foi.nwtis.konfiguracije;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Klasa za konfiguraciju s xml zapisom podataka (Properties) u datoteku.
 */
public final class KonfiguracijaXml extends KonfiguracijaApstraktna {

  /** Konstanta za tip konfiguracije. */
  public static final String TIP = "xml";

  /**
   * Instancira novi objekt klase.
   *
   * @param nazivDatoteke the naziv datoteke
   */
  public KonfiguracijaXml(String nazivDatoteke) {
    super(nazivDatoteke);
  }

  /**
   * Spremi konfiguraciju.
   *
   * @param datotekaNaziv naziv datoteke
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void spremiKonfiguraciju(String datotekaNaziv) throws NeispravnaKonfiguracija {
    var datoteka = Path.of(datotekaNaziv);
    var tip = Konfiguracija.dajTipKonfiguracije(datotekaNaziv);
    if (tip == null || tip.compareTo(KonfiguracijaXml.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nema tip: " + KonfiguracijaXml.TIP);
    } else if (Files.exists(datoteka)
        && (!Files.isRegularFile(datoteka) || !Files.isWritable(datoteka))) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " ne postoji/nije datoteka/ne može se u nju pisati");
    }
    try {
      this.postavke.storeToXML(Files.newOutputStream(datoteka), KonfiguracijaApstraktna.verzija);
    } catch (IOException ex) {
      throw new NeispravnaKonfiguracija(
          "Problem kod spremanja u datoteku: '" + nazivDatoteke + "'.");
    }
  }

  /**
   * Učitaj konfiguraciju.
   *
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void ucitajKonfiguraciju() throws NeispravnaKonfiguracija {
    var datoteka = Path.of(this.nazivDatoteke);
    var tip = Konfiguracija.dajTipKonfiguracije(this.nazivDatoteke);
    if (tip == null || tip.compareTo(KonfiguracijaXml.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nema tip: " + KonfiguracijaXml.TIP);
    } else if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
        || !Files.isReadable(datoteka)) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nije ispravnog tipa/ne postoji/ne može se učitati");
    }
    try {
      this.postavke.loadFromXML(Files.newInputStream(datoteka));
    } catch (IOException ex) {
      throw new NeispravnaKonfiguracija(
          "Problem kod učitavanja datoteke: '" + this.nazivDatoteke + "'.");
    }
  }
}
