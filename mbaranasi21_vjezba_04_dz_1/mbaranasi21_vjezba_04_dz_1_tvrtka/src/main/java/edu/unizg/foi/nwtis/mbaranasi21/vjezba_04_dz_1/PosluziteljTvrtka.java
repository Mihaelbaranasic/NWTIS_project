package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Jelovnik;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.KartaPica;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Obracun;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Partner;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.PartnerPopis;

/**
 * Poslužitelj tvrtke koji upravlja partnerima, jelovnicima i kartom pića.
 */
public class PosluziteljTvrtka {

  /** Konfiguracijski podaci. */
  protected Konfiguracija konfig;

  /** Pokretač dretvi. */
  private ExecutorService executor = null;
  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada. */
  private String kodZaKraj = "";

  /** Zastavica za kraj rada. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Lista partnera. */
  private List<Partner> partneri = new ArrayList<>();

  /** Kolekcija jelovnika. */
  private List<Jelovnik> jelovnici = new ArrayList<>();

  /** Kolekcija karte pića. */
  private List<KartaPica> kartaPica = new ArrayList<>();

  /** Kolekcija obračuna. */
  private List<Obracun> obracuni = new ArrayList<>();

  /** Gson objekt za rad s JSON-om. */
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /** Broj prekinutih dretvi. */
  private AtomicInteger brojPrekinutihDretvi = new AtomicInteger(0);

  /** Broj zatvorenih veza. */
  private AtomicInteger brojZatvorenihVeza = new AtomicInteger(0);

  /** Aktivne dretve. */
  private List<Thread> aktivneDretve = Collections.synchronizedList(new ArrayList<>());

  /**
   * Glavna metoda za pokretanje poslužitelja tvrtke.
   * 
   * @param args argumenti naredbenog retka
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Broj argumenata nije 1.");
      return;
    }
    var program = new PosluziteljTvrtka();
    var nazivDatoteke = args[0];
    program.pripremiKreni(nazivDatoteke);
  }

  /**
   * Priprema i pokreće poslužitelj.
   * 
   * @param nazivDatoteke naziv konfiguracijske datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    if (!provjeriObaveznePostavke()) {
      return;
    }

    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    ucitajPodatke();

    pokreniPosluzitelje();
  }

  /**
   * Provjerava postoje li sve obavezne postavke u konfiguraciji.
   * 
   * @return true ako postoje sve obavezne postavke, inače false
   */
  private boolean provjeriObaveznePostavke() {
    String[] obaveznePostavke = {"datotekaPartnera", "mreznaVrataKraj", "mreznaVrataRegistracija",
        "mreznaVrataRad", "kodZaKraj", "datotekaKartaPica", "datotekaObracuna"};

    for (String postavka : obaveznePostavke) {
      if (!this.konfig.postojiPostavka(postavka)) {
        System.out.println("Nedostaje obavezna postavka: " + postavka);
        return false;
      }
    }

    if (!this.konfig.dajPostavku("datotekaPartnera").endsWith(".json")) {
      return false;
    }

    return true;
  }

  /**
   * Pokreće poslužitelje za kraj, registraciju i rad.
   */
  private void pokreniPosluzitelje() {
    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    Future<?> dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
    Future<?> dretvaZaRegistraciju =
        this.executor.submit(() -> this.pokreniPosluziteljRegistracija());
    Future<?> dretvaZaRad = this.executor.submit(() -> this.pokreniPosluziteljRad());

    cekajNaKraj(dretvaZaRegistraciju, dretvaZaRad);
  }

  /**
   * Čeka dok ne dođe zahtjev za kraj, a zatim prekida dretve.
   * 
   * @param dretvaZaRegistraciju dretva za registraciju
   * @param dretvaZaRad dretva za rad
   */
  private void cekajNaKraj(Future<?> dretvaZaRegistraciju, Future<?> dretvaZaRad) {
    while (!this.kraj.get()) {
      try {
        Thread.sleep(this.pauzaDretve);

        if (this.kraj.get()) {
          prekiniDretve(dretvaZaRegistraciju, dretvaZaRad);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Prekida dretve za registraciju i rad.
   * 
   * @param dretvaZaRegistraciju dretva za registraciju
   * @param dretvaZaRad dretva za rad
   */
  private void prekiniDretve(Future<?> dretvaZaRegistraciju, Future<?> dretvaZaRad) {
    if (!dretvaZaRegistraciju.isDone()) {
      dretvaZaRegistraciju.cancel(true);
    }
    if (!dretvaZaRad.isDone()) {
      dretvaZaRad.cancel(true);
    }
  }

  /**
   * Učitava podatke iz datoteka: partnere, jelovnike i kartu pića.
   */
  private void ucitajPodatke() {
    try {
      ucitajPartnere();
      ucitajJelovnike();
      ucitajKartuPica();
      filtrirajPartnere();
    } catch (IOException e) {
    }
  }

  /**
   * Učitava podatke o partnerima iz datoteke.
   * 
   * @throws IOException ako dođe do greške pri čitanju datoteke
   */
  private void ucitajPartnere() throws IOException {
    String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
    BufferedReader reader = new BufferedReader(new FileReader(datotekaPartnera));
    this.partneri = gson.fromJson(reader, new TypeToken<List<Partner>>() {}.getType());
    reader.close();
  }

  /**
   * Učitava jelovnike iz datoteka.
   * 
   * @throws IOException ako dođe do greške pri čitanju datoteke
   */
  private void ucitajJelovnike() throws IOException {
    for (int i = 1; i <= 9; i++) {
      String kljucKuhinje = "kuhinja_" + i;
      if (this.konfig.postojiPostavka(kljucKuhinje)) {
        String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
        String[] dijelovi = vrijednostKuhinje.split(";");
        if (dijelovi.length >= 1) {
          String datotekaJelovnika = kljucKuhinje + ".json";

          try {
            BufferedReader reader = new BufferedReader(new FileReader(datotekaJelovnika));
            List<Jelovnik> jelovniciKuhinje =
                gson.fromJson(reader, new TypeToken<List<Jelovnik>>() {}.getType());
            this.jelovnici.addAll(jelovniciKuhinje);
            reader.close();
          } catch (IOException e) {
          }
        }
      }
    }
  }

  /**
   * Učitava kartu pića iz datoteke.
   * 
   * @throws IOException ako dođe do greške pri čitanju datoteke
   */
  private void ucitajKartuPica() throws IOException {
    String datotekaKartaPica = this.konfig.dajPostavku("datotekaKartaPica");
    BufferedReader reader = new BufferedReader(new FileReader(datotekaKartaPica));
    this.kartaPica = gson.fromJson(reader, new TypeToken<List<KartaPica>>() {}.getType());
    reader.close();
  }

  /**
   * Filtrira partnere prema postojećim kuhinjama.
   */
  private void filtrirajPartnere() {
    List<Partner> validniPartneri = new ArrayList<>();
    Set<String> postojeceKuhinje = dohvatiPostojeceKuhinje();

    for (Partner p : partneri) {
      if (postojeceKuhinje.contains(p.vrstaKuhinje())) {
        validniPartneri.add(p);
      }
    }

    partneri = validniPartneri;
  }

  /**
   * Dohvaća set postojećih kuhinja.
   * 
   * @return set oznaka postojećih kuhinja
   */
  private Set<String> dohvatiPostojeceKuhinje() {
    Set<String> postojeceKuhinje = new HashSet<>();

    for (int i = 1; i <= 9; i++) {
      String kljucKuhinje = "kuhinja_" + i;
      if (this.konfig.postojiPostavka(kljucKuhinje)) {
        String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
        String[] dijelovi = vrijednostKuhinje.split(";");
        if (dijelovi.length >= 1) {
          postojeceKuhinje.add(dijelovi[0]);
        }
      }
    }

    return postojeceKuhinje;
  }

  /**
   * Pokreće poslužitelj za dohvat naredbe za kraj.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
    var brojCekaca = 0;
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          this.executor.submit(() -> {
            aktivneDretve.add(Thread.currentThread());
            try {
              return this.obradiKraj(mreznaUticnica);
            } finally {
              aktivneDretve.remove(Thread.currentThread());
            }
          });
        } catch (IOException e) {
          if (!this.kraj.get()) {
          }
        }
      }
    } catch (IOException e) {
    }
  }

  /**
   * Obrađuje zahtjev za kraj.
   * 
   * @param mreznaUticnica mrežna utičnica za komunikaciju
   * @return Boolean.TRUE ako je obrada uspješna, inače Boolean.FALSE
   */
  public Boolean obradiKraj(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      if (!provjeriKomandaKraj(linija, out)) {
        zatvoriVezu(mreznaUticnica);
        return Boolean.FALSE;
      }

      if (!provjeriAdresuZahtjeva(mreznaUticnica, out)) {
        zatvoriVezu(mreznaUticnica);
        return Boolean.FALSE;
      }

      out.write("OK\n");
      out.flush();
      this.kraj.set(true);

      zatvoriVezu(mreznaUticnica);
    } catch (Exception e) {
      zatvoriVezu(mreznaUticnica);
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  /**
   * Provjerava je li komanda za kraj ispravna.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   * @return true ako je komanda ispravna, inače false
   */
  private boolean provjeriKomandaKraj(String linija, PrintWriter out) {
    if (linija == null) {
      out.write("ERROR 19 - Prazna datoteka\n");
      out.flush();
      return false;
    }

    if (!linija.startsWith("KRAJ ")) {
      out.write("ERROR 10 - Format komande nije ispravan (nedostaje razmak nakon KRAJ)\n");
      out.flush();
      return false;
    }

    String[] dijelovi = linija.trim().split(" ");
    if (dijelovi.length != 2 || !dijelovi[0].equals("KRAJ")
        || !dijelovi[1].equals(this.kodZaKraj)) {
      out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
      out.flush();
      return false;
    }

    return true;
  }

  /**
   * Provjerava je li adresa zahtjeva lokalna.
   * 
   * @param mreznaUticnica mrežna utičnica za komunikaciju
   * @param out izlaz za odgovor
   * @return true ako je adresa lokalna, inače false
   */
  private boolean provjeriAdresuZahtjeva(Socket mreznaUticnica, PrintWriter out) {
    try {
      InetAddress adresaZahtjeva = mreznaUticnica.getInetAddress();
      InetAddress lokalnaAdresa = InetAddress.getLocalHost();

      if (!adresaZahtjeva.equals(lokalnaAdresa) && !adresaZahtjeva.isLoopbackAddress()) {
        out.write("ERROR 11 - Adresa računala s kojeg je poslan zahtjev nije lokalna adresa\n");
        out.flush();
        return false;
      }

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Pokreće poslužitelj za registraciju partnera.
   */
  public void pokreniPosluziteljRegistracija() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));

    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        Socket mreznaUticnica = ss.accept();
        this.executor.submit(() -> obradiRegistraciju(mreznaUticnica));
      }
    } catch (IOException e) {
    }
  }

  /**
   * Obrađuje zahtjeve za registraciju partnera.
   * 
   * @param mreznaUticnica mrežna utičnica za komunikaciju
   * @return Boolean.TRUE ako je obrada uspješna, inače Boolean.FALSE
   */
  private Boolean obradiRegistraciju(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      if (!provjeriKomandaRegistracija(linija, out)) {
        zatvoriVezu(mreznaUticnica);
        return Boolean.FALSE;
      }

      if (linija.startsWith("PARTNER ")) {
        obradiKomanduPartner(linija, out);
      } else if (linija.startsWith("OBRIŠI ")) {
        obradiKomanduObrisi(linija, out);
      } else if (linija.trim().equals("POPIS")) {
        obradiKomanduPopis(out);
      } else {
        out.write("ERROR 20 - Format komande nije ispravan\n");
        out.flush();
      }

      zatvoriVezu(mreznaUticnica);
    } catch (Exception e) {
      zatvoriVezu(mreznaUticnica);
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }

  /**
   * Provjerava je li komanda za registraciju ispravna.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   * @return true ako je komanda ispravna, inače false
   */
  private boolean provjeriKomandaRegistracija(String linija, PrintWriter out) {
    if (linija == null) {
      out.write("ERROR 29 - Prazna datoteka\n");
      out.flush();
      return false;
    }

    if (linija.trim().isEmpty()) {
      out.write("ERROR 20 - Format komande nije ispravan (prazna komanda)\n");
      out.flush();
      return false;
    }

    String komanda = linija.split(" ", 2)[0];

    if (komanda.equals("PARTNER") || komanda.equals("OBRIŠI")) {
      if (!linija.startsWith(komanda + " ")) {
        out.write(
            "ERROR 20 - Format komande nije ispravan (nedostaje razmak nakon naziva komande)\n");
        out.flush();
        return false;
      }
    }

    return true;
  }

  /**
   * Obrađuje komandu za registraciju partnera.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduPartner(String linija, PrintWriter out) {
    try {
      int[] indexes = pronadiIndekseNaziva(linija);
      if (indexes[0] == -1 || indexes[1] == -1) {
        out.write("ERROR 20 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      int pocetakNaziva = indexes[0];
      int krajNaziva = indexes[1];
      String naziv = linija.substring(pocetakNaziva + 1, krajNaziva);

      if (!provjeriParametreKomandePartner(linija, krajNaziva, pocetakNaziva, out)) {
        return;
      }

      int id = Integer.parseInt(linija.substring(0, pocetakNaziva).trim().split(" ")[1]);

      if (partnerPostoji(id)) {
        out.write("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
        out.flush();
        return;
      }

      String ostatakLinije = linija.substring(krajNaziva + 1).trim();
      String[] parametri = ostatakLinije.split(" ");

      String vrstaKuhinje = parametri[0];
      if (!kuhinjaPostoji(vrstaKuhinje)) {
        out.write("ERROR 29 - Registracija za nepostojeću kuhinju\n");
        out.flush();
        return;
      }

      kreirajIDodajPartnera(id, naziv, vrstaKuhinje, parametri, out);

    } catch (Exception e) {
      out.write("ERROR 29 - Nešto drugo nije u redu\n");
      out.flush();
    }
  }

  /**
   * Pronalazi indekse početka i kraja naziva partnera.
   * 
   * @param linija komanda s nazivom
   * @return polje s indeksom početka i kraja naziva
   */
  private int[] pronadiIndekseNaziva(String linija) {
    int pocetakNaziva = linija.indexOf("\"");
    int krajNaziva = linija.indexOf("\"", pocetakNaziva + 1);
    return new int[] {pocetakNaziva, krajNaziva};
  }

  /**
   * Provjerava parametre komande za registraciju partnera.
   * 
   * @param linija komanda
   * @param krajNaziva indeks kraja naziva
   * @param pocetakNaziva indeks početka naziva
   * @param out izlaz za odgovor
   * @return true ako su parametri ispravni, inače false
   */
  private boolean provjeriParametreKomandePartner(String linija, int krajNaziva, int pocetakNaziva,
      PrintWriter out) {
    String ostatakLinije = linija.substring(krajNaziva + 1).trim();
    String[] parametri = ostatakLinije.split(" ");

    if (parametri.length != 5) {
      out.write("ERROR 20 - Format komande nije ispravan\n");
      out.flush();
      return false;
    }

    String[] prviDio = linija.substring(0, pocetakNaziva).trim().split(" ");
    if (prviDio.length != 2) {
      out.write("ERROR 20 - Format komande nije ispravan\n");
      out.flush();
      return false;
    }

    return true;
  }

  /**
   * Provjerava postoji li partner s određenim ID-om.
   * 
   * @param id ID partnera
   * @return true ako partner postoji, inače false
   */
  private boolean partnerPostoji(int id) {
    for (Partner p : partneri) {
      if (p.id() == id) {
        return true;
      }
    }
    return false;
  }

  /**
   * Provjerava postoji li kuhinja s određenom oznakom.
   * 
   * @param vrstaKuhinje oznaka kuhinje
   * @return true ako kuhinja postoji, inače false
   */
  private boolean kuhinjaPostoji(String vrstaKuhinje) {
    for (int i = 1; i <= 9; i++) {
      String kljucKuhinje = "kuhinja_" + i;
      if (this.konfig.postojiPostavka(kljucKuhinje)) {
        String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
        String[] dijelovi = vrijednostKuhinje.split(";");
        if (dijelovi.length >= 1 && dijelovi[0].equals(vrstaKuhinje)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Kreira i dodaje novog partnera.
   * 
   * @param id ID partnera
   * @param naziv naziv partnera
   * @param vrstaKuhinje vrsta kuhinje
   * @param parametri ostali parametri partnera
   * @param out izlaz za odgovor
   */
  private void kreirajIDodajPartnera(int id, String naziv, String vrstaKuhinje, String[] parametri,
      PrintWriter out) {
    String adresa = parametri[1];
    int mreznaVrata = Integer.parseInt(parametri[2]);
    float gpsSirina = Float.parseFloat(parametri[3]);
    float gpsDuzina = Float.parseFloat(parametri[4]);

    String podatakZaKod = naziv + adresa;
    int hash = podatakZaKod.hashCode();
    String sigurnosniKod = Integer.toHexString(hash);

    Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina,
        gpsDuzina, sigurnosniKod);
    partneri.add(noviPartner);

    spremiPartnere();

    out.write("OK " + sigurnosniKod + "\n");
    out.flush();
  }

  /**
   * Obrađuje komandu za brisanje partnera.
   * 
   * @param linija komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduObrisi(String linija, PrintWriter out) {
    try {
      String[] dijelovi = linija.trim().split(" ");

      if (dijelovi.length != 3) {
        out.write("ERROR 20 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      int id = Integer.parseInt(dijelovi[1]);
      String sigurnosniKod = dijelovi[2];

      Partner partnerZaBrisanje = pronadiPartnera(id);

      if (partnerZaBrisanje == null) {
        out.write(
            "ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
        out.flush();
        return;
      }

      if (!partnerZaBrisanje.sigurnosniKod().equals(sigurnosniKod)) {
        out.write("ERROR 22 - Neispravan sigurnosni kod partnera\n");
        out.flush();
        return;
      }

      partneri.remove(partnerZaBrisanje);
      spremiPartnere();

      out.write("OK\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 29 - Došlo je do greške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Pronalazi partnera prema ID-u.
   * 
   * @param id ID partnera
   * @return pronađeni partner ili null ako ne postoji
   */
  private Partner pronadiPartnera(int id) {
    for (Partner p : partneri) {
      if (p.id() == id) {
        return p;
      }
    }
    return null;
  }

  /**
   * Obrađuje komandu za ispis popisa partnera.
   * 
   * @param out izlaz za odgovor
   */
  private void obradiKomanduPopis(PrintWriter out) {
    try {
      List<PartnerPopis> popisPartnera = new ArrayList<>();

      for (Partner p : partneri) {
        popisPartnera.add(new PartnerPopis(p.id(), p.naziv(), p.vrstaKuhinje(), p.adresa(),
            p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina()));
      }

      String jsonPopis = gson.toJson(popisPartnera);

      out.write("OK\n");
      out.write(jsonPopis + "\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 29 - Došlo je do greške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Sprema podatke o partnerima u datoteku.
   */
  private void spremiPartnere() {
    try {
      String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
      OutputStreamWriter writer =
          new OutputStreamWriter(new FileOutputStream(datotekaPartnera), StandardCharsets.UTF_8);
      gson.toJson(partneri, writer);
      writer.close();
    } catch (IOException e) {
    }
  }

  /**
   * Pokreće poslužitelj za rad s partnerima.
   */
  public void pokreniPosluziteljRad() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));

    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          Socket mreznaUticnica = ss.accept();
          this.executor.submit(() -> {
            aktivneDretve.add(Thread.currentThread());
            try {
              return obradiRad(mreznaUticnica);
            } finally {
              aktivneDretve.remove(Thread.currentThread());
            }
          });
        } catch (IOException e) {
          if (!this.kraj.get()) {
          }
        }
      }
    } catch (IOException e) {
    }
  }

  /**
   * Obrađuje zahtjeve za rad s partnerima.
   * 
   * @param mreznaUticnica mrežna utičnica za komunikaciju
   * @return Boolean.TRUE ako je obrada uspješna, inače Boolean.FALSE
   */
  private Boolean obradiRad(Socket mreznaUticnica) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();

      if (!provjeriKomandaRad(linija, out)) {
        zatvoriVezu(mreznaUticnica);
        return Boolean.FALSE;
      }

      obradiKomanduRad(linija, in, out);

      zatvoriVezu(mreznaUticnica);
    } catch (Exception e) {
      zatvoriVezu(mreznaUticnica);
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }

  /**
   * Provjerava je li komanda za rad ispravna.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   * @return true ako je komanda ispravna, inače false
   */
  private boolean provjeriKomandaRad(String linija, PrintWriter out) {
    if (linija == null) {
      out.write("ERROR 39 - Nešto drugo nije u redu\n");
      out.flush();
      return false;
    }

    if (linija.trim().isEmpty()) {
      out.write("ERROR 30 - Format komande nije ispravan (prazna komanda)\n");
      out.flush();
      return false;
    }

    String komanda = linija.split(" ", 2)[0];

    if (komanda.equals("JELOVNIK") || komanda.equals("KARTAPIĆA") || komanda.equals("OBRAČUN")) {
      if (!linija.startsWith(komanda + " ")) {
        out.write(
            "ERROR 30 - Format komande nije ispravan (nedostaje razmak nakon naziva komande)\n");
        out.flush();
        return false;
      }
    }

    return true;
  }

  /**
   * Obrađuje komande za rad.
   * 
   * @param linija primljena komanda
   * @param in ulaz za čitanje podataka
   * @param out izlaz za odgovor
   */
  private void obradiKomanduRad(String linija, BufferedReader in, PrintWriter out) {
    if (linija.startsWith("JELOVNIK ")) {
      obradiKomanduJelovnik(linija, out);
    } else if (linija.startsWith("KARTAPIĆA ")) {
      obradiKomanduKartaPica(linija, out);
    } else if (linija.startsWith("OBRAČUN ")) {
      obradiKomanduObracun(linija, in, out);
    } else {
      out.write("ERROR 30 - Format komande nije ispravan\n");
      out.flush();
    }
  }

  /**
   * Obrađuje komandu za dohvat jelovnika.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduJelovnik(String linija, PrintWriter out) {
    try {
      String[] dijelovi = linija.trim().split(" ");

      if (dijelovi.length != 3) {
        out.write("ERROR 30 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      int id = Integer.parseInt(dijelovi[1]);
      String sigurnosniKod = dijelovi[2];

      Partner partner = provjeriPartneraISigurnosniKod(id, sigurnosniKod, out);
      if (partner == null) {
        return;
      }

      List<Jelovnik> jelovnikPartnera = dohvatiJelovnikPartnera(partner);

      if (jelovnikPartnera.isEmpty()) {
        out.write("ERROR 32 - Ne postoji jelovnik s vrstom kuhinje koju partner ima ugovorenu\n");
        out.flush();
        return;
      }

      String jsonJelovnik = gson.toJson(jelovnikPartnera);

      out.write("OK\n");
      out.write(jsonJelovnik + "\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 39 - Došlo je do greške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Provjerava postoji li partner i je li sigurnosni kod ispravan.
   * 
   * @param id ID partnera
   * @param sigurnosniKod sigurnosni kod
   * @param out izlaz za odgovor
   * @return partner ako je pronađen i sigurnosni kod ispravan, inače null
   */
  private Partner provjeriPartneraISigurnosniKod(int id, String sigurnosniKod, PrintWriter out) {
    Partner partner = pronadiPartnera(id);

    if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
      out.write(
          "ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
      out.flush();
      return null;
    }

    return partner;
  }

  /**
   * Dohvaća jelovnik za partnera.
   * 
   * @param partner partner za kojeg se dohvaća jelovnik
   * @return lista jela iz jelovnika partnera
   */
  private List<Jelovnik> dohvatiJelovnikPartnera(Partner partner) {
    List<Jelovnik> jelovnikPartnera = new ArrayList<>();
    String vrstaKuhinje = partner.vrstaKuhinje();

    for (Jelovnik j : jelovnici) {
      if (j.id().startsWith(vrstaKuhinje)) {
        jelovnikPartnera.add(j);
      }
    }

    return jelovnikPartnera;
  }

  /**
   * Obrađuje komandu za dohvat karte pića.
   * 
   * @param linija primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduKartaPica(String linija, PrintWriter out) {
    try {
      String[] dijelovi = linija.trim().split(" ");

      if (dijelovi.length != 3) {
        out.write("ERROR 30 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      int id = Integer.parseInt(dijelovi[1]);
      String sigurnosniKod = dijelovi[2];

      Partner partner = provjeriPartneraISigurnosniKod(id, sigurnosniKod, out);
      if (partner == null) {
        return;
      }

      if (kartaPica.isEmpty()) {
        out.write("ERROR 34 - Neispravna karta pića\n");
        out.flush();
        return;
      }

      String jsonKartaPica = gson.toJson(kartaPica);

      out.write("OK\n");
      out.write(jsonKartaPica + "\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 39 - Greška pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Obrađuje komandu za obračun.
   * 
   * @param linija primljena komanda
   * @param in ulaz za čitanje podataka
   * @param out izlaz za odgovor
   */
  private synchronized void obradiKomanduObracun(String linija, BufferedReader in,
      PrintWriter out) {
    try {
      String[] dijelovi = linija.trim().split(" ");

      if (dijelovi.length != 3) {
        out.write("ERROR 30 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      int id = Integer.parseInt(dijelovi[1]);
      String sigurnosniKod = dijelovi[2];

      Partner partner = provjeriPartneraISigurnosniKod(id, sigurnosniKod, out);
      if (partner == null) {
        return;
      }

      String jsonObracun = procitajJsonObracun(in);

      List<Obracun> noviObracuni = validirajObracun(jsonObracun, id, out);
      if (noviObracuni == null) {
        return;
      }

      azurirajIspremiObracune(noviObracuni);

      out.write("OK\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 39 - Nešto drugo nije u redu\n");
      out.flush();
    }
  }

  /**
   * Čita JSON obračun iz ulaznog toka.
   * 
   * @param in ulazni tok
   * @return JSON string s obračunom
   * @throws IOException ako dođe do greške pri čitanju
   */
  private String procitajJsonObracun(BufferedReader in) throws IOException {
    StringBuilder jsonObracun = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null && !line.trim().endsWith("]")) {
      jsonObracun.append(line).append("\n");
    }

    if (line != null) {
      jsonObracun.append(line);
    }

    return jsonObracun.toString();
  }

  /**
   * Validira obračun.
   * 
   * @param jsonObracun JSON string s obračunom
   * @param idPartnera ID partnera
   * @param out izlaz za odgovor
   * @return lista obračuna ako je validacija uspješna, inače null
   */
  private List<Obracun> validirajObracun(String jsonObracun, int idPartnera, PrintWriter out) {
    try {
      List<Obracun> noviObracuni =
          gson.fromJson(jsonObracun, new TypeToken<List<Obracun>>() {}.getType());

      if (noviObracuni == null) {
        out.write("ERROR 35 - Neispravan obračun: neispravan JSON format\n");
        out.flush();
        return null;
      }

      for (Obracun o : noviObracuni) {
        if (!validirajStavkuObracuna(o, idPartnera, out)) {
          return null;
        }
      }

      return noviObracuni;

    } catch (Exception e) {
      out.write("ERROR 35 - Neispravan obračun: pogreška pri parsiranju JSON-a\n");
      out.flush();
      return null;
    }
  }

  /**
   * Validira stavku obračuna.
   * 
   * @param obracun stavka obračuna
   * @param idPartnera ID partnera
   * @param out izlaz za odgovor
   * @return true ako je stavka ispravna, inače false
   */
  private boolean validirajStavkuObracuna(Obracun obracun, int idPartnera, PrintWriter out) {
    if (obracun.partner() != idPartnera) {
      out.write("ERROR 35 - Neispravan obračun: ID partnera ne odgovara\n");
      out.flush();
      return false;
    }

    String itemId = obracun.id();
    boolean isJelo = obracun.jelo();
    boolean validId = validirajIdStavke(itemId, isJelo, idPartnera);

    if (!validId) {
      out.write("ERROR 35 - Neispravan obračun: nepostojeći ID jela/pića\n");
      out.flush();
      return false;
    }

    if (obracun.kolicina() < 0) {
      out.write("ERROR 35 - Neispravan obračun: količina ne može biti negativna\n");
      out.flush();
      return false;
    }

    return true;
  }

  /**
   * Validira ID stavke.
   * 
   * @param itemId ID stavke
   * @param isJelo true ako je stavka jelo, false ako je piće
   * @param idPartnera ID partnera
   * @return true ako je ID ispravan, inače false
   */
  private boolean validirajIdStavke(String itemId, boolean isJelo, int idPartnera) {
    if (isJelo) {
      Partner partner = pronadiPartnera(idPartnera);
      if (partner != null) {
        String vrstaKuhinje = partner.vrstaKuhinje();
        for (Jelovnik j : jelovnici) {
          if (j.id().equals(itemId) && j.id().startsWith(vrstaKuhinje)) {
            return true;
          }
        }
      }
    } else {
      for (KartaPica p : kartaPica) {
        if (p.id().equals(itemId)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Ažurira i sprema obračune.
   * 
   * @param noviObracuni novi obračuni
   * @throws IOException ako dođe do greške pri pisanju
   */
  private void azurirajIspremiObracune(List<Obracun> noviObracuni) throws IOException {
    try {
      ucitajPostojeceObracune();
    } catch (IOException e) {
    }

    if (noviObracuni != null) {
      obracuni.addAll(noviObracuni);
    }

    String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
    FileWriter writer = new FileWriter(datotekaObracuna);
    gson.toJson(obracuni, writer);
    writer.close();
  }

  /**
   * Učitava postojeće obračune.
   * 
   * @throws IOException ako dođe do greške pri čitanju
   */
  private void ucitajPostojeceObracune() throws IOException {
    String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
    BufferedReader reader = new BufferedReader(new FileReader(datotekaObracuna));
    List<Obracun> postojeciObracuni =
        gson.fromJson(reader, new TypeToken<List<Obracun>>() {}.getType());
    reader.close();

    if (postojeciObracuni != null) {
      obracuni = postojeciObracuni;
    }
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

  /**
   * Zatvara mrežnu utičnicu i povećava brojač zatvorenih veza.
   * 
   * @param mreznaUticnica veza koju treba zatvoriti
   */
  private void zatvoriVezu(Socket mreznaUticnica) {
    if (mreznaUticnica != null && !mreznaUticnica.isClosed()) {
      try {
        mreznaUticnica.close();
        brojZatvorenihVeza.incrementAndGet();
      } catch (IOException e) {
      }
    }
  }
}
