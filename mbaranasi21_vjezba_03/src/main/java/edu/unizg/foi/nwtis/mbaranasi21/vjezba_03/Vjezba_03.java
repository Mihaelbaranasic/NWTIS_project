package edu.unizg.foi.nwtis.mbaranasi21.vjezba_03;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class Vjezba_03 {

  private Konfiguracija konfig;
  private AtomicInteger brojSlobodnihDretvi = new AtomicInteger(0);
  private AtomicInteger UkupniBrojDretvi = new AtomicInteger(0);
  private int maxDubinaDirektorija = 0;
  private String trazenaRijec = "";
  private String nazivPocetnogDirektorija = ".";

  public static void main(String[] args) {
    if (args.length < 2 || args.length > 4) {
      System.out.println("Broj argumenata nije u raspornu 1 - 4.");
      return;
    }
    var program = new Vjezba_03();
    if (!program.ucitajKonfiguraciju(args[0])) {
      return;
    }
    System.out.println(program.konfig.dajPostavku("maksDubina"));
    program.maxDubinaDirektorija = Integer.parseInt(program.konfig.dajPostavku("maxDubina"));
  }

  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  public SkupPodatakaPretrazivanja pretraziPutanju(Path nazivPutanje, int a) {
    this.brojSlobodnihDretvi.decrementAndGet();
    this.UkupniBrojDretvi.incrementAndGet();
    var skupPodataka = new SkupPodatakaPretrazivanja();
    System.out.println(nazivPutanje);
    try {
      var tipPodatka = Files.list(nazivPutanje);
    } catch (IOException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return skupPodataka;
  }
}
