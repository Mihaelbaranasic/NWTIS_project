package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	private int kvotaNarudzbi = 10;

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

		// Postavite globalnu varijablu umjesto lokalne
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
						this.executor.submit(() -> obradiZahtjevKupca(socket));
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
				socket.close();
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
				out.write("ERROR 49 Nepoznata komanda\n");
				out.flush();
			}

			socket.close();

		} catch (Exception e) {
			System.out.println("Greška pri obradi zahtjeva kupca: " + e.getMessage());
			try {
				socket.close();
			} catch (IOException ex) {
			}
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
			if (this.konfig.postojiPostavka("sigKod") && !this.konfig.dajPostavku("sigKod").isEmpty()) {
		        System.out.println("Partner je već registriran. Sigurnosni kod: " + this.konfig.dajPostavku("sigKod"));
		        return;
		    }
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

			String komanda = String.format("PARTNER %d \"%s\" %s %s %d %.5f %.5f\n", id, naziv, kuhinja, partnerAdresa,
					partnerMreznaVrata, gpsSirina, gpsDuzina);

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

	private void obradiKomanduNarudzba(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 2) {
				out.write("ERROR 40\n");
				out.flush();
				return;
			}

			String korisnik = dijelovi[1];

			if (otvoreneNarudzbe.containsKey(korisnik) && !otvoreneNarudzbe.get(korisnik).isEmpty()) {
				out.write("ERROR 44\n");
				out.flush();
				return;
			}

			otvoreneNarudzbe.put(korisnik, new ArrayList<>());

			out.write("OK\n");
			out.flush();

		} catch (Exception e) {
			System.out.println("Greška pri obradi komande NARUDŽBA: " + e.getMessage());
			out.write("ERROR 49\n");
			out.flush();
		}
	}

	private void obradiKomanduJelo(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 4) {
				out.write("ERROR 40\n");
				out.flush();
				return;
			}

			String korisnik = dijelovi[1];
			String idJela = dijelovi[2];
			float kolicina = Float.parseFloat(dijelovi[3]);

			if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
				out.write("ERROR 43\n");
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
				out.write("ERROR 41\n");
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
			out.write("ERROR 49\n");
			out.flush();
		}
	}

	private void obradiKomanduPice(String komanda, PrintWriter out) {
		try {
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 4) {
				out.write("ERROR 40\n");
				out.flush();
				return;
			}

			String korisnik = dijelovi[1];
			String idPica = dijelovi[2];
			float kolicina = Float.parseFloat(dijelovi[3]);

			if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null) {
				out.write("ERROR 43\n");
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
				out.write("ERROR 42\n");
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
			out.write("ERROR 49\n");
			out.flush();
		}
	}

	private void obradiKomanduRacun(String komanda, PrintWriter out) {
		try {
			// Format: RAČUN korisnik
			String[] dijelovi = komanda.trim().split(" ");
			if (dijelovi.length != 2) {
				out.write("ERROR 40\n");
				out.flush();
				return;
			}

			String korisnik = dijelovi[1];

			// Provjera postoji li otvorena narudžba za korisnika
			if (!otvoreneNarudzbe.containsKey(korisnik) || otvoreneNarudzbe.get(korisnik) == null
					|| otvoreneNarudzbe.get(korisnik).isEmpty()) {
				out.write("ERROR 43\n");
				out.flush();
				return;
			}

			// Prebacivanje iz otvorenih u plaćene narudžbe
			List<Narudzba> narudzba = otvoreneNarudzbe.get(korisnik);

			if (!placeneNarudzbe.containsKey(korisnik)) {
				placeneNarudzbe.put(korisnik, new ArrayList<>());
			}

			placeneNarudzbe.get(korisnik).addAll(narudzba);
			otvoreneNarudzbe.remove(korisnik);

			brojNaplacenihNarudzbi++;

			// Provjera kvote za obračun - korištenje this.kvotaNarudzbi
			if (brojNaplacenihNarudzbi % this.kvotaNarudzbi == 0) {
				// Kod za obračun...
			} else {
				out.write("OK\n");
				out.flush();
			}

		} catch (Exception e) {
			System.out.println("Greška pri obradi komande RAČUN: " + e.getMessage());
			out.write("ERROR 49\n");
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
				System.out.println("Obračun uspješno poslan.");
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
}
