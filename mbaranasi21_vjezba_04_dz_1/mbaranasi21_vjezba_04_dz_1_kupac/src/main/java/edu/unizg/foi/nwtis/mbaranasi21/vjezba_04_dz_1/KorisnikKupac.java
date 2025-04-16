package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klijentska aplikacija za kupca koja šalje komande poslužitelju partnera.
 */
public class KorisnikKupac {
  /** Konfiguracijski podaci */
  private Konfiguracija konfig;

  /**
   * Glavna metoda za pokretanje klijenta kupca.
   * 
   * @param args argumenti naredbenog retka
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println(
          "Broj argumenata nije 2. Očekivano: datoteka_konfiguracije.txt datoteka_podataka.csv");
      return;
    }

    String nazivDatoteke = args[0];
    String datotekaPodataka = args[1];

    KorisnikKupac program = new KorisnikKupac();
    program.pokreni(nazivDatoteke, datotekaPodataka);
  }

  /**
   * Pokreće klijenta kupca s učitanom konfiguracijom i podacima.
   * 
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @param datotekaPodataka naziv datoteke s komandama
   */
  private void pokreni(String nazivDatoteke, String datotekaPodataka) {
    if (!ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    obradiDatotekuPodataka(datotekaPodataka);
  }

  /**
   * Obrađuje datoteku s podacima i šalje komande.
   * 
   * @param datotekaPodataka naziv datoteke s podacima
   */
  private void obradiDatotekuPodataka(String datotekaPodataka) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(datotekaPodataka));

      String linija;
      while ((linija = reader.readLine()) != null) {
        obradiLiniju(linija);
      }

      reader.close();

    } catch (IOException e) {
    }
  }

  /**
   * Obrađuje liniju iz datoteke podataka.
   * 
   * @param linija linija za obradu
   */
  private void obradiLiniju(String linija) {
    try {
      String[] dijelovi = linija.split(";");
      if (dijelovi.length != 5) {
        return;
      }

      String korisnik = dijelovi[0];
      String adresa = dijelovi[1];
      int mreznaVrata = Integer.parseInt(dijelovi[2]);
      int spavanje = Integer.parseInt(dijelovi[3]);
      String komanda = dijelovi[4];

      Thread.sleep(spavanje);

      posaljiKomandu(adresa, mreznaVrata, komanda);

    } catch (Exception e) {
    }
  }

  /**
   * Učitava konfiguraciju iz datoteke.
   * 
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @return true ako je učitavanje uspjelo, inače false
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }

  /**
   * Šalje komandu na određenu adresu i port.
   * 
   * @param adresa adresa poslužitelja
   * @param mreznaVrata port poslužitelja
   * @param komanda komanda za slanje
   */
  private void posaljiKomandu(String adresa, int mreznaVrata, String komanda) {
    try {
      Socket socket = new Socket(adresa, mreznaVrata);

      BufferedReader in =
          new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

      out.write(komanda + "\n");
      out.flush();

      procitajOdgovor(in, komanda);

      socket.close();

    } catch (IOException e) {
    }
  }

  /**
   * Čita odgovor na poslanu komandu.
   * 
   * @param in ulazni tok
   * @param komanda poslana komanda
   * @throws IOException ako dođe do greške pri čitanju
   */
  private void procitajOdgovor(BufferedReader in, String komanda) throws IOException {
    String odgovor = in.readLine();

    if (odgovor != null && odgovor.equals("OK")
        && (komanda.startsWith("JELOVNIK") || komanda.startsWith("KARTAPIĆA")
            || komanda.startsWith("RAČUN") || komanda.startsWith("POPIS"))) {

      StringBuilder jsonBuilder = new StringBuilder();
      String red;
      while ((red = in.readLine()) != null) {
        jsonBuilder.append(red);
      }

    }
  }
}
