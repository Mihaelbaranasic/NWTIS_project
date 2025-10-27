package edu.unizg.foi.nwtis.konfiguracije;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import com.google.gson.Gson;

/**
 * Klasa za konfiguraciju s json zapisom podataka (Gson) u datoteku.
 */
public final class KonfiguracijaJson extends KonfiguracijaApstraktna {

  /** Konstanta za tip konfiguracije. */
  public static final String TIP = "json";

  /**
   * Instancira novi objekt klase.
   *
   * @param nazivDatoteke the naziv datoteke
   */
  public KonfiguracijaJson(String nazivDatoteke) {
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
    if (tip == null || tip.compareTo(KonfiguracijaJson.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nema tip: " + KonfiguracijaJson.TIP);
    } else if (Files.exists(datoteka)
        && (!Files.isRegularFile(datoteka) || !Files.isWritable(datoteka))) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + datotekaNaziv + " nije datoteka/ne može se u nju pisati");
    }
    try (var bw = Files.newBufferedWriter(datoteka)) {
      Gson gson = new Gson();
      gson.toJson(this.postavke, bw);
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
    if (tip == null || tip.compareTo(KonfiguracijaJson.TIP) != 0) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nema tip: " + KonfiguracijaJson.TIP);
    } else if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka)
        || !Files.isReadable(datoteka)) {
      throw new NeispravnaKonfiguracija(
          "Datoteka: " + this.nazivDatoteke + " nije ispravnog tipa/ne postoji/ne može se učitati");
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      this.postavke = gson.fromJson(br, Properties.class);
    } catch (IOException ex) {
      throw new NeispravnaKonfiguracija(
          "Problem kod učitavanja datoteke: '" + this.nazivDatoteke + "'.");
    }
  }

}
