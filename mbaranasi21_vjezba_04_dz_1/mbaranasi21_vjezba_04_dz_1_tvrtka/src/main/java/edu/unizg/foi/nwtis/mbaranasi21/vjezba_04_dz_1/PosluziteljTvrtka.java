package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class PosluziteljTvrtka {

  /** Konfiguracijski podaci */
  private Konfiguracija konfig;

  /** Pokretač dretvi */
  private ExecutorService executor = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Broj argumenata nije 1.");
      return;
    }

    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];

    program.pripremiKreni(nazivDatoteke);
  }

  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    var dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());

    while (!dretvaZaKraj.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
      }
    }
  }

  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
    var brojCekaca = 0;
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        var mreznaUticnica = ss.accept();
        this.obradiKraj(mreznaUticnica);
      }
      ss.close();

    } catch (IOException e) {
    }
  }

  public Boolean obradiKraj(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();
      if (linija.trim().equals("KRAJ " + this.kodZaKraj)) {
        out.write("OK\n");
        this.kraj.set(true);
      } else {
        out.write("ERROR 10\n");
      }

      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {

    }
    return Boolean.TRUE;
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
}
