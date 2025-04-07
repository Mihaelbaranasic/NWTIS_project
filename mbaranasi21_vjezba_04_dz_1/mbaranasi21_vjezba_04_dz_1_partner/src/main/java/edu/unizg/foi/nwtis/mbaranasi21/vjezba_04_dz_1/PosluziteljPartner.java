package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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
	/** Mapa otvorenih narudžbi po korisnicima */
	private Map<String, List<Narudzba>> otvoreneNarudzbe = new HashMap<>();
	/** Mapa plaćenih narudžbi po korisnicima */
	private Map<String, List<Narudzba>> placeneNarudzbe = new HashMap<>();
	/** Broj naplaćenih narudžbi */
	private int brojNaplacenihNarudzbi = 0;
	/** Zastavica za kraj rada */
	private volatile boolean kraj = false;
	/** Izvršitelj dretve */
	private ExecutorService executor;

	public static void main(String[] args) {
		if (args.length > 2) {
			System.out.println("Broj argumenata veći od 2.");
			return;
		}

		var program = new PosluziteljPartner();
		var nazivDatoteke = args[0];

		if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
			return;
		}

		if (args.length == 1) {
			return;
		}
		var linija = args[1];

		var poklapanje = program.predlozakKraj.matcher(linija);
		var status = poklapanje.matches();
		if (status) {
			program.posaljiKraj();
			return;
		} else {
			return;
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
			mreznaUticnica.close();
		} catch (IOException e) {
			System.out.println("Greška pri slanju zahtjeva za kraj: " + e.getMessage());
		}
	}

	private void registrirajPartnera() {
		try {
			var adresa = this.konfig.dajPostavku("adresa");
			var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
			var mreznaUticnica = new Socket(adresa, mreznaVrata);

			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

			int id = Integer.parseInt(this.konfig.dajPostavku("id"));
			String naziv = this.konfig.dajPostavku("naziv");
			String vrstaKuhinje = this.konfig.dajPostavku("vrstaKuhinje");
			String partnerAdresa = this.konfig.dajPostavku("adresa");
			int partnerMreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
			float gpsSirina = Float.parseFloat(this.konfig.dajPostavku("gpsSirina"));
			float gpsDuzina = Float.parseFloat(this.konfig.dajPostavku("gpsDuzina"));

			String komanda = String.format("PARTNER %d \"%s\" %s %s %d %.5f %.5f\n", id, naziv, vrstaKuhinje,
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

			mreznaUticnica.close();

		} catch (Exception e) {
			System.out.println("Greška pri registraciji partnera: " + e.getMessage());
		}
	}

	private boolean preuzmiJelovnik() {
		try {
			String adresa = this.konfig.dajPostavku("adresa");
			int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
			int id = Integer.parseInt(this.konfig.dajPostavku("id"));
			String sigKod = this.konfig.dajPostavku("sigKod");

			Socket socket = new Socket(adresa, mreznaVrataRad);
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
				socket.close();
				return true;
			} else {
				System.out.println("Greška pri dohvatu jelovnika: " + odgovorStatus);
				socket.close();
				return false;
			}

		} catch (Exception e) {
			System.out.println("Greška pri preuzimanju jelovnika: " + e.getMessage());
			return false;
		}
	}

	private boolean preuzmiKartuPica() {
		try {
			String adresa = this.konfig.dajPostavku("adresa");
			int mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
			int id = Integer.parseInt(this.konfig.dajPostavku("id"));
			String sigKod = this.konfig.dajPostavku("sigKod");

			Socket socket = new Socket(adresa, mreznaVrataRad);
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
				socket.close();
				return true;
			} else {
				System.out.println("Greška pri dohvatu karte pića: " + odgovorStatus);
				socket.close();
				return false;
			}

		} catch (Exception e) {
			System.out.println("Greška pri preuzimanju karte pića: " + e.getMessage());
			return false;
		}
	}

	private void obradiKomanduJelovnik(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 2) {
				out.write("ERROR 40\n");
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
			out.write("ERROR 49\n");
			out.flush();
		}
	}

	private void obradiKomanduKartaPica(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 2) {
				out.write("ERROR 40\n");
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
			out.write("ERROR 49\n");
			out.flush();
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
}
