package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

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
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Jelovnik;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.KartaPica;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Narudzba;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Obracun;

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

	public static void main(String[] args) {
		if (args.length > 2) {
			System.out.println("Broj argumenata veći od 2.");
			return;
		}
		var program = new PosluziteljPartner();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        System.out.println("Program se prekida (Ctrl+C). Zatvaranje resursa...");
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
	        
	        System.out.println("Ukupno zatvoreno veza: " + program.brojZatvorenihVeza.get());
	        System.out.println("Ukupno prekinuto dretvi: " + program.brojPrekinutihDretvi.get());
	    }));
		var nazivDatoteke = args[0];
		if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
			return;
		}
		if (args.length == 1) {
			program.registrirajPartnera();
			return;
		}
		var linija = args[1];

		var poklapanjeKraj = program.predlozakKraj.matcher(linija);
		var statusKraj = poklapanjeKraj.matches();
		if (statusKraj) {
			program.posaljiKraj();
			return;
		}

		var poklapanjePartner = program.predlozakPartner.matcher(linija);
		var statusPartner = poklapanjePartner.matches();
		if (statusPartner) {
			program.pokreniPosluzitelj();
			return;
		}

		System.out.println("Nevažeća opcija: " + linija);
	}

	private void pokreniPosluzitelj() {
	    if (!this.konfig.postojiPostavka("sigKod")) {
	        System.out.println("Partner nije registriran. Prvo registrirajte partnera.");
	        return;
	    }

	    if (!preuzmiJelovnik() || !preuzmiKartuPica()) {
	        System.out.println("Nije moguće preuzeti jelovnik ili kartu pića. Prekidam rad.");
	        return;
	    }

	    var builder = Thread.ofVirtual();
	    var factory = builder.factory();
	    this.executor = Executors.newThreadPerTaskExecutor(factory);

	    if (this.konfig.postojiPostavka("kvotaNarudzbi")) {
	        this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
	    }

	    try {
	        int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
	        int brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
	        int pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

	        try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
	            System.out.println("Poslužitelj partner pokrenut na portu " + mreznaVrata);

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
	                        System.out.println("Greška pri prihvaćanju veze: " + e.getMessage());
	                    }
	                }

	                try {
	                    Thread.sleep(pauzaDretve);
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                }
	            }
	        }

	    } catch (IOException e) {
	        System.out.println("Greška pri pokretanju poslužitelja: " + e.getMessage());
	    } finally {
	        if (this.executor != null) {
	            this.executor.shutdown();
	        }
	    }
	}

	private void obradiZahtjevKupca(Socket socket) {
	    try {
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));
	        
	        String komanda = in.readLine();
	        if (komanda == null) {
	            zatvoriVezu(socket);
	            return;
	        }
	        
	        String[] dijelovi = komanda.split(" ", 2);
	        String nazivKomande = dijelovi[0];
	        
	        if (!nazivKomande.equals(nazivKomande.toUpperCase())) {
	            out.write("ERROR 40 - Format komande nije ispravan (komanda mora biti velikim slovima)\n");
	            out.flush();
	            zatvoriVezu(socket);
	            return;
	        }
	        
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
	            out.write("ERROR 49 - Nepoznata komanda: " + komanda + "\n");
	            out.flush();
	        }
	        
	        zatvoriVezu(socket);
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi zahtjeva kupca: " + e.getMessage());
	        zatvoriVezu(socket);
	    }
	}

	private void posaljiKraj() {
	    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
	    var adresa = this.konfig.dajPostavku("adresa");
	    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
	    try {
	        var mreznaUticnica = new Socket(adresa, mreznaVrata);
	        BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
	        out.write("KRAJ " + kodZaKraj + "\n");
	        out.flush();
	        mreznaUticnica.shutdownOutput();
	        var linija = in.readLine();
	        mreznaUticnica.shutdownInput();
	        if (linija.equals("OK")) {
	            System.out.println("Uspješan kraj poslužitelja.");
	        }
	        zatvoriVezu(mreznaUticnica);
	    } catch (IOException e) {
	        System.out.println("Greška pri slanju zahtjeva za kraj: " + e.getMessage());
	    }
	}

	private void registrirajPartnera() {
	    if (this.konfig.postojiPostavka("sigKod") && !this.konfig.dajPostavku("sigKod").isEmpty()) {
	        System.out.println("Partner je već registriran. Sigurnosni kod: " + this.konfig.dajPostavku("sigKod"));
	        return;
	    }
	    
	    try {
	        var adresa = this.konfig.dajPostavku("adresa");
	        var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
	        var mreznaUticnica = new Socket(adresa, mreznaVrata);

	        BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

	        int id = Integer.parseInt(this.konfig.dajPostavku("id"));
	        String naziv = this.konfig.dajPostavku("naziv");
	        String kuhinja = this.konfig.dajPostavku("kuhinja");
	        String partnerAdresa = this.konfig.dajPostavku("adresa");
	        int partnerMreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
	        float gpsSirina = Float.parseFloat(this.konfig.dajPostavku("gpsSirina"));
	        float gpsDuzina = Float.parseFloat(this.konfig.dajPostavku("gpsDuzina"));

	        String komanda = String.format("PARTNER %d \"%s\" %s %s %d %.5f %.5f\n", id, naziv, kuhinja,
	                partnerAdresa, partnerMreznaVrata, gpsSirina, gpsDuzina);

	        out.write(komanda);
	        out.flush();
	        mreznaUticnica.shutdownOutput();

	        String odgovor = in.readLine();
	        mreznaUticnica.shutdownInput();

	        if (odgovor != null && odgovor.startsWith("OK")) {
	            String[] dijelovi = odgovor.split(" ");
	            if (dijelovi.length >= 2) {
	                String sigKod = dijelovi[1];

	                this.konfig.spremiPostavku("sigKod", sigKod);
	                this.konfig.spremiKonfiguraciju();

	                System.out.println("Partner uspješno registriran. Sigurnosni kod: " + sigKod);
	            }
	        } else {
	            System.out.println("Greška pri registraciji partnera: " + odgovor);
	        }

	        zatvoriVezu(mreznaUticnica);

	    } catch (Exception e) {
	        System.out.println("Greška pri registraciji partnera: " + e.getMessage());
	    }
	}

	private boolean preuzmiJelovnik() {
	    Socket socket = null;
	    try {
	        String adresa = this.konfig.dajPostavku("adresa");
	        int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
	        int id = Integer.parseInt(this.konfig.dajPostavku("id"));
	        String sigKod = this.konfig.dajPostavku("sigKod");

	        socket = new Socket(adresa, mreznaVrataRad);
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

	        String komanda = "JELOVNIK " + id + " " + sigKod + "\n";
	        out.write(komanda);
	        out.flush();

	        String odgovorStatus = in.readLine();
	        if (odgovorStatus != null && odgovorStatus.equals("OK")) {
	            StringBuilder jsonBuilder = new StringBuilder();
	            String red;
	            while ((red = in.readLine()) != null) {
	                jsonBuilder.append(red);
	            }

	            String json = jsonBuilder.toString();
	            this.jelovnici = gson.fromJson(json, new TypeToken<List<Jelovnik>>() {
	            }.getType());

	            System.out.println("Jelovnik uspješno preuzet. Broj jela: " + this.jelovnici.size());
	            zatvoriVezu(socket);
	            return true;
	        } else {
	            System.out.println("Greška pri dohvatu jelovnika: " + odgovorStatus);
	            zatvoriVezu(socket);
	            return false;
	        }

	    } catch (Exception e) {
	        System.out.println("Greška pri preuzimanju jelovnika: " + e.getMessage());
	        zatvoriVezu(socket);
	        return false;
	    }
	}

	private boolean preuzmiKartuPica() {
	    Socket socket = null;
	    try {
	        String adresa = this.konfig.dajPostavku("adresa");
	        int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
	        int id = Integer.parseInt(this.konfig.dajPostavku("id"));
	        String sigKod = this.konfig.dajPostavku("sigKod");

	        socket = new Socket(adresa, mreznaVrataRad);
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

	        String komanda = "KARTAPIĆA " + id + " " + sigKod + "\n";
	        out.write(komanda);
	        out.flush();

	        String odgovorStatus = in.readLine();
	        if (odgovorStatus != null && odgovorStatus.equals("OK")) {
	            StringBuilder jsonBuilder = new StringBuilder();
	            String red;
	            while ((red = in.readLine()) != null) {
	                jsonBuilder.append(red);
	            }

	            String json = jsonBuilder.toString();
	            this.kartaPica = gson.fromJson(json, new TypeToken<List<KartaPica>>() {
	            }.getType());

	            System.out.println("Karta pića uspješno preuzeta. Broj pića: " + this.kartaPica.size());
	            zatvoriVezu(socket);
	            return true;
	        } else {
	            System.out.println("Greška pri dohvatu karte pića: " + odgovorStatus);
	            zatvoriVezu(socket);
	            return false;
	        }

	    } catch (Exception e) {
	        System.out.println("Greška pri preuzimanju karte pića: " + e.getMessage());
	        zatvoriVezu(socket);
	        return false;
	    }
	}

	private void obradiKomanduJelovnik(String komanda, PrintWriter out) {
	    try {
	        String[] dijelovi = komanda.trim().split(" ");
	        if (dijelovi.length != 2) {
	            out.write("ERROR 40 - Format komande nije ispravan\n");
	            out.flush();
	            return;
	        }

	        String korisnik = dijelovi[1];

	        String jsonJelovnik = gson.toJson(this.jelovnici);

	        out.write("OK\n");
	        out.write(jsonJelovnik + "\n");
	        out.flush();

	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande JELOVNIK: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

	private void obradiKomanduKartaPica(String komanda, PrintWriter out) {
	    try {
	        String[] dijelovi = komanda.trim().split(" ");
	        if (dijelovi.length != 2) {
	            out.write("ERROR 40 - Format komande nije ispravan\n");
	            out.flush();
	            return;
	        }

	        String korisnik = dijelovi[1];

	        String jsonKartaPica = gson.toJson(this.kartaPica);

	        out.write("OK\n");
	        out.write(jsonKartaPica + "\n");
	        out.flush();

	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande KARTAPIĆA: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

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
	        System.out.println("Greška pri obradi komande NARUDŽBA: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

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

	        if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n");
	            out.flush();
	            return;
	        }

	        Jelovnik jelo = null;
	        for (Jelovnik j : jelovnici) {
	            if (j.id().equals(idJela)) {
	                jelo = j;
	                break;
	            }
	        }

	        if (jelo == null) {
	            out.write("ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n");
	            out.flush();
	            return;
	        }

	        int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
	        Narudzba stavka = new Narudzba(korisnik, idJela, true, kolicina, jelo.cijena(),
	                System.currentTimeMillis() / 1000);
	        otvoreneNarudzbe.get(korisnik).add(stavka);

	        out.write("OK\n");
	        out.flush();

	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande JELO: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

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

	        if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n");
	            out.flush();
	            return;
	        }

	        KartaPica pice = null;
	        for (KartaPica p : kartaPica) {
	            if (p.id().equals(idPica)) {
	                pice = p;
	                break;
	            }
	        }

	        if (pice == null) {
	            out.write("ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera\n");
	            out.flush();
	            return;
	        }

	        int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
	        Narudzba stavka = new Narudzba(korisnik, idPica, false, kolicina, pice.cijena(),
	                System.currentTimeMillis() / 1000);
	        otvoreneNarudzbe.get(korisnik).add(stavka);

	        out.write("OK\n");
	        out.flush();

	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande PIĆE: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

	private synchronized void obradiKomanduRacun(String komanda, PrintWriter out) {
	    try {
	        String[] dijelovi = komanda.trim().split(" ");
	        if (dijelovi.length != 2) {
	            out.write("ERROR 40 - Format komande nije ispravan\n");
	            out.flush();
	            return;
	        }
	        
	        String korisnik = dijelovi[1];
	        
	        if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
	            out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n");
	            out.flush();
	            return;
	        }
	        
	        List<Narudzba> narudzba = otvoreneNarudzbe.get(korisnik);
	        
	        if (!placeneNarudzbe.containsKey(korisnik)) {
	            placeneNarudzbe.put(korisnik, new ArrayList<>());
	        }
	        
	        placeneNarudzbe.get(korisnik).addAll(narudzba);
	        
	        otvoreneNarudzbe.remove(korisnik);
	        
	        brojNaplacenihNarudzbi++;
	        
	        System.out.println("Naplaćena narudžba #" + brojNaplacenihNarudzbi + " za korisnika " + korisnik + 
	                           " sa " + narudzba.size() + " stavki. Kvota: " + this.kvotaNarudzbi);
	        
	        if (brojNaplacenihNarudzbi % this.kvotaNarudzbi == 0) {
	            List<Obracun> obracuni = new ArrayList<>();
	            
	            Map<String, Float> kolicinePoID = new HashMap<>();
	            Map<String, Float> cijenePoID = new HashMap<>();
	            Map<String, Boolean> jeLoJelo = new HashMap<>();
	            
	            for (List<Narudzba> narudzbe : placeneNarudzbe.values()) {
	                for (Narudzba n : narudzbe) {
	                    String id = n.id();
	                    boolean jelo = n.jelo();
	                    String kljuc = id + (jelo ? "_jelo" : "_pice");
	                    
	                    kolicinePoID.put(kljuc, kolicinePoID.getOrDefault(kljuc, 0f) + n.kolicina());
	                    
	                    cijenePoID.put(kljuc, n.cijena());
	                    
	                    jeLoJelo.put(kljuc, jelo);
	                }
	            }
	            
	            int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
	            for (Map.Entry<String, Float> entry : kolicinePoID.entrySet()) {
	                String kljuc = entry.getKey();
	                String[] dijeloviKljuca = kljuc.split("_");
	                String id = dijeloviKljuca[0];
	                boolean jelo = jeLoJelo.get(kljuc);
	                float kolicina = entry.getValue();
	                float cijena = cijenePoID.get(kljuc);
	                
	                Obracun o = new Obracun(idPartnera, id, jelo, kolicina, cijena, System.currentTimeMillis() / 1000);
	                obracuni.add(o);
	            }
	            
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
	        } else {
	            out.write("OK\n");
	            out.flush();
	        }
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande RAČUN: " + e.getMessage());
	        out.write("ERROR 49 - Nešto drugo nije u redu\n");
	        out.flush();
	    }
	}

	private boolean posaljiObracun(List<Obracun> obracuni) {
	    try {
	        String jsonObracun = gson.toJson(obracuni);

	        String adresa = this.konfig.dajPostavku("adresa");
	        int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
	        int id = Integer.parseInt(this.konfig.dajPostavku("id"));
	        String sigKod = this.konfig.dajPostavku("sigKod");

	        Socket socket = new Socket(adresa, mreznaVrataRad);
	        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

	        String komanda = "OBRAČUN " + id + " " + sigKod + "\n";
	        out.write(komanda);
	        out.write(jsonObracun + "\n");
	        out.flush();

	        String odgovor = in.readLine();
	        if (odgovor != null && odgovor.equals("OK")) {
	            socket.close();
	            return true;
	        } else {
	            System.out.println("Greška pri slanju obračuna: " + odgovor);
	            socket.close();
	            return false;
	        }

	    } catch (Exception e) {
	        System.out.println("Greška pri slanju obračuna: " + e.getMessage());
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
