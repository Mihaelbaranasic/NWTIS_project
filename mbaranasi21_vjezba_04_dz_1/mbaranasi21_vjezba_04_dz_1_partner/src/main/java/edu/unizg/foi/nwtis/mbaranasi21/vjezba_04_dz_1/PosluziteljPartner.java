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
