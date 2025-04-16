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

public class PosluziteljTvrtka {
    /** Konfiguracijski podaci */
    protected Konfiguracija konfig;
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
        
        String[] obaveznePostavke = {
            "datotekaPartnera", "mreznaVrataKraj", "mreznaVrataRegistracija", 
            "mreznaVrataRad", "kodZaKraj", "datotekaKartaPica", "datotekaObracuna"
        };
        
        for (String postavka : obaveznePostavke) {
            if (!this.konfig.postojiPostavka(postavka)) {
                System.out.println("Nedostaje obavezna postavka: " + postavka);
                return;
            }
        }
        
        if (!this.konfig.dajPostavku("datotekaPartnera").endsWith(".json")) {
            return;
        }
        
        this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
        this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
        
        ucitajPodatke();
        
        var builder = Thread.ofVirtual();
        var factory = builder.factory();
        this.executor = Executors.newThreadPerTaskExecutor(factory);
        
        Future<?> dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
        Future<?> dretvaZaRegistraciju = this.executor.submit(() -> this.pokreniPosluziteljRegistracija());
        Future<?> dretvaZaRad = this.executor.submit(() -> this.pokreniPosluziteljRad());
        
        while (!this.kraj.get()) {
            try {
                Thread.sleep(this.pauzaDretve);
                
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
        
    }

    /**
     * Učitava podatke iz datoteka: partnere, jelovnike i kartu pića
     */
    private void ucitajPodatke() {
        try {
            String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
            BufferedReader reader = new BufferedReader(new FileReader(datotekaPartnera));
            this.partneri = gson.fromJson(reader, new TypeToken<List<Partner>>(){}.getType());
            reader.close();
            
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
                        }
                    }
                }
            }
            
            String datotekaKartaPica = this.konfig.dajPostavku("datotekaKartaPica");
            reader = new BufferedReader(new FileReader(datotekaKartaPica));
            this.kartaPica = gson.fromJson(reader, new TypeToken<List<KartaPica>>(){}.getType());
            reader.close();
            
            List<Partner> validniPartneri = new ArrayList<>();
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
            
            for (Partner p : partneri) {
                if (postojeceKuhinje.contains(p.vrstaKuhinje())) {
                    validniPartneri.add(p);
                } else {
                }
            }
            
            partneri = validniPartneri;
            
        } catch (IOException e) {
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
            ss.close();
        } catch (IOException e) {
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
            
            if (!linija.startsWith("KRAJ ")) {
                out.write("ERROR 10 - Format komande nije ispravan (nedostaje razmak nakon KRAJ)\n");
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
            
            if (linija.trim().isEmpty()) {
                out.write("ERROR 20 - Format komande nije ispravan (prazna komanda)\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            String komanda = linija.split(" ", 2)[0];
            
            if (komanda.equals("PARTNER") || komanda.equals("OBRIŠI")) {
                if (!linija.startsWith(komanda + " ")) {
                    out.write("ERROR 20 - Format komande nije ispravan (nedostaje razmak nakon naziva komande)\n");
                    out.flush();
                    zatvoriVezu(mreznaUticnica);
                    return Boolean.FALSE;
                }
            }
            
            if (linija.startsWith("PARTNER ")) {
                obradiKomanduPartner(linija, out);
            }
            else if (linija.startsWith("OBRIŠI ")) {
                obradiKomanduObrisi(linija, out);
            }
            else if (linija.trim().equals("POPIS")) {
                obradiKomanduPopis(out);
            }
            else {
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
     * Obrađuje komandu za registraciju partnera
     */
    private void obradiKomanduPartner(String linija, PrintWriter out) {
        try {

            int pocetakNaziva = linija.indexOf("\"");
            int krajNaziva = linija.indexOf("\"", pocetakNaziva + 1);

            if (pocetakNaziva == -1 || krajNaziva == -1) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }

            String naziv = linija.substring(pocetakNaziva + 1, krajNaziva);

            String ostatakLinije = linija.substring(krajNaziva + 1).trim();
            String[] parametri = ostatakLinije.split(" ");

            if (parametri.length != 5) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }

            String vrstaKuhinje = parametri[0];
            String adresa = parametri[1];
            int mreznaVrata = Integer.parseInt(parametri[2]);
            float gpsSirina = Float.parseFloat(parametri[3]);
            float gpsDuzina = Float.parseFloat(parametri[4]);

            String[] prviDio = linija.substring(0, pocetakNaziva).trim().split(" ");
            if (prviDio.length != 2) {
                out.write("ERROR 20 - Format komande nije ispravan\n");
                out.flush();
                return;
            }
            int id = Integer.parseInt(prviDio[1]);

            for (Partner p : partneri) {
                if (p.id() == id) {
                    out.write("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
                    out.flush();
                    return;
                }
            }

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

            String podatakZaKod = naziv + adresa;
            int hash = podatakZaKod.hashCode();
            String sigurnosniKod = Integer.toHexString(hash);

            Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina, sigurnosniKod);
            partneri.add(noviPartner);

            spremiPartnere();

            out.write("OK " + sigurnosniKod + "\n");
            out.flush();

        } catch (Exception e) {
            out.write("ERROR 29 - Nešto drugo nije u redu\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za brisanje partnera
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
            OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(datotekaPartnera), StandardCharsets.UTF_8);
            gson.toJson(partneri, writer);
            writer.close();
        } catch (IOException e) {
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
            
            if (linija.trim().isEmpty()) {
                out.write("ERROR 30 - Format komande nije ispravan (prazna komanda)\n");
                out.flush();
                zatvoriVezu(mreznaUticnica);
                return Boolean.FALSE;
            }
            
            String komanda = linija.split(" ", 2)[0];
            
            if (komanda.equals("JELOVNIK") || komanda.equals("KARTAPIĆA") || komanda.equals("OBRAČUN")) {
                if (!linija.startsWith(komanda + " ")) {
                    out.write("ERROR 30 - Format komande nije ispravan (nedostaje razmak nakon naziva komande)\n");
                    out.flush();
                    zatvoriVezu(mreznaUticnica);
                    return Boolean.FALSE;
                }
            }
            
            if (linija.startsWith("JELOVNIK ")) {
                obradiKomanduJelovnik(linija, out);
            }
            else if (linija.startsWith("KARTAPIĆA ")) {
                obradiKomanduKartaPica(linija, out);
            }
            else if (linija.startsWith("OBRAČUN ")) {
                obradiKomanduObracun(linija, in, out);
            }
            else {
                out.write("ERROR 30 - Format komande nije ispravan\n");
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
     * Obrađuje komandu za dohvat jelovnika
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
            out.write("ERROR 39 - Došlo je do greške pri obradi komande\n");
            out.flush();
        }
    }
    
    /**
     * Obrađuje komandu za dohvat karte pića
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
                
                if (noviObracuni == null) {
                    out.write("ERROR 35 - Neispravan obračun: neispravan JSON format\n");
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
                    
                    if (o.kolicina() < 0) {
                        out.write("ERROR 35 - Neispravan obračun: količina ne može biti negativna\n");
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
                out.write("ERROR 35 - Neispravan obračun: pogreška pri parsiranju JSON-a\n");
                out.flush();
            }

        } catch (Exception e) {
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
            }
        }
    }
}