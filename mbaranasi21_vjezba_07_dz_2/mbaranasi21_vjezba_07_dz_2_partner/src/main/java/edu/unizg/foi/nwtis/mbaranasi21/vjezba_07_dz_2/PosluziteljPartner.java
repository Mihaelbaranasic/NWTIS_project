package edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * Poslužitelj partnera koji obrađuje narudžbe kupaca.
 */
public class PosluziteljPartner {

  /** Konfiguracijski podaci */
  private Konfiguracija konfig;
  /** Predložak za kraj */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");
  /** Predložak za partner */
  private Pattern predlozakPartner = Pattern.compile("^PARTNER$");
  /** Gson objekt za rad s JSON-om */
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();
  /** Kolekcija jelovnika */
  private List<Jelovnik> jelovnici = new ArrayList<>();
  /** Kolekcija karte pića */
  private List<KartaPica> kartaPica = new ArrayList<>();
  /** Broj naplaćenih narudžbi */
  private int brojNaplacenihNarudzbi = 0;
  /** Zastavica za kraj rada */
  private volatile boolean kraj = false;
  /** Izvršitelj dretve */
  private ExecutorService executor;
  /** Brojač prekinutih dretvi */
  private AtomicInteger brojPrekinutihDretvi = new AtomicInteger(0);
  /** Brojač zatvorenih veza */
  private AtomicInteger brojZatvorenihVeza = new AtomicInteger(0);
  /** Lista aktivnih dretvi */
  private List<Thread> aktivneDretve = Collections.synchronizedList(new ArrayList<>());
  /** Mapa otvorenih narudžbi po korisnicima */
  private Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
  /** Mapa plaćenih narudžbi po korisnicima */
  private Map<String, List<Narudzba>> placeneNarudzbe = new ConcurrentHashMap<>();

  private int kvotaNarudzbi = 10;

  /**
   * Glavna metoda za pokretanje poslužitelja partnera.
   * 
   * @param args argumenti naredbenog retka
   */
  public static void main(String[] args) {
    if (args.length > 2) {
      System.out.println("Broj argumenata veći od 2.");
      return;
    }
    var program = new PosluziteljPartner();
    dodajShutdownHook(program);

    var nazivDatoteke = args[0];
    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }

    if (args.length == 1) {
      program.registrirajPartnera();
      return;
    }

    var linija = args[1];
    program.obradiArgument(linija);
  }

  /**
   * Dodaje shutdown hook za čišćenje resursa.
   * 
   * @param program instanca poslužitelja partnera
   */
  private static void dodajShutdownHook(PosluziteljPartner program) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      program.kraj = true;
      for (Thread dretva : program.aktivneDretve) {
        if (dretva != null && dretva.isAlive()) {
          dretva.interrupt();
          program.brojPrekinutihDretvi.incrementAndGet();
        }
      }

      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }));
  }

  /**
   * Obrađuje argument naredbenog retka.
   * 
   * @param linija argument koji treba obraditi
   */
  private void obradiArgument(String linija) {
    var poklapanjeKraj = this.predlozakKraj.matcher(linija);
    var statusKraj = poklapanjeKraj.matches();
    if (statusKraj) {
      this.posaljiKraj();
      return;
    }

    var poklapanjePartner = this.predlozakPartner.matcher(linija);
    var statusPartner = poklapanjePartner.matches();
    if (statusPartner) {
      this.pokreniPosluzitelj();
      return;
    }
  }

  /**
   * Pokreće poslužitelj za obradu zahtjeva kupaca.
   */
  private void pokreniPosluzitelj() {
    if (!this.konfig.postojiPostavka("sigKod")) {
      return;
    }

    if (!preuzmiJelovnik() || !preuzmiKartuPica()) {
      return;
    }

    inicijalizirajPosluzitelj();

    int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
    int brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    int pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
    pokreniServerSocket(mreznaVrata, brojCekaca, pauzaDretve);
  }

  /**
   * Inicijalizira poslužitelj.
   */
  private void inicijalizirajPosluzitelj() {
    var builder = Thread.ofVirtual();
    var factory = builder.factory();
    this.executor = Executors.newThreadPerTaskExecutor(factory);

    if (this.konfig.postojiPostavka("kvotaNarudzbi")) {
      this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
    }
  }

  /**
   * Pokreće server socket i čeka na zahtjeve kupaca.
   * 
   * @param mreznaVrata mreža vrata na kojima poslužitelj sluša
   * @param brojCekaca broj čekača u redu
   * @param pauzaDretve pauza između obrade zahtjeva
   */
  private void pokreniServerSocket(int mreznaVrata, int brojCekaca, int pauzaDretve) {
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj) {
        try {
          Socket socket = ss.accept();
          this.executor.submit(() -> {
            aktivneDretve.add(Thread.currentThread());
            try {
              obradiZahtjevKupca(socket);
            } finally {
              aktivneDretve.remove(Thread.currentThread());
            }
          });
        } catch (IOException e) {
          if (!this.kraj) {
          }
        }

        try {
          Thread.sleep(pauzaDretve);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (IOException e) {
    } finally {
      if (this.executor != null) {
        this.executor.shutdown();
      }
    }
  }

  /**
   * Obrađuje zahtjev kupca.
   * 
   * @param socket mrežna utičnica za komunikaciju
   */
  private void obradiZahtjevKupca(Socket socket) {
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

      String komanda = in.readLine();
      if (!provjeriKomandu(komanda, out)) {
        zatvoriVezu(socket);
        return;
      }

      obradiKomandu(komanda, out);

      zatvoriVezu(socket);

    } catch (Exception e) {
      zatvoriVezu(socket);
    }
  }

  /**
   * Provjerava je li komanda ispravna.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   * @return true ako je komanda ispravna, inače false
   */
  private boolean provjeriKomandu(String komanda, PrintWriter out) {
    if (komanda == null) {
      zatvoriVezu(out);
      return false;
    }

    if (komanda.trim().length() == 0) {
      out.write("ERROR 40 - Format komande nije ispravan\n");
      out.flush();
      return false;
    }

    String[] dijelovi = komanda.split(" ", 2);
    String nazivKomande = dijelovi[0];

    if (!nazivKomande.equals(nazivKomande.toUpperCase())) {
      out.write("ERROR 40 - Format komande nije ispravan\n");
      out.flush();
      return false;
    }

    if (nazivKomande.equals("JELOVNIK") || nazivKomande.equals("KARTAPIĆA")
        || nazivKomande.equals("NARUDŽBA") || nazivKomande.equals("JELO")
        || nazivKomande.equals("PIĆE") || nazivKomande.equals("RAČUN")) {

      if (dijelovi.length < 2 || dijelovi[1].trim().isEmpty()) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return false;
      }
    }

    return true;
  }

  /**
   * Zatvara vezu preko izlaznog toka.
   * 
   * @param out izlazni tok
   */
  private void zatvoriVezu(PrintWriter out) {
    out.write("ERROR 49 - Došlo je do pogreške\n");
    out.flush();
  }

  /**
   * Obrađuje primljenu komandu.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomandu(String komanda, PrintWriter out) {
    if (komanda.startsWith("JELOVNIK ")) {
      obradiKomanduJelovnik(komanda, out);
    } else if (komanda.startsWith("KARTAPIĆA ")) {
      obradiKomanduKartaPica(komanda, out);
    } else if (komanda.startsWith("NARUDŽBA ")) {
      obradiKomanduNarudzba(komanda, out);
    } else if (komanda.startsWith("JELO ")) {
      obradiKomanduJelo(komanda, out);
    } else if (komanda.startsWith("PIĆE ")) {
      obradiKomanduPice(komanda, out);
    } else if (komanda.startsWith("RAČUN ")) {
      obradiKomanduRacun(komanda, out);
    } else {
      out.write("ERROR 49 - Nepostojeća komanda\n");
      out.flush();
    }
  }

  /**
   * Šalje komandu za kraj rada poslužitelja tvrtke.
   */
  private void posaljiKraj() {
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      out.write("KRAJ " + kodZaKraj + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();

      var linija = in.readLine();
      mreznaUticnica.shutdownInput();

      if (linija.equals("OK")) {
      }

      zatvoriVezu(mreznaUticnica);
    } catch (IOException e) {
    }
  }

  /**
   * Registrira partnera kod poslužitelja tvrtke.
   */
  private void registrirajPartnera() {
    if (this.konfig.postojiPostavka("sigKod") && !this.konfig.dajPostavku("sigKod").isEmpty()) {
      return;
    }

    try {
      Socket mreznaUticnica = uspostaviVezuZaRegistraciju();
      if (mreznaUticnica == null)
        return;

      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String komanda = kreirajKomanduZaRegistraciju();

      out.write(komanda);
      out.flush();
      mreznaUticnica.shutdownOutput();

      obradiOdgovorRegistracije(in);
      mreznaUticnica.shutdownInput();

      zatvoriVezu(mreznaUticnica);

    } catch (Exception e) {
    }
  }

  /**
   * Uspostavlja vezu za registraciju partnera.
   * 
   * @return socket za komunikaciju ili null ako dođe do greške
   */
  private Socket uspostaviVezuZaRegistraciju() {
    try {
      var adresa = this.konfig.dajPostavku("adresa");
      var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
      return new Socket(adresa, mreznaVrata);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Kreira komandu za registraciju partnera.
   * 
   * @return komanda za registraciju
   */
  private String kreirajKomanduZaRegistraciju() {
    int id = Integer.parseInt(this.konfig.dajPostavku("id"));
    String naziv = this.konfig.dajPostavku("naziv");
    String kuhinja = this.konfig.dajPostavku("kuhinja");
    String partnerAdresa = this.konfig.dajPostavku("adresa");
    int partnerMreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
    float gpsSirina = Float.parseFloat(this.konfig.dajPostavku("gpsSirina"));
    float gpsDuzina = Float.parseFloat(this.konfig.dajPostavku("gpsDuzina"));

    return String.format("PARTNER %d \"%s\" %s %s %d %.5f %.5f\n", id, naziv, kuhinja,
        partnerAdresa, partnerMreznaVrata, gpsSirina, gpsDuzina);
  }

  /**
   * Obrađuje odgovor na registraciju partnera.
   * 
   * @param in ulazni tok
   * @throws IOException ako dođe do greške pri čitanju
   */
  private void obradiOdgovorRegistracije(BufferedReader in) throws IOException {
    String odgovor = in.readLine();
    if (odgovor != null && odgovor.startsWith("OK")) {
      String[] dijelovi = odgovor.split(" ");
      if (dijelovi.length >= 2) {
        String sigKod = dijelovi[1];
        this.konfig.spremiPostavku("sigKod", sigKod);
        try {
          this.konfig.spremiKonfiguraciju();
        } catch (NeispravnaKonfiguracija e) {
        }
      }
    }
  }

  /**
   * Preuzima jelovnik od poslužitelja tvrtke.
   * 
   * @return true ako je preuzimanje uspjelo, inače false
   */
  private boolean preuzmiJelovnik() {
    Socket socket = null;
    try {
      socket = uspostaviVezuZaRad();
      if (socket == null)
        return false;

      BufferedReader in =
          new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

      String komanda = "JELOVNIK " + this.konfig.dajPostavku("id") + " "
          + this.konfig.dajPostavku("sigKod") + "\n";
      out.write(komanda);
      out.flush();

      return procitajOdgovorJelovnik(in, socket);

    } catch (Exception e) {
      zatvoriVezu(socket);
      return false;
    }
  }

  /**
   * Uspostavlja vezu za rad s poslužiteljem tvrtke.
   * 
   * @return socket za komunikaciju ili null ako dođe do greške
   */
  private Socket uspostaviVezuZaRad() {
    try {
      String adresa = this.konfig.dajPostavku("adresa");
      int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
      return new Socket(adresa, mreznaVrataRad);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Čita odgovor s jelovnikom.
   * 
   * @param in ulazni tok
   * @param socket mrežna utičnica
   * @return true ako je čitanje uspjelo, inače false
   * @throws IOException ako dođe do greške pri čitanju
   */
  private boolean procitajOdgovorJelovnik(BufferedReader in, Socket socket) throws IOException {
    String odgovorStatus = in.readLine();
    if (odgovorStatus != null && odgovorStatus.equals("OK")) {
      StringBuilder jsonBuilder = new StringBuilder();
      String red;
      while ((red = in.readLine()) != null) {
        jsonBuilder.append(red);
      }

      String json = jsonBuilder.toString();
      this.jelovnici = gson.fromJson(json, new TypeToken<List<Jelovnik>>() {}.getType());

      zatvoriVezu(socket);
      return true;
    } else {
      zatvoriVezu(socket);
      return false;
    }
  }

  /**
   * Preuzima kartu pića od poslužitelja tvrtke.
   * 
   * @return true ako je preuzimanje uspjelo, inače false
   */
  private boolean preuzmiKartuPica() {
    Socket socket = null;
    try {
      socket = uspostaviVezuZaRad();
      if (socket == null)
        return false;

      BufferedReader in =
          new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

      String komanda = "KARTAPIĆA " + this.konfig.dajPostavku("id") + " "
          + this.konfig.dajPostavku("sigKod") + "\n";
      out.write(komanda);
      out.flush();

      return procitajOdgovorKartaPica(in, socket);

    } catch (Exception e) {
      zatvoriVezu(socket);
      return false;
    }
  }

  /**
   * Čita odgovor s kartom pića.
   * 
   * @param in ulazni tok
   * @param socket mrežna utičnica
   * @return true ako je čitanje uspjelo, inače false
   * @throws IOException ako dođe do greške pri čitanju
   */
  private boolean procitajOdgovorKartaPica(BufferedReader in, Socket socket) throws IOException {
    String odgovorStatus = in.readLine();
    if (odgovorStatus != null && odgovorStatus.equals("OK")) {
      StringBuilder jsonBuilder = new StringBuilder();
      String red;
      while ((red = in.readLine()) != null) {
        jsonBuilder.append(red);
      }

      String json = jsonBuilder.toString();
      this.kartaPica = gson.fromJson(json, new TypeToken<List<KartaPica>>() {}.getType());

      zatvoriVezu(socket);
      return true;
    } else {
      zatvoriVezu(socket);
      return false;
    }
  }

  /**
   * Obrađuje komandu za dohvat jelovnika.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduJelovnik(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 2) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String jsonJelovnik = gson.toJson(this.jelovnici);

      out.write("OK\n");
      out.write(jsonJelovnik + "\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Obrađuje komandu za dohvat karte pića.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private void obradiKomanduKartaPica(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 2) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String jsonKartaPica = gson.toJson(this.kartaPica);

      out.write("OK\n");
      out.write(jsonKartaPica + "\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Obrađuje komandu za kreiranje nove narudžbe.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private synchronized void obradiKomanduNarudzba(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 2) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String korisnik = dijelovi[1];

      if (otvoreneNarudzbe.containsKey(korisnik) && !otvoreneNarudzbe.get(korisnik).isEmpty()) {
        out.write("ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca\n");
        out.flush();
        return;
      }

      otvoreneNarudzbe.put(korisnik, new ArrayList<>());

      out.write("OK\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 49 - " + e.getMessage() + "\n");
      out.flush();
    }
  }

  /**
   * Obrađuje komandu za dodavanje jela u narudžbu.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private synchronized void obradiKomanduJelo(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 4) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String korisnik = dijelovi[1];
      String idJela = dijelovi[2];
      float kolicina = Float.parseFloat(dijelovi[3]);

      if (!provjeriPostojiNarudzba(korisnik, out)) {
        return;
      }

      Jelovnik jelo = pronadiJelo(idJela);
      if (jelo == null) {
        out.write("ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n");
        out.flush();
        return;
      }

      dodajStavkuNarudzbe(korisnik, idJela, true, kolicina, jelo.cijena());

      out.write("OK\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Provjerava postoji li otvorena narudžba za korisnika.
   * 
   * @param korisnik korisnik
   * @param out izlaz za odgovor
   * @return true ako narudžba postoji, inače false
   */
  private boolean provjeriPostojiNarudzba(String korisnik, PrintWriter out) {
    if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
      out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n");
      out.flush();
      return false;
    }
    return true;
  }

  /**
   * Pronalazi jelo prema ID-u.
   * 
   * @param idJela ID jela
   * @return pronađeno jelo ili null ako ne postoji
   */
  private Jelovnik pronadiJelo(String idJela) {
    for (Jelovnik j : jelovnici) {
      if (j.id().equals(idJela)) {
        return j;
      }
    }
    return null;
  }

  /**
   * Dodaje stavku u narudžbu.
   * 
   * @param korisnik korisnik
   * @param id ID jela/pića
   * @param jelo true ako je jelo, false ako je piće
   * @param kolicina količina
   * @param cijena cijena
   */
  private void dodajStavkuNarudzbe(String korisnik, String id, boolean jelo, float kolicina,
      float cijena) {
    int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
    Narudzba stavka =
        new Narudzba(korisnik, id, jelo, kolicina, cijena, System.currentTimeMillis());
    otvoreneNarudzbe.get(korisnik).add(stavka);
  }

  /**
   * Obrađuje komandu za dodavanje pića u narudžbu.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private synchronized void obradiKomanduPice(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 4) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String korisnik = dijelovi[1];
      String idPica = dijelovi[2];
      float kolicina = Float.parseFloat(dijelovi[3]);

      if (!provjeriPostojiNarudzba(korisnik, out)) {
        return;
      }

      KartaPica pice = pronadiPice(idPica);
      if (pice == null) {
        out.write("ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera\n");
        out.flush();
        return;
      }

      dodajStavkuNarudzbe(korisnik, idPica, false, kolicina, pice.cijena());

      out.write("OK\n");
      out.flush();

    } catch (Exception e) {
      out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Pronalazi piće prema ID-u.
   * 
   * @param idPica ID pića
   * @return pronađeno piće ili null ako ne postoji
   */
  private KartaPica pronadiPice(String idPica) {
    for (KartaPica p : kartaPica) {
      if (p.id().equals(idPica)) {
        return p;
      }
    }
    return null;
  }

  /**
   * Obrađuje komandu za naplatu narudžbe.
   * 
   * @param komanda primljena komanda
   * @param out izlaz za odgovor
   */
  private synchronized void obradiKomanduRacun(String komanda, PrintWriter out) {
    try {
      String[] dijelovi = komanda.trim().split(" ");
      if (dijelovi.length != 2) {
        out.write("ERROR 40 - Format komande nije ispravan\n");
        out.flush();
        return;
      }

      String korisnik = dijelovi[1];

      if (!provjeriPostojiNarudzba(korisnik, out)) {
        return;
      }

      List<Narudzba> narudzba = otvoreneNarudzbe.get(korisnik);

      azurirajPlaceneNarudzbe(korisnik, narudzba);
      otvoreneNarudzbe.remove(korisnik);

      brojNaplacenihNarudzbi++;

      if (brojNaplacenihNarudzbi % this.kvotaNarudzbi == 0) {
        posaljiObracunTvrtki(out);
      } else {
        out.write("OK\n");
        out.flush();
      }

    } catch (Exception e) {
      out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
      out.flush();
    }
  }

  /**
   * Ažurira plaćene narudžbe.
   * 
   * @param korisnik korisnik
   * @param narudzba lista narudžbi
   */
  private void azurirajPlaceneNarudzbe(String korisnik, List<Narudzba> narudzba) {
    if (!placeneNarudzbe.containsKey(korisnik)) {
      placeneNarudzbe.put(korisnik, new ArrayList<>());
    }

    placeneNarudzbe.get(korisnik).addAll(narudzba);
  }

  /**
   * Šalje obračun tvrtki i vraća odgovor korisniku.
   * 
   * @param out izlaz za odgovor korisniku
   */
  private void posaljiObracunTvrtki(PrintWriter out) {
    List<Obracun> obracuni = kreirajObracun();

    if (posaljiObracun(obracuni)) {
      placeneNarudzbe.clear();

      String jsonObracun = gson.toJson(obracuni);
      out.write("OK\n");
      out.write(jsonObracun + "\n");
      out.flush();
    } else {
      out.write("ERROR 45 - Neuspješno slanje obračuna\n");
      out.flush();
    }
  }

  /**
   * Kreira obračun iz plaćenih narudžbi.
   * 
   * @return lista stavki obračuna
   */
  private List<Obracun> kreirajObracun() {
    List<Obracun> obracuni = new ArrayList<>();

    Map<String, Float> kolicinePoID = new HashMap<>();
    Map<String, Float> cijenePoID = new HashMap<>();
    Map<String, Boolean> jeLiJelo = new HashMap<>();

    for (List<Narudzba> narudzbe : placeneNarudzbe.values()) {
      for (Narudzba n : narudzbe) {
        String id = n.id();
        boolean jelo = n.jelo();
        String kljuc = id + (jelo ? "##jelo" : "##pice");

        kolicinePoID.put(kljuc, kolicinePoID.getOrDefault(kljuc, 0f) + n.kolicina());
        cijenePoID.put(kljuc, n.cijena());
        jeLiJelo.put(kljuc, jelo);
      }
    }

    int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));

    for (Map.Entry<String, Float> entry : kolicinePoID.entrySet()) {
      String kljuc = entry.getKey();
      String[] dijeloviKljuca = kljuc.split("##");
      String id = dijeloviKljuca[0];
      boolean jelo = jeLiJelo.get(kljuc);
      float kolicina = entry.getValue();
      float cijena = cijenePoID.get(kljuc);

      Obracun o = new Obracun(idPartnera, id, jelo, kolicina, cijena, System.currentTimeMillis());
      obracuni.add(o);
    }

    return obracuni;
  }

  /**
   * Šalje obračun poslužitelju tvrtke.
   * 
   * @param obracuni lista stavki obračuna
   * @return true ako je slanje uspjelo, inače false
   */
  private boolean posaljiObracun(List<Obracun> obracuni) {
    try {
      String jsonObracun = gson.toJson(obracuni);

      Socket socket = uspostaviVezuZaRad();
      if (socket == null)
        return false;

      BufferedReader in =
          new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

      String komanda = "OBRAČUN " + this.konfig.dajPostavku("id") + " "
          + this.konfig.dajPostavku("sigKod") + "\n";
      out.write(komanda);
      out.write(jsonObracun + "\n");
      out.flush();

      String odgovor = in.readLine();
      if (odgovor != null && odgovor.equals("OK")) {
        socket.close();
        return true;
      } else {
        socket.close();
        return false;
      }

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Ucitaj konfiguraciju.
   *
   * @param nazivDatoteke naziv datoteke
   * @return true, ako je uspješno učitavanje konfiguracije
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
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
