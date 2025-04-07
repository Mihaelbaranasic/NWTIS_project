package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Jelovnik;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.KartaPica;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Partner;

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

	/** Mapa kuhinja */
	private Map<String, String> kuhinje = new ConcurrentHashMap<>();
	/** Mapa jelovnika po kuhinji */
	private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();
	/** Mapa karte pića */
	private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();
	/** Mapa partnera */
	private Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

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
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
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

	private boolean ucitajKartuPica() {
		var nazivDatotekePica = this.konfig.dajPostavku("datotekaKartaPica");
		var datoteka = Path.of(nazivDatotekePica);
		return true;
	}
	
	private void ucitajPodatke() {
	    try {
	        // Učitaj kuhinje iz konfiguracije
	        for (int i = 1; i <= 9; i++) {
	            String kljucKuhinje = "kuhinja_" + i;
	            if (this.konfig.postojiPostavka(kljucKuhinje)) {
	                String vrijednostKuhinje = this.konfig.dajPostavku(kljucKuhinje);
	                String[] dijelovi = vrijednostKuhinje.split(";");
	                if (dijelovi.length >= 2) {
	                    String oznaka = dijelovi[0];
	                    String naziv = dijelovi[1];
	                    this.kuhinje.put(oznaka, naziv);
	                }
	            }
	        }
	        
	        // Učitaj partnere
	        String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
	        BufferedReader reader = new BufferedReader(new FileReader(datotekaPartnera));
	        List<Partner> partneriLista = gson.fromJson(reader, new TypeToken<List<Partner>>(){}.getType());
	        reader.close();
	        
	        if (partneriLista != null) {
	            for (Partner p : partneriLista) {
	                this.partneri.put(p.id(), p);
	            }
	        }
	        
	        // Učitaj jelovnike za svaku kuhinju
	        for (String vrstaKuhinje : this.kuhinje.keySet()) {
	            String datotekaJelovnika = "kuhinja_" + vrstaKuhinje + ".json";
	            try {
	                reader = new BufferedReader(new FileReader(datotekaJelovnika));
	                List<Jelovnik> jelovniciLista = gson.fromJson(reader, new TypeToken<List<Jelovnik>>(){}.getType());
	                reader.close();
	                
	                Map<String, Jelovnik> jelovnikMapa = new ConcurrentHashMap<>();
	                if (jelovniciLista != null) {
	                    for (Jelovnik j : jelovniciLista) {
	                        jelovnikMapa.put(j.id(), j);
	                    }
	                }
	                this.jelovnici.put(vrstaKuhinje, jelovnikMapa);
	            } catch (IOException e) {
	                System.out.println("Nije moguće učitati jelovnik za kuhinju: " + vrstaKuhinje);
	            }
	        }
	        
	        // Učitaj kartu pića
	        String datotekaKartaPica = this.konfig.dajPostavku("datotekaKartaPica");
	        reader = new BufferedReader(new FileReader(datotekaKartaPica));
	        List<KartaPica> kartaPicaLista = gson.fromJson(reader, new TypeToken<List<KartaPica>>(){}.getType());
	        reader.close();
	        
	        if (kartaPicaLista != null) {
	            for (KartaPica kp : kartaPicaLista) {
	                this.kartaPica.put(kp.id(), kp);
	            }
	        }
	        
	        System.out.println("Podaci uspješno učitani.");
	    } catch (IOException e) {
	        System.out.println("Greška pri učitavanju podataka: " + e.getMessage());
	    }
	}
	
	private void obradiKomanduPartner(String linija, PrintWriter out) {
	    try {
	        // Format: PARTNER id "Naziv partnera" vrstaKuhinje adresa mreznaVrata gpsSirina gpsDuzina
	        // Npr: PARTNER 1 "Roštilj Pero" MK localhost 8010 46.29950 16.33001
	        
	        // Izvlačenje naziva partnera između navodnika
	        int pocetakNaziva = linija.indexOf("\"");
	        int krajNaziva = linija.indexOf("\"", pocetakNaziva + 1);
	        
	        if (pocetakNaziva == -1 || krajNaziva == -1) {
	            out.write("ERROR 20\n");
	            out.flush();
	            return;
	        }
	        
	        String naziv = linija.substring(pocetakNaziva + 1, krajNaziva);
	        
	        // Preostali dio linije nakon zatvaranja navodnika
	        String ostatakLinije = linija.substring(krajNaziva + 1).trim();
	        String[] parametri = ostatakLinije.split(" ");
	        
	        if (parametri.length != 5) {
	            out.write("ERROR 20\n");
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
	            out.write("ERROR 20\n");
	            out.flush();
	            return;
	        }
	        int id = Integer.parseInt(prviDio[1]);
	        
	        // Provjera postoji li već partner s istim ID-om
	        if (partneri.containsKey(id)) {
	            out.write("ERROR 21\n");
	            out.flush();
	            return;
	        }
	        
	        // Generiranje sigurnosnog koda
	        String podatakZaKod = naziv + adresa;
	        int hash = podatakZaKod.hashCode();
	        String sigurnosniKod = Integer.toHexString(hash);
	        
	        // Kreiranje novog partnera
	        Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina, sigurnosniKod);
	        partneri.put(id, noviPartner);
	        
	        // Spremanje u datoteku
	        spremiPartnere();
	        
	        // Slanje odgovora
	        out.write("OK " + sigurnosniKod + "\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande PARTNER: " + e.getMessage());
	        out.write("ERROR 29\n");
	        out.flush();
	    }
	}
	
	private void obradiKomanduObrisi(String linija, PrintWriter out) {
	    try {
	        // Format: OBRIŠI id sigurnosniKod
	        // Npr: OBRIŠI 1 4958583733
	        String[] dijelovi = linija.trim().split(" ");
	        
	        if (dijelovi.length != 3) {
	            out.write("ERROR 20\n");
	            out.flush();
	            return;
	        }
	        
	        int id = Integer.parseInt(dijelovi[1]);
	        String sigurnosniKod = dijelovi[2];
	        
	        Partner partner = partneri.get(id);
	        
	        if (partner == null) {
	            out.write("ERROR 23\n");
	            out.flush();
	            return;
	        }
	        
	        if (!partner.sigurnosniKod().equals(sigurnosniKod)) {
	            out.write("ERROR 22\n");
	            out.flush();
	            return;
	        }
	        
	        partneri.remove(id);
	        spremiPartnere();
	        
	        out.write("OK\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande OBRIŠI: " + e.getMessage());
	        out.write("ERROR 29\n");
	        out.flush();
	    }
	}
	
	private void obradiKomanduPopis(PrintWriter out) {
	    try {
	        List<PartnerPopis> popisPartnera = new ArrayList<>();
	        
	        for (Partner p : partneri.values()) {
	            popisPartnera.add(new PartnerPopis(p.id(), p.naziv(), p.vrstaKuhinje(), p.adresa(), p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina()));
	        }
	        
	        String jsonPopis = gson.toJson(popisPartnera);
	        
	        out.write("OK\n");
	        out.write(jsonPopis + "\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande POPIS: " + e.getMessage());
	        out.write("ERROR 29\n");
	        out.flush();
	    }
	}

	private void spremiPartnere() {
	    try {
	        String datotekaPartnera = this.konfig.dajPostavku("datotekaPartnera");
	        FileWriter writer = new FileWriter(datotekaPartnera);
	        List<Partner> partneriLista = new ArrayList<>(partneri.values());
	        gson.toJson(partneriLista, writer);
	        writer.close();
	    } catch (IOException e) {
	        System.out.println("Greška pri spremanju partnera: " + e.getMessage());
	    }
	}
	
	private void obradiKomanduJelovnik(String linija, PrintWriter out) {
	    try {
	        // Format: JELOVNIK id sigurnosniKod
	        // Npr: JELOVNIK 1 4958583733
	        String[] dijelovi = linija.trim().split(" ");
	        
	        if (dijelovi.length != 3) {
	            out.write("ERROR 30\n");
	            out.flush();
	            return;
	        }
	        
	        int id = Integer.parseInt(dijelovi[1]);
	        String sigurnosniKod = dijelovi[2];
	        
	        Partner partner = partneri.get(id);
	        
	        if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
	            out.write("ERROR 31\n");
	            out.flush();
	            return;
	        }
	        
	        // Dohvat jelovnika za vrstu kuhinje partnera
	        String vrstaKuhinje = partner.vrstaKuhinje();
	        Map<String, Jelovnik> jelovnikKuhinje = jelovnici.get(vrstaKuhinje);
	        
	        if (jelovnikKuhinje == null || jelovnikKuhinje.isEmpty()) {
	            out.write("ERROR 32\n");
	            out.flush();
	            return;
	        }
	        
	        String jsonJelovnik = gson.toJson(new ArrayList<>(jelovnikKuhinje.values()));
	        
	        out.write("OK\n");
	        out.write(jsonJelovnik + "\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande JELOVNIK: " + e.getMessage());
	        out.write("ERROR 39\n");
	        out.flush();
	    }
	}
	
	private void obradiKomanduKartaPica(String linija, PrintWriter out) {
	    try {
	        // Format: KARTAPIĆA id sigurnosniKod
	        // Npr: KARTAPIĆA 1 4958583733
	        String[] dijelovi = linija.trim().split(" ");
	        
	        if (dijelovi.length != 3) {
	            out.write("ERROR 30\n");
	            out.flush();
	            return;
	        }
	        
	        int id = Integer.parseInt(dijelovi[1]);
	        String sigurnosniKod = dijelovi[2];
	        
	        Partner partner = partneri.get(id);
	        
	        if (partner == null || !partner.sigurnosniKod().equals(sigurnosniKod)) {
	            out.write("ERROR 31\n");
	            out.flush();
	            return;
	        }
	        
	        if (kartaPica.isEmpty()) {
	            out.write("ERROR 34\n");
	            out.flush();
	            return;
	        }
	        
	        String jsonKartaPica = gson.toJson(new ArrayList<>(kartaPica.values()));
	        
	        out.write("OK\n");
	        out.write(jsonKartaPica + "\n");
	        out.flush();
	        
	    } catch (Exception e) {
	        System.out.println("Greška pri obradi komande KARTAPIĆA: " + e.getMessage());
	        out.write("ERROR 39\n");
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
}
