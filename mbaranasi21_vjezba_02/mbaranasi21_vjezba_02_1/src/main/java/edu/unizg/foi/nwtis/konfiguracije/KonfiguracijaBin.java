package edu.unizg.foi.nwtis.konfiguracije;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Klasa za konfiguraciju s binarnin zapisom podataka (serijalizacija) u datoteku.
 */
public final class KonfiguracijaBin extends KonfiguracijaApstraktna {

  /** Konstanta za tip konfiguracije. */
  public static final String TIP = "bin";

  /**
   * Instancira novi objekt klase.
   *
   * @param nazivDatoteke naziv datoteke
   */
  public KonfiguracijaBin(String nazivDatoteke) {
    super(nazivDatoteke);
  }

  /**
   * Spremi konfiguraciju.
   *
   * @param datotekaNaziv naziv datoteka
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void spremiKonfiguraciju(String datotekaNaziv) throws NeispravnaKonfiguracija {
    var datoteka = Path.of(datotekaNaziv);
    var tip = Konfiguracija.dajTipKonfiguracije(datotekaNaziv);
    if (tip == null || tip.compareTo(KonfiguracijaBin.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nema tip: " + KonfiguracijaBin.TIP);
    } else if (Files.exists(datoteka)
        && (!Files.isRegularFile(datoteka) || !Files.isWritable(datoteka))) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nije datoteka/ne može se u nju pisati");
    }
    try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(datoteka))) {
      oos.writeObject(this.postavke);
    } catch (IOException e) {
      throw new NeispravnaKonfiguracija(
          "Problem kod spremanja u datoteku: '" + nazivDatoteke + "'.");
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @throws NeispravnaKonfiguracija iznimka kada je neispravna konfiguracija
   */
  @Override
  public void ucitajKonfiguraciju() throws NeispravnaKonfiguracija {
    var datoteka = Path.of(this.nazivDatoteke);
    var tip = Konfiguracija.dajTipKonfiguracije(this.nazivDatoteke);
    if (tip == null || tip.compareTo(KonfiguracijaBin.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nema tip: " + KonfiguracijaBin.TIP);
    } else if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
        || !Files.isReadable(datoteka)) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nije ispravnog tipa/ne postoji/ne može se učitati");
    }
    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(datoteka))) {
      Object o = ois.readObject();
      if (o instanceof Properties) {
        this.postavke = (Properties) o;
      } else {
        throw new NeispravnaKonfiguracija(
            "Problem kod učitavanja datoteke: '" + this.nazivDatoteke + "'.");
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new NeispravnaKonfiguracija(e.getMessage());
    }
  }

}
