package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    /** Lista partnera */
    private List<Partner> partneri = new ArrayList<>();
    /** Kolekcija jelovnika */
    private List<Jelovnik> jelovnici = new ArrayList<>();
    /** Kolekcija karte pića */
    private List<KartaPica> kartaPica = new ArrayList<>();
    /** Kolekcija obračuna */
    private List<Obracun> obracuni = new ArrayList<>();
    /** Gson objekt za rad s JSON-om */
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private AtomicInteger brojPrekinutihDretvi = new AtomicInteger(0);
    private AtomicInteger brojZatvorenihVeza = new AtomicInteger(0);
    private List<Thread> aktivneDretve = Collections.synchronizedList(new ArrayList<>());

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
        
        // Učitavanje podataka
        ucitajPodatke();
        
        var builder = Thread.ofVirtual();
        var factory = builder.factory();
        this.executor = Executors.newThreadPerTaskExecutor(factory);
        
        // Pokretanje poslužitelja
        Future<?> dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
        Future<?> dretvaZaRegistraciju = this.executor.submit(() -> this.pokreniPosluziteljRegistracija());
        Future<?> dretvaZaRad = this.executor.submit(() -> this.pokreniPosluziteljRad());
        
        // Čekanje dok dretve ne završe ili dok ne dođe zahtjev za kraj
        while (!this.kraj.get()) {
            try {
                Thread.sleep(this.pauzaDretve);
                
                // Provjera je li kraj rada
                if (this.kraj.get()) {
                    if (!dretvaZaRegistraciju.isDone()) {
                        dretvaZaRegistraciju.cancel(true);
                    }
                    if (!dretvaZaRad.isDone()) {
                        dretvaZaRad.cancel(true);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Poslužitelj tvrtka završava s radom.");
    }

    /**
     * Učitava podatke iz datoteka: partnere, jelovnike i kartu pića
     */
    private void ucitajPodatke() {
        try {
            // Učitaj partnere
            String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
            BufferedReader reader = new BufferedReader(new FileReader(datotekaPartnera));
            this.partneri = gson.fromJson(reader, new TypeToken<List<Partner>>(){}.getType());
            reader.close();
            
            // Učitaj jelovnike za svaku kuhinju
            for (int i = 1; i <= 9; i++) {
                String kljucKuhinje = "kuhinja_" + i;
                if (this.konfig.postojiPostavka(kljucKuhinje)) {
                    String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
                    String[] dijelovi = vrijednostKuhinje.split(";");
                    if (dijelovi.length >= 1) {
                        String oznaka = dijelovi[0];
                        String datotekaJelovnika = kljucKuhinje + ".json";
                        
                        try {
                            reader = new BufferedReader(new FileReader(datotekaJelovnika));
                            List<Jelovnik> jelovniciKuhinje = gson.fromJson(reader, new TypeToken<List<Jelovnik>>(){}.getType());
                            this.jelovnici.addAll(jelovniciKuhinje);
                            reader.close();
                        } catch (IOException e) {
                            System.out.println("Nije moguće učitati jelovnik za kuhinju: " + kljucKuhinje);
                        }
                    }
                }
            }
            
            // Učitaj kartu pića
            String datotekaKartaPica = this.konfig.dajPostavku("datotekaKartaPica");
            reader = new BufferedReader(new FileReader(datotekaKartaPica));
            this.kartaPica = gson.fromJson(reader, new TypeToken<List<KartaPica>>(){}.getType());
            reader.close();
            
            System.out.println("Podaci uspješno učitani.");
        } catch (IOException e) {
            System.out.println("Greška pri učitavanju podataka: " + e.getMessage());
        }
    }
    
    public void pokreniPosluziteljKraj() {
        var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
        var brojCekaca = 0;
        try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
            while (!this.kraj.get()) {
                try {
                    var mreznaUticnica = ss.accept();
                    this.executor.submit(() -> {
                        // Dodajemo trenutnu dretvu u listu aktivnih
                        aktivneDretve.add(Thread.currentThread());
                        try {
                            return this.obradiKraj(mreznaUticnica);
                        } finally {
                            // Uklanjamo dretvu iz liste aktivnih kad završi
                            aktivneDretve.remove(Thread.currentThread());
                        }
                    });
                } catch (IOException e) {
                    if (!this.kraj.get()) {
                        System.out.println("Greška u poslužitelju za kraj: " + e.getMessage());
                    }
                }
            }
            ss.close();
        } catch (IOException e) {
            System.out.println("Greška pri pokretanju poslužitelja za kraj: " + e.getMessage());
        }
    }

    public Boolean obradiKraj(Socket mreznaUticnica) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
            
            String linija = in.readLine();
            mreznaUticnica.shutdownInput();
            
            if (linija == null) {
                out.write("ERROR 19 - Prazna datoteka\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            String[] dijelovi = linija.trim().split(" ");
            if (dijelovi.length != 2 || !dijelovi[0].equals("KRAJ") || !dijelovi[1].equals(this.kodZaKraj)) {
                out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            // Provjera lokalne adrese
            InetAddress adresaZahtjeva = mreznaUticnica.getInetAddress();
            InetAddress lokalnaAdresa = InetAddress.getLocalHost();
            
            if (!adresaZahtjeva.equals(lokalnaAdresa) && !adresaZahtjeva.isLoopbackAddress()) {
                out.write("ERROR 11 - Adresa računala s kojeg je poslan zahtjev nije lokalna adresa\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            out.write("OK\n");
            out.flush();
            this.kraj.set(true);
            
            zatvoriVezu(mreznaUticnica);
        } catch (Exception e) {
            System.out.println("Greška pri obradi zahtjeva za kraj: " + e.getMessage());
            zatvoriVezu(mreznaUticnica);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    /**
     * Pokreće poslužitelj za registraciju partnera
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
            System.out.println("Greška u poslužitelju za registraciju: " + e.getMessage());
        }
    }
    
    /**
     * Obrađuje zahtjeve za registraciju partnera
     */
    private Boolean obradiRegistraciju(Socket mreznaUticnica) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
            
            String linija = in.readLine();
            mreznaUticnica.shutdownInput();
            
            if (linija == null) {
                out.write("ERROR 29 - Prazna datoteka\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            // Obrada komande PARTNER
            if (linija.startsWith("PARTNER ")) {
                obradiKomanduPartner(linija, out);
            }
            // Obrada komande OBRIŠI
            else if (linija.startsWith("OBRIŠI ")) {
                obradiKomanduObrisi(linija, out);
            }
            // Obrada komande POPIS
            else if (linija.trim().equals("POPIS")) {
                obradiKomanduPopis(out);
            }
            // Nepoznata komanda
            else {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
            }
            
            zatvoriVezu(mreznaUticnica);
        } catch (Exception e) {
            System.out.println("Greška pri obradi zahtjeva za registraciju: " + e.getMessage());
            zatvoriVezu(mreznaUticnica);
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * Obrađuje komandu za registraciju partnera
     */
    private void obradiKomanduPartner(String linija, PrintWriter out) {
        try {
            // Format: PARTNER id "Naziv partnera" vrstaKuhinje adresa mreznaVrata gpsSirina gpsDuzina
            // Npr: PARTNER 1 "Roštilj Pero" MK localhost 8010 46.29950 16.33001

            // Izvlačenje naziva partnera između navodnika
            int pocetakNaziva = linija.indexOf("\"");
            int krajNaziva = linija.indexOf("\"", pocetakNaziva + 1);

            if (pocetakNaziva == -1 || krajNaziva == -1) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }

            String naziv = linija.substring(pocetakNaziva + 1, krajNaziva);

            // Preostali dio linije nakon zatvaranja navodnika
            String ostatakLinije = linija.substring(krajNaziva + 1).trim();
            String[] parametri = ostatakLinije.split(" ");

            if (parametri.length != 5) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }

            // Izdvajanje parametara
            String vrstaKuhinje = parametri[0];
            String adresa = parametri[1];
            int mreznaVrata = Integer.parseInt(parametri[2]);
            float gpsSirina = Float.parseFloat(parametri[3]);
            float gpsDuzina = Float.parseFloat(parametri[4]);

            // Izdvajanje ID-a partnera
            String[] prviDio = linija.substring(0, pocetakNaziva).trim().split(" ");
            if (prviDio.length != 2) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }
            int id = Integer.parseInt(prviDio[1]);

            // Provjera postoji li već partner s istim ID-om
            for (Partner p : partneri) {
                if (p.id() == id) {
                    out.write("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
                    out.flush();
                    return;
                }
            }

            // Provjera postoji li kuhinja
            boolean kuhinjaPostoji = false;
            for (int i = 1; i <= 9; i++) {
                String kljucKuhinje = "kuhinja_" + i;
                if (this.konfig.postojiPostavka(kljucKuhinje)) {
                    String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
                    String[] dijelovi = vrijednostKuhinje.split(";");
                    if (dijelovi.length >= 1 && dijelovi[0].equals(vrstaKuhinje)) {
                        kuhinjaPostoji = true;
                        break;
                    }
                }
            }

            if (!kuhinjaPostoji) {
                out.write("ERROR 29 - Registracija za nepostojeću kuhinju\n");
                out.flush();
                return;
            }

            // Generiranje sigurnosnog koda
            String podatakZaKod = naziv + adresa;
            int hash = podatakZaKod.hashCode();
            String sigurnosniKod = Integer.toHexString(hash);

            // Kreiranje novog partnera
            Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina, sigurnosniKod);
            partneri.add(noviPartner);

            // Spremanje u datoteku
            spremiPartnere();

            // Slanje odgovora
            out.write("OK " + sigurnosniKod + "\n");
            out.flush();

        } catch (Exception e) {
            System.out.println("Greška pri obradi komande PARTNER: " + e.getMessage());
            out.write("ERROR 29 - Nešto drugo nije u redu\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za brisanje partnera
     */
    private void obradiKomanduObrisi(String linija, PrintWriter out) {
        try {
            // Format: OBRIŠI id sigurnosniKod
            // Npr: OBRIŠI 1 4958583733
            String[] dijelovi = linija.trim().split(" ");
            
            if (dijelovi.length != 3) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }
            
            int id = Integer.parseInt(dijelovi[1]);
            String sigurnosniKod = dijelovi[2];
            
            Partner partnerZaBrisanje = null;
            for (Partner p : partneri) {
                if (p.id() == id) {
                    partnerZaBrisanje = p;
                    break;
                }
            }
            
            if (partnerZaBrisanje == null) {
                out.write("ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
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
            System.out.println("Greška pri obradi komande OBRIŠI: " + e.getMessage());
            out.write("ERROR 29 - Došlo je do greške pri obradi komande\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za ispis popisa partnera
     */
    private void obradiKomanduPopis(PrintWriter out) {
        try {
            List<PartnerPopis> popisPartnera = new ArrayList<>();
            
            for (Partner p : partneri) {
                popisPartnera.add(new PartnerPopis(p.id(), p.naziv(), p.vrstaKuhinje(), p.adresa(), p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina()));
            }
            
            String jsonPopis = gson.toJson(popisPartnera);
            
            out.write("OK\n");
            out.write(jsonPopis + "\n");
            out.flush();
            
        } catch (Exception e) {
            System.out.println("Greška pri obradi komande POPIS: " + e.getMessage());
            out.write("ERROR 29 - Došlo je do greške pri obradi komande\n");
            out.flush();
        }
    }
    
    /**
     * Sprema podatke o partnerima u datoteku
     */
    private void spremiPartnere() {
        try {
            String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
            FileWriter writer = new FileWriter(datotekaPartnera);
            gson.toJson(partneri, writer);
            writer.close();
        } catch (IOException e) {
            System.out.println("Greška pri spremanju partnera: " + e.getMessage());
        }
    }
    
    /**
     * Pokreće poslužitelj za rad s partnerima
     */
    public void pokreniPosluziteljRad() {
        var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
        var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
        
        try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
            while (!this.kraj.get()) {
                try {
                    Socket mreznaUticnica = ss.accept();
                    this.executor.submit(() -> {
                        // Dodajemo trenutnu dretvu u listu aktivnih
                        aktivneDretve.add(Thread.currentThread());
                        try {
                            return obradiRad(mreznaUticnica);
                        } finally {
                            // Uklanjamo dretvu iz liste aktivnih kad završi
                            aktivneDretve.remove(Thread.currentThread());
                        }
                    });
                } catch (IOException e) {
                    if (!this.kraj.get()) {
                        System.out.println("Greška u poslužitelju za rad s partnerima: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Greška pri pokretanju poslužitelja za rad s partnerima: " + e.getMessage());
        }
    }
    
    
    private Boolean obradiRad(Socket mreznaUticnica) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
            
            String linija = in.readLine();
            
            if (linija == null) {
                out.write("ERROR 39 - Nešto drugo nije u redu\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            // Obrada komande JELOVNIK
            if (linija.startsWith("JELOVNIK ")) {
                obradiKomanduJelovnik(linija, out);
            }
            // Obrada komande KARTAPIĆA
            else if (linija.startsWith("KARTAPIĆA ")) {
                obradiKomanduKartaPica(linija, out);
            }
            // Obrada komande OBRAČUN
            else if (linija.startsWith("OBRAČUN ")) {
                obradiKomanduObracun(linija, in, out);
            }
            // Nepoznata komanda
            else {
                out.write("ERROR 30 - Format komande nije ispravan\n");
                out.flush();
            }
            
            zatvoriVezu(mreznaUticnica);
        } catch (Exception e) {
            System.out.println("Greška pri obradi zahtjeva za rad s partnerima: " + e.getMessage());
            zatvoriVezu(mreznaUticnica);
            return Boolean.FALSE;
        }
        
        return Boolean.TRUE;
    }
    
    /**
     * Obrađuje komandu za dohvat jelovnika
     */
    private void obradiKomanduJelovnik(String linija, PrintWriter out) {
        try {
            // Format: JELOVNIK id sigurnosniKod
            // Npr: JELOVNIK 1 4958583733
            String[] dijelovi = linija.trim().split(" ");
            
            if (dijelovi.length != 3) {
                out.write("ERROR 30 - Format komande nije ispravan\n");
                out.flush();
                return;
            }
            
            int id = Integer.parseInt(dijelovi[1]);
            String sigurnosniKod = dijelovi[2];
            
            Partner partner = null;
            for (Partner p : partneri) {
                if (p.id() == id) {
                    partner = p;
                    break;
                }
            }
            
            if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
                out.write("ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
                out.flush();
                return;
            }
            
            // Filtriranje jelovnika prema vrsti kuhinje partnera
            List<Jelovnik> jelovnikPartnera = new ArrayList<>();
            String vrstaKuhinje = partner.vrstaKuhinje();
            
            for (Jelovnik j : jelovnici) {
                if (j.id().startsWith(vrstaKuhinje)) {
                    jelovnikPartnera.add(j);
                }
            }
            
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
            System.out.println("Greška pri obradi komande JELOVNIK: " + e.getMessage());
            out.write("ERROR 39 - Došlo je do greške pri obradi komande\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za dohvat karte pića
     */
    private void obradiKomanduKartaPica(String linija, PrintWriter out) {
        try {
            // Format: KARTAPIĆA id sigurnosniKod
            // Npr: KARTAPIĆA 1 4958583733
            String[] dijelovi = linija.trim().split(" ");
            
            if (dijelovi.length != 3) {
                out.write("ERROR 30 - Format komande nije ispravan\n");
                out.flush();
                return;
            }
            
            int id = Integer.parseInt(dijelovi[1]);
            String sigurnosniKod = dijelovi[2];
            
            Partner partner = null;
            for (Partner p : partneri) {
                if (p.id() == id) {
                    partner = p;
                    break;
                }
            }
            
            if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
                out.write("ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
                out.flush();
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
            System.out.println("Greška pri obradi komande KARTAPIĆA: " + e.getMessage());
            out.write("ERROR 39 - Greška pri obradi komande\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za obračun
     */
    private synchronized void obradiKomanduObracun(String linija, BufferedReader in, PrintWriter out) {
        try {
            String[] dijelovi = linija.trim().split(" ");

            if (dijelovi.length != 3) {
                out.write("ERROR 30 - Format komande nije ispravan\n");
                out.flush();
                return;
            }

            int id = Integer.parseInt(dijelovi[1]);
            String sigurnosniKod = dijelovi[2];

            Partner partner = null;
            for (Partner p : partneri) {
                if (p.id() == id) {
                    partner = p;
                    break;
                }
            }

            if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
                out.write("ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
                out.flush();
                return;
            }

            StringBuilder jsonObracun = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.trim().endsWith("]")) {
                jsonObracun.append(line).append("\n");
            }

            if (line != null) {
                jsonObracun.append(line);
            }

            try {
                List<Obracun> noviObracuni = gson.fromJson(jsonObracun.toString(), new TypeToken<List<Obracun>>(){}.getType());
                
                if (noviObracuni == null || noviObracuni.isEmpty()) {
                    out.write("ERROR 35 - Neispravan obračun: podaci su prazni\n");
                    out.flush();
                    return;
                }
                
                for (Obracun o : noviObracuni) {
                    if (o.partner() != id) {
                        out.write("ERROR 35 - Neispravan obračun: ID partnera ne odgovara\n");
                        out.flush();
                        return;
                    }
                    
                    String itemId = o.id();
                    boolean isJelo = o.jelo();
                    boolean validId = false;
                    
                    if (isJelo) {
                        String vrstaKuhinje = partner.vrstaKuhinje();
                        for (Jelovnik j : jelovnici) {
                            if (j.id().equals(itemId) && j.id().startsWith(vrstaKuhinje)) {
                                validId = true;
                                break;
                            }
                        }
                    } else {
                        for (KartaPica p : kartaPica) {
                            if (p.id().equals(itemId)) {
                                validId = true;
                                break;
                            }
                        }
                    }
                    
                    if (!validId) {
                        out.write("ERROR 35 - Neispravan obračun: nepostojeći ID jela/pića\n");
                        out.flush();
                        return;
                    }
                    
                    if (o.kolicina() < 1) {
                        out.write("ERROR 35 - Neispravan obračun: količina mora biti barem 1\n");
                        out.flush();
                        return;
                    }
                }

                try {
                    String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
                    BufferedReader reader = new BufferedReader(new FileReader(datotekaObracuna));
                    List<Obracun> postojeciObracuni = gson.fromJson(reader, new TypeToken<List<Obracun>>(){}.getType());
                    reader.close();

                    if (postojeciObracuni != null) {
                        obracuni = postojeciObracuni;
                    }
                } catch (IOException e) {
                    System.out.println("Nije moguće učitati postojeće obračune: " + e.getMessage());
                }

                if (noviObracuni != null) {
                    obracuni.addAll(noviObracuni);
                }

                String datotekaObracuna = this.konfig.dajPostavku("datotekaObracuna");
                FileWriter writer = new FileWriter(datotekaObracuna);
                gson.toJson(obracuni, writer);
                writer.close();

                out.write("OK\n");
                out.flush();

            } catch (Exception e) {
                System.out.println("Greška pri obradi JSON obračuna: " + e.getMessage());
                out.write("ERROR 35 - Neispravan obračun: pogreška pri parsiranju JSON-a\n");
                out.flush();
            }

        } catch (Exception e) {
            System.out.println("Greška pri obradi komande OBRAČUN: " + e.getMessage());
            out.write("ERROR 39 - Nešto drugo nije u redu\n");
            out.flush();
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
     * @param mreznaUticnica veza koju treba zatvoriti
     */
    private void zatvoriVezu(Socket mreznaUticnica) {
        if (mreznaUticnica != null && !mreznaUticnica.isClosed()) {
            try {
                mreznaUticnica.close();
                brojZatvorenihVeza.incrementAndGet();
            } catch (IOException e) {
                System.out.println("Greška pri zatvaranju veze: " + e.getMessage());
            }
        }
    }
}