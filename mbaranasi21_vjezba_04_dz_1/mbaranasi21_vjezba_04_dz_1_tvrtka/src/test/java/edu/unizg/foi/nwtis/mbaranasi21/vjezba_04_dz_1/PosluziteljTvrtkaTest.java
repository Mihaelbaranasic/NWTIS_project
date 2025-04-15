package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1.PosluziteljTvrtka;

class PosluziteljTvrtkaTest {

private PosluziteljTvrtka posluziteljTvrtka;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}
	
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}
	
	@BeforeEach
	void setUp() throws Exception {
		posluziteljTvrtka = new PosluziteljTvrtka();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		posluziteljTvrtka = null;
	}
	
	@Test
	@Order(6)
	void testMain() {
	}
	
	@Test
	@Order(5)
	void testPripremiKreni() {
	}
	
	@Test
	@Order(4)
	void testPokreniPosluziteljKraj() {
	}
	
	@Test
	@Order(3)
	void testObradiKraj() {
	}
	
	@Test
	@Order(2)
	void testPokreniPosluziteljRegistracija() {
	}
	
	@Test
	@Order(7)
	void testPokreniPosluziteljRad() {
	}
	
	@Test
	@Order(1)
	void testUcitajKonfiguraciju() {
		try {
		      String nazivDatoteke = this.getClass().getName() + ".txt";
		      Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);

		      konfig.spremiPostavku(this.getClass().getName(), "1");
		      konfig.spremiPostavku("2", this.getClass().getName());
		      konfig.spremiPostavku("3", "4");
		      konfig.spremiPostavku(this.getClass().getName(), this.getClass().getName());

		      konfig.spremiKonfiguraciju();

		      // assertFalse(this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke + ".pero"),
		      assertTrue(this.posluziteljTvrtka.ucitajKonfiguraciju(nazivDatoteke),
		          "Problem kod učitavanja datoteke.");

		      var props1 = konfig.dajSvePostavke();
		      var props2 = this.posluziteljTvrtka.konfig.dajSvePostavke();

		      var kljucevi1 = props1.keySet().stream().sorted().toArray();
		      var kljucevi2 = props2.keySet().stream().sorted().toArray();

		      assertArrayEquals(kljucevi1, kljucevi2, "Ključevi nisu isti");

		      var vrijednosti1 = props1.values().stream().sorted().toArray();
		      var vrijednosti2 = props2.values().stream().sorted().toArray();

		      assertArrayEquals(vrijednosti1, vrijednosti2, "Vrijednosti nisu iste");

		      for (var p : props1.keySet()) {
		        if (!props2.get(p).equals(props1.get(p))) {
		          fail("Nema sve postavke.");
		        }
		      }

		      Files.delete(Path.of(nazivDatoteke));
		    } catch (NeispravnaKonfiguracija | IOException e) {
		      fail(e.getMessage());
		    }
		  }

}
