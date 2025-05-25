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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
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


// TODO: Auto-generated Javadoc
/**
 * Poslužitelj partnera koji obrađuje narudžbe kupaca i upravlja kontrolnim komandama.
 */
public class PosluziteljPartner {

	/** Konfiguracijski podaci. */
	private Konfiguracija konfig;
	
	/** Predložak za kraj. */
	private Pattern predlozakKraj = Pattern.compile("^KRAJ$");
	
	/** Predložak za partner. */
	private Pattern predlozakPartner = Pattern.compile("^PARTNER$");
	
	/** Gson objekt za rad s JSON-om. */
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	/** Kolekcija jelovnika. */
	private List<Jelovnik> jelovnici = new ArrayList<>();
	
	/** Kolekcija karte pića. */
	private List<KartaPica> kartaPica = new ArrayList<>();
	
	/** Broj naplaćenih narudžbi. */
	private int brojNaplacenihNarudzbi = 0;
	
	/** Zastavica za kraj rada. */
	private volatile boolean kraj = false;
	
	/** Izvršitelj dretve. */
	private ExecutorService executor;
	
	/** Brojač prekinutih dretvi. */
	private AtomicInteger brojPrekinutihDretvi = new AtomicInteger(0);
	
	/** Brojač zatvorenih veza. */
	private AtomicInteger brojZatvorenihVeza = new AtomicInteger(0);
	
	/** Lista aktivnih dretvi. */
	private List<Thread> aktivneDretve = Collections.synchronizedList(new ArrayList<>());
	
	/** Mapa otvorenih narudžbi po korisnicima. */
	private Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
	
	/** Mapa plaćenih narudžbi po korisnicima. */
	private Map<String, List<Narudzba>> placeneNarudzbe = new ConcurrentHashMap<>();

	/** The kvota narudzbi. */
	private int kvotaNarudzbi = 10;
	
	/** The kod za kraj. */
	private String kodZaKraj = "";
	
	/** The kod za admin partnera. */
	private String kodZaAdminPartnera = "";
	
	/** The pauza dretve. */
	private int pauzaDretve = 1000;

	/** The pauza kupci. */
	private AtomicBoolean pauzaKupci = new AtomicBoolean(false);

	/**
	 * The main method.
	 *
	 * @param args the arguments
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
	 * Dodaj shutdown hook.
	 *
	 * @param program the program
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
	 * Obradi argument.
	 *
	 * @param linija the linija
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
	 * Pokreni posluzitelj.
	 */
	private void pokreniPosluzitelj() {
		if (!this.konfig.postojiPostavka("sigKod")) {
			return;
		}

		if (!preuzmiJelovnik() || !preuzmiKartuPica()) {
			return;
		}

		inicijalizirajPosluzitelj();
		pokreniPosluzitelje();
	}

	/**
	 * Inicijaliziraj posluzitelj.
	 */
	private void inicijalizirajPosluzitelj() {
		var builder = Thread.ofVirtual();
		var factory = builder.factory();
		this.executor = Executors.newThreadPerTaskExecutor(factory);

		this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
		this.kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdminPartnera");
		this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

		if (this.konfig.postojiPostavka("kvotaNarudzbi")) {
			this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
		}
	}

	/**
	 * Pokreni posluzitelje.
	 */
	private void pokreniPosluzitelje() {
		Future<?> dretvaZaKraj = this.executor.submit(() -> this.pokreniPosluziteljKraj());
		Future<?> dretvaZaKupce = this.executor.submit(() -> this.pokreniPosluziteljKupce());

		cekajNaKraj(dretvaZaKupce);
	}

	/**
	 * Cekaj na kraj.
	 *
	 * @param dretvaZaKupce the dretva za kupce
	 */
	private void cekajNaKraj(Future<?> dretvaZaKupce) {
		while (!this.kraj) {
			try {
				Thread.sleep(this.pauzaDretve);

				if (this.kraj) {
					prekiniDretve(dretvaZaKupce);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Prekini dretve.
	 *
	 * @param dretvaZaKupce the dretva za kupce
	 */
	private void prekiniDretve(Future<?> dretvaZaKupce) {
		if (!dretvaZaKupce.isDone()) {
			dretvaZaKupce.cancel(true);
		}
	}
	/**
	 * Pokreće poslužitelj za kraj rada.
	 */
	public void pokreniPosluziteljKraj() {
		var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
		var brojCekaca = 0;
		try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
			while (!this.kraj) {
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
					if (!this.kraj) {
					}
				}
			}
		} catch (IOException e) {
		}
	}
	/**
	 * Obrađuje zahtjeve za kraj i kontrolne komande.
	 * 
	 * @param mreznaUticnica mrežna utičnica za komunikaciju
	 * @return Boolean.TRUE ako je obrada uspješna, inače Boolean.FALSE
	 */
	public Boolean obradiKraj(Socket mreznaUticnica) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

			String linija = in.readLine();
			mreznaUticnica.shutdownInput();

			if (linija.startsWith("KRAJ ")) {
				return obradiKomanduKraj(linija, out, mreznaUticnica);
			} else if (linija.startsWith("STATUS ")) {
				return obradiKomanduStatus(linija, out, mreznaUticnica);
			} else if (linija.startsWith("PAUZA ")) {
				return obradiKomanduPauza(linija, out, mreznaUticnica);
			} else if (linija.startsWith("START ")) {
				return obradiKomanduStart(linija, out, mreznaUticnica);
			} else if (linija.startsWith("SPAVA ")) {
				return obradiKomanduSpava(linija, out, mreznaUticnica);
			} else if (linija.startsWith("OSVJEŽI ")) {
				return obradiKomanduOsvjezi(linija, out, mreznaUticnica);
			} else {
				out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
				out.flush();
				zatvoriVezu(mreznaUticnica);
				return Boolean.FALSE;
			}

		} catch (Exception e) {
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}
	}

	/**
	 * Obrađuje komandu KRAJ.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return the boolean
	 */
	private Boolean obradiKomanduKraj(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 2 || !dijelovi[0].equals("KRAJ") || !dijelovi[1].equals(this.kodZaKraj)) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		out.write("OK\n");
		out.flush();
		this.kraj = true;

		zatvoriVezu(mreznaUticnica);
		return Boolean.TRUE;
	}

	/**
	 * Obrađuje komandu STATUS.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return boolean
	 */
	private Boolean obradiKomanduStatus(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 3) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (!dijelovi[1].equals(this.kodZaAdminPartnera)) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		int tipPosluzitelja = Integer.parseInt(dijelovi[2]);
		if (tipPosluzitelja != 1) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		int status = pauzaKupci.get() ? 0 : 1;

		out.write("OK " + status + "\n");
		out.flush();
		zatvoriVezu(mreznaUticnica);
		return Boolean.TRUE;
	}

	/**
	 * Obradi komandu pauza.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return the boolean
	 */
	private Boolean obradiKomanduPauza(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 3) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (!dijelovi[1].equals(this.kodZaAdminPartnera)) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		int tipPosluzitelja = Integer.parseInt(dijelovi[2]);
		if (tipPosluzitelja != 1) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (pauzaKupci.get()) {
			out.write("ERROR 62 - Pogrešna promjena pauze ili starta\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		pauzaKupci.set(true);

		out.write("OK\n");
		out.flush();
		zatvoriVezu(mreznaUticnica);
		return Boolean.TRUE;
	}

	/**
	 * Obradi komandu start.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return the boolean
	 */
	private Boolean obradiKomanduStart(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 3) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (!dijelovi[1].equals(this.kodZaAdminPartnera)) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		int tipPosluzitelja = Integer.parseInt(dijelovi[2]);
		if (tipPosluzitelja != 1) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (!pauzaKupci.get()) {
			out.write("ERROR 62 - Pogrešna promjena pauze ili starta\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		pauzaKupci.set(false);

		out.write("OK\n");
		out.flush();
		zatvoriVezu(mreznaUticnica);
		return Boolean.TRUE;
	}

	/**
	 * Obradi komandu spava.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return the boolean
	 */
	private Boolean obradiKomanduSpava(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 3) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (!dijelovi[1].equals(this.kodZaAdminPartnera)) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		try {
			int milisekunde = Integer.parseInt(dijelovi[2]);
			Thread.sleep(milisekunde);

			out.write("OK\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.TRUE;

		} catch (InterruptedException e) {
			out.write("ERROR 63 - Prekid spavanja dretve\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		} catch (NumberFormatException e) {
			out.write("ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}
	}

	/**
	 * Obradi komandu osvjezi.
	 *
	 * @param linija the linija
	 * @param out the out
	 * @param mreznaUticnica the mrezna uticnica
	 * @return the boolean
	 */
	private Boolean obradiKomanduOsvjezi(String linija, PrintWriter out, Socket mreznaUticnica) {
		String[] dijelovi = linija.trim().split(" ");

		if (dijelovi.length != 2 || !dijelovi[1].equals(this.kodZaAdminPartnera)) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (pauzaKupci.get()) {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}

		if (preuzmiJelovnik() && preuzmiKartuPica()) {
			out.write("OK\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.TRUE;
		} else {
			out.write("ERROR 61 - Pogrešan kodZaAdminPartnera\n");
			out.flush();
			zatvoriVezu(mreznaUticnica);
			return Boolean.FALSE;
		}
	}

	/**
	 * Pokreni posluzitelj kupce.
	 */
	private void pokreniPosluziteljKupce() {
		int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
		int brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
		int pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

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
	 * Obradi zahtjev kupca.
	 *
	 * @param socket the socket
	 */
	private void obradiZahtjevKupca(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

			String komanda = in.readLine();

			if (pauzaKupci.get()) {
				out.write("ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi\n");
				out.flush();
				zatvoriVezu(socket);
				return;
			}

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
	 * Provjeri komandu.
	 *
	 * @param komanda the komanda
	 * @param out the out
	 * @return true, if successful
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

		if (nazivKomande.equals("JELOVNIK") || nazivKomande.equals("KARTAPIĆA") || nazivKomande.equals("NARUDŽBA")
				|| nazivKomande.equals("JELO") || nazivKomande.equals("PIĆE") || nazivKomande.equals("RAČUN")
				|| nazivKomande.equals("STANJE")) {

			if (nazivKomande.equals("STANJE")) {
				if (dijelovi.length < 2 || dijelovi[1].trim().isEmpty()) {
					out.write("ERROR 40 - Format komande nije ispravan\n");
					out.flush();
					return false;
				}
			} else if (nazivKomande.equals("JELOVNIK") || nazivKomande.equals("KARTAPIĆA")
					|| nazivKomande.equals("NARUDŽBA") || nazivKomande.equals("JELO") || nazivKomande.equals("PIĆE")
					|| nazivKomande.equals("RAČUN")) {
				if (dijelovi.length < 2 || dijelovi[1].trim().isEmpty()) {
					out.write("ERROR 40 - Format komande nije ispravan\n");
					out.flush();
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Zatvori vezu.
	 *
	 * @param out the out
	 */
	private void zatvoriVezu(PrintWriter out) {
		out.write("ERROR 49 - Došlo je do pogreške\n");
		out.flush();
	}

	/**
	 * Obradi komandu.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
		} else if (komanda.startsWith("STANJE ")) {
			obradiKomanduStanje(komanda, out);
		} else {
			out.write("ERROR 49 - Nepostojeća komanda\n");
			out.flush();
		}
	}

	/**
	 * Obradi komandu stanje.
	 *
	 * @param komanda the komanda
	 * @param out the out
	 */
	private synchronized void obradiKomanduStanje(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 2) {
				out.write("ERROR 40 - Format komande nije ispravan\n");
				out.flush();
				return;
			}

			String korisnik = dijelovi[1];

			if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null
					|| otvoreneNarudzbe.get(korisnik).isEmpty()) {
				out.write("ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca\n");
				out.flush();
				return;
			}

			List<Narudzba> narudzba = otvoreneNarudzbe.get(korisnik);
			String jsonNarudzba = gson.toJson(narudzba);

			out.write("OK\n");
			out.write(jsonNarudzba + "\n");
			out.flush();

		} catch (Exception e) {
			out.write("ERROR 49 - Došlo je do pogreške pri obradi komande\n");
			out.flush();
		}
	}

	/**
	 * Posalji kraj.
	 */
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
			}

			zatvoriVezu(mreznaUticnica);
		} catch (IOException e) {
		}
	}

	/**
	 * Registriraj partnera.
	 */
	private void registrirajPartnera() {
		if (this.konfig.postojiPostavka("sigKod") && !this.konfig.dajPostavku("sigKod").isEmpty()) {
			return;
		}

		try {
			Socket mreznaUticnica = uspostaviVezuZaRegistraciju();
			if (mreznaUticnica == null)
				return;

			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

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
	 * Uspostavi vezu za registraciju.
	 *
	 * @return the socket
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
	 * Kreiraj komandu za registraciju.
	 *
	 * @return the string
	 */
	private String kreirajKomanduZaRegistraciju() {
		int id = Integer.parseInt(this.konfig.dajPostavku("id"));
		String naziv = this.konfig.dajPostavku("naziv");
		String kuhinja = this.konfig.dajPostavku("kuhinja");
		String partnerAdresa = this.konfig.dajPostavku("adresa");
		int partnerMreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
		float gpsSirina = Float.parseFloat(this.konfig.dajPostavku("gpsSirina"));
		float gpsDuzina = Float.parseFloat(this.konfig.dajPostavku("gpsDuzina"));
		int mreznaVrataKraj = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
		String adminKod = this.konfig.dajPostavku("kodZaAdminPartnera");

		return String.format("PARTNER %d \"%s\" %s %s %d %.5f %.5f %d %s\n", id, naziv, kuhinja, partnerAdresa,
				partnerMreznaVrata, gpsSirina, gpsDuzina, mreznaVrataKraj, adminKod);
	}

	/**
	 * Obradi odgovor registracije.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
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
	 * Preuzmi jelovnik.
	 *
	 * @return true, if successful
	 */
	private boolean preuzmiJelovnik() {
		Socket socket = null;
		try {
			socket = uspostaviVezuZaRad();
			if (socket == null)
				return false;

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

			String komanda = "JELOVNIK " + this.konfig.dajPostavku("id") + " " + this.konfig.dajPostavku("sigKod")
					+ "\n";
			out.write(komanda);
			out.flush();

			return procitajOdgovorJelovnik(in, socket);

		} catch (Exception e) {
			zatvoriVezu(socket);
			return false;
		}
	}

	/**
	 * Uspostavi vezu za rad.
	 *
	 * @return the socket
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
	 * Procitaj odgovor jelovnik.
	 *
	 * @param in the in
	 * @param socket the socket
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
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
			this.jelovnici = gson.fromJson(json, new TypeToken<List<Jelovnik>>() {
			}.getType());

			zatvoriVezu(socket);
			return true;
		} else {
			zatvoriVezu(socket);
			return false;
		}
	}

	/**
	 * Preuzmi kartu pica.
	 *
	 * @return true, if successful
	 */
	private boolean preuzmiKartuPica() {
		Socket socket = null;
		try {
			socket = uspostaviVezuZaRad();
			if (socket == null)
				return false;

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

			String komanda = "KARTAPIĆA " + this.konfig.dajPostavku("id") + " " + this.konfig.dajPostavku("sigKod")
					+ "\n";
			out.write(komanda);
			out.flush();

			return procitajOdgovorKartaPica(in, socket);

		} catch (Exception e) {
			zatvoriVezu(socket);
			return false;
		}
	}

	/**
	 * Procitaj odgovor karta pica.
	 *
	 * @param in the in
	 * @param socket the socket
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
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
			this.kartaPica = gson.fromJson(json, new TypeToken<List<KartaPica>>() {
			}.getType());

			zatvoriVezu(socket);
			return true;
		} else {
			zatvoriVezu(socket);
			return false;
		}
	}

	/**
	 * Obradi komandu jelovnik.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Obradi komandu karta pica.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Obradi komandu narudzba.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Obradi komandu jelo.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Provjeri postoji narudzba.
	 *
	 * @param korisnik the korisnik
	 * @param out the out
	 * @return true, if successful
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
	 * Pronadi jelo.
	 *
	 * @param idJela the id jela
	 * @return the jelovnik
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
	 * Dodaj stavku narudzbe.
	 *
	 * @param korisnik the korisnik
	 * @param id the id
	 * @param jelo the jelo
	 * @param kolicina the kolicina
	 * @param cijena the cijena
	 */
	private void dodajStavkuNarudzbe(String korisnik, String id, boolean jelo, float kolicina, float cijena) {
		int idPartnera = Integer.parseInt(this.konfig.dajPostavku("id"));
		Narudzba stavka = new Narudzba(korisnik, id, jelo, kolicina, cijena, System.currentTimeMillis());
		otvoreneNarudzbe.get(korisnik).add(stavka);
	}

	/**
	 * Obradi komandu pice.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Pronadi pice.
	 *
	 * @param idPica the id pica
	 * @return the karta pica
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
	 * Obradi komandu racun.
	 *
	 * @param komanda the komanda
	 * @param out the out
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
	 * Azuriraj placene narudzbe.
	 *
	 * @param korisnik the korisnik
	 * @param narudzba the narudzba
	 */
	private void azurirajPlaceneNarudzbe(String korisnik, List<Narudzba> narudzba) {
		if (!placeneNarudzbe.containsKey(korisnik)) {
			placeneNarudzbe.put(korisnik, new ArrayList<>());
		}

		placeneNarudzbe.get(korisnik).addAll(narudzba);
	}

	/**
	 * Posalji obracun tvrtki.
	 *
	 * @param out the out
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
	 * Kreiraj obracun.
	 *
	 * @return the list
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
	 * Posalji obracun.
	 *
	 * @param obracuni the obracuni
	 * @return true, if successful
	 */
	private boolean posaljiObracun(List<Obracun> obracuni) {
		try {
			String jsonObracun = gson.toJson(obracuni);

			Socket socket = uspostaviVezuZaRad();
			if (socket == null)
				return false;

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));

			String komanda = "OBRAČUN " + this.konfig.dajPostavku("id") + " " + this.konfig.dajPostavku("sigKod")
					+ "\n";
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
	 * @param nazivDatoteke the naziv datoteke
	 * @return true, if successful
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
	 * Zatvori vezu.
	 *
	 * @param mreznaUticnica the mrezna uticnica
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