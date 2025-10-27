package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

class KorisnikKupacTest {

  private KorisnikKupac korisnikKupac;
  private static String testDataFile;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    testDataFile = "test_podaci.csv";
    FileWriter writer = new FileWriter(testDataFile);
    writer.write("korisnik1;localhost;12345;100;JELOVNIK korisnik1\n");
    writer.write("korisnik2;localhost;12345;200;KARTAPIĆA korisnik2\n");
    writer.close();
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception {
    Files.deleteIfExists(Path.of(testDataFile));
  }

  @BeforeEach
  void setUp() throws Exception {
    korisnikKupac = new KorisnikKupac();
  }

  @AfterEach
  void tearDown() throws Exception {
    korisnikKupac = null;
  }

  @Test
  @Order(1)
  void testUcitajKonfiguraciju() throws Exception {
    try {
      String nazivDatoteke = this.getClass().getName() + ".txt";
      Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
      konfig.spremiPostavku(this.getClass().getName(), "1");
      konfig.spremiPostavku("2", this.getClass().getName());
      konfig.spremiPostavku("3", "4");
      konfig.spremiPostavku(this.getClass().getName(), this.getClass().getName());
      konfig.spremiKonfiguraciju();

      Method method = KorisnikKupac.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
      method.setAccessible(true);
      boolean result = (boolean) method.invoke(korisnikKupac, nazivDatoteke);

      assertTrue(result, "Problem kod učitavanja datoteke.");

      Field field = KorisnikKupac.class.getDeclaredField("konfig");
      field.setAccessible(true);
      Konfiguracija ucitanaKonfig = (Konfiguracija) field.get(korisnikKupac);

      var props1 = konfig.dajSvePostavke();
      var props2 = ucitanaKonfig.dajSvePostavke();
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

  @Test
  @Order(2)
  void testMain() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    KorisnikKupac.main(new String[] {"config.txt"});

    assertEquals(
        "Broj argumenata nije 2. Očekivano: datoteka_konfiguracije.txt datoteka_podataka.csv",
        outContent.toString().trim());

    System.setOut(originalOut);
  }

  @Test
  @Order(3)
  void testObradiLiniju() throws Exception {
    String linija = "korisnik1;localhost;12345;100;JELOVNIK korisnik1";

    Method method = KorisnikKupac.class.getDeclaredMethod("obradiLiniju", String.class);
    method.setAccessible(true);

    method.invoke(korisnikKupac, linija);

    String neispravnaLinija = "korisnik1;localhost;12345";
    method.invoke(korisnikKupac, neispravnaLinija);
  }

  @Test
  @Order(4)
  void testObradiDatotekuPodataka() throws Exception {
    String nazivDatoteke = this.getClass().getName() + ".txt";
    Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
    konfig.spremiKonfiguraciju();

    Method ucitajMethod =
        KorisnikKupac.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
    ucitajMethod.setAccessible(true);
    ucitajMethod.invoke(korisnikKupac, nazivDatoteke);

    Method method = KorisnikKupac.class.getDeclaredMethod("obradiDatotekuPodataka", String.class);
    method.setAccessible(true);

    method.invoke(korisnikKupac, testDataFile);

    method.invoke(korisnikKupac, "nepostojeca_datoteka.csv");

    Files.delete(Path.of(nazivDatoteke));
  }

  @Test
  @Order(5)
  void testPokreni() throws Exception {
    String nazivDatoteke = this.getClass().getName() + ".txt";
    Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
    konfig.spremiKonfiguraciju();

    Method method = KorisnikKupac.class.getDeclaredMethod("pokreni", String.class, String.class);
    method.setAccessible(true);

    method.invoke(korisnikKupac, nazivDatoteke, testDataFile);

    method.invoke(korisnikKupac, "nepostojeca_konfiguracija.txt", testDataFile);

    Files.delete(Path.of(nazivDatoteke));
  }

}
