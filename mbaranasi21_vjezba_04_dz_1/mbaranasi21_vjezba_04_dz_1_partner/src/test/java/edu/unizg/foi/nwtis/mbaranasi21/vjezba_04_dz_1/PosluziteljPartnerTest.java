package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Jelovnik;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.KartaPica;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Narudzba;



class PosluziteljPartnerTest {

  private PosluziteljPartner posluziteljPartner;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    posluziteljPartner = new PosluziteljPartner();
  }

  @AfterEach
  void tearDown() throws Exception {
    posluziteljPartner = null;
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

      Method method =
          PosluziteljPartner.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
      method.setAccessible(true);
      boolean result = (boolean) method.invoke(posluziteljPartner, nazivDatoteke);

      assertTrue(result, "Problem kod učitavanja datoteke.");

      Field field = PosluziteljPartner.class.getDeclaredField("konfig");
      field.setAccessible(true);
      Konfiguracija ucitanaKonfig = (Konfiguracija) field.get(posluziteljPartner);

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
  void testPronadiJelo() throws Exception {
    List<Jelovnik> jelovnici = new ArrayList<>();
    Jelovnik testJelo = new Jelovnik("ITA_1", "Pizza Margherita", 8.0f);
    jelovnici.add(testJelo);
    jelovnici.add(new Jelovnik("ITA_2", "Pasta Carbonara", 7.5f));

    Field field = PosluziteljPartner.class.getDeclaredField("jelovnici");
    field.setAccessible(true);
    field.set(posluziteljPartner, jelovnici);

    Method method = PosluziteljPartner.class.getDeclaredMethod("pronadiJelo", String.class);
    method.setAccessible(true);

    Jelovnik pronadjenoJelo = (Jelovnik) method.invoke(posluziteljPartner, "ITA_1");
    assertNotNull(pronadjenoJelo);
    assertEquals(testJelo.id(), pronadjenoJelo.id());
    assertEquals(testJelo.naziv(), pronadjenoJelo.naziv());

    Jelovnik nepostojeceJelo = (Jelovnik) method.invoke(posluziteljPartner, "ITA_999");
    assertNull(nepostojeceJelo);
  }

  @Test
  @Order(3)
  void testPronadiPice() throws Exception {
    List<KartaPica> kartaPica = new ArrayList<>();
    KartaPica testPice = new KartaPica("P_1", "Coca-Cola", 2.0f, 2.0f);
    kartaPica.add(testPice);
    kartaPica.add(new KartaPica("P_2", "Voda", 1.5f, 5.0f));

    Field field = PosluziteljPartner.class.getDeclaredField("kartaPica");
    field.setAccessible(true);
    field.set(posluziteljPartner, kartaPica);

    Method method = PosluziteljPartner.class.getDeclaredMethod("pronadiPice", String.class);
    method.setAccessible(true);

    KartaPica pronadjenoPice = (KartaPica) method.invoke(posluziteljPartner, "P_1");
    assertNotNull(pronadjenoPice);
    assertEquals(testPice.id(), pronadjenoPice.id());
    assertEquals(testPice.naziv(), pronadjenoPice.naziv());

    KartaPica nepostojecePice = (KartaPica) method.invoke(posluziteljPartner, "P_999");
    assertNull(nepostojecePice);
  }

  @Test
  @Order(4)
  void testProvejiPostojiNarudzba() throws Exception {
    Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
    List<Narudzba> narudzbe = new ArrayList<>();
    narudzbe.add(new Narudzba("korisnik1", "ITA_1", true, 1.0f, 8.0f, System.currentTimeMillis()));
    otvoreneNarudzbe.put("korisnik1", narudzbe);

    Field field = PosluziteljPartner.class.getDeclaredField("otvoreneNarudzbe");
    field.setAccessible(true);
    field.set(posluziteljPartner, otvoreneNarudzbe);

    StringWriter stringWriter = new StringWriter();
    PrintWriter out = new PrintWriter(stringWriter);

    Method method = PosluziteljPartner.class.getDeclaredMethod("provjeriPostojiNarudzba",
        String.class, PrintWriter.class);
    method.setAccessible(true);

    boolean postojiNarudzba = (boolean) method.invoke(posluziteljPartner, "korisnik1", out);
    assertTrue(postojiNarudzba);

    boolean nePostojiNarudzba =
        (boolean) method.invoke(posluziteljPartner, "nePostojeciKorisnik", out);
    assertFalse(nePostojiNarudzba);
    assertTrue(stringWriter.toString().contains("Ne postoji otvorena narudžba za korisnika/kupca"));
  }

  @Test
  @Order(5)
  void testDodajStavkuNarudzbe() throws Exception {
    Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
    otvoreneNarudzbe.put("korisnik1", new ArrayList<>());

    Field field = PosluziteljPartner.class.getDeclaredField("otvoreneNarudzbe");
    field.setAccessible(true);
    field.set(posluziteljPartner, otvoreneNarudzbe);

    String nazivDatoteke = this.getClass().getName() + "_id.txt";
    Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
    konfig.spremiPostavku("id", "1");
    konfig.spremiKonfiguraciju();

    Method ucitajMethod =
        PosluziteljPartner.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
    ucitajMethod.setAccessible(true);
    ucitajMethod.invoke(posluziteljPartner, nazivDatoteke);

    Method method = PosluziteljPartner.class.getDeclaredMethod("dodajStavkuNarudzbe", String.class,
        String.class, boolean.class, float.class, float.class);
    method.setAccessible(true);

    method.invoke(posluziteljPartner, "korisnik1", "ITA_1", true, 1.0f, 8.0f);

    List<Narudzba> narudzbe = otvoreneNarudzbe.get("korisnik1");
    assertEquals(1, narudzbe.size());

    Narudzba dodanaStavka = narudzbe.get(0);
    assertEquals("korisnik1", dodanaStavka.korisnik());
    assertEquals("ITA_1", dodanaStavka.id());
    assertTrue(dodanaStavka.jelo());
    assertEquals(1.0f, dodanaStavka.kolicina());
    assertEquals(8.0f, dodanaStavka.cijena());

    Files.delete(Path.of(nazivDatoteke));
  }

  @Test
  @Order(6)
  void testAzurirajPlaceneNarudzbe() throws Exception {
    Map<String, List<Narudzba>> placeneNarudzbe = new ConcurrentHashMap<>();

    Field field = PosluziteljPartner.class.getDeclaredField("placeneNarudzbe");
    field.setAccessible(true);
    field.set(posluziteljPartner, placeneNarudzbe);

    List<Narudzba> narudzba = new ArrayList<>();
    narudzba.add(new Narudzba("korisnik1", "ITA_1", true, 1.0f, 8.0f, System.currentTimeMillis()));

    Method method = PosluziteljPartner.class.getDeclaredMethod("azurirajPlaceneNarudzbe",
        String.class, List.class);
    method.setAccessible(true);

    method.invoke(posluziteljPartner, "korisnik1", narudzba);

    assertTrue(placeneNarudzbe.containsKey("korisnik1"));
    assertEquals(1, placeneNarudzbe.get("korisnik1").size());

    List<Narudzba> dodatnaNarudzba = new ArrayList<>();
    dodatnaNarudzba
        .add(new Narudzba("korisnik1", "P_1", false, 2.0f, 4.0f, System.currentTimeMillis()));

    method.invoke(posluziteljPartner, "korisnik1", dodatnaNarudzba);

    assertEquals(2, placeneNarudzbe.get("korisnik1").size());
  }

  @Test
  @Order(7)
  void testKreirajObracun() throws Exception {
    Map<String, List<Narudzba>> placeneNarudzbe = new ConcurrentHashMap<>();
    List<Narudzba> narudzbe1 = new ArrayList<>();
    narudzbe1.add(new Narudzba("korisnik1", "ITA_1", true, 1.0f, 8.0f, System.currentTimeMillis()));
    narudzbe1.add(new Narudzba("korisnik1", "P_1", false, 2.0f, 4.0f, System.currentTimeMillis()));

    List<Narudzba> narudzbe2 = new ArrayList<>();
    narudzbe2.add(new Narudzba("korisnik2", "ITA_1", true, 3.0f, 8.0f, System.currentTimeMillis()));

    placeneNarudzbe.put("korisnik1", narudzbe1);
    placeneNarudzbe.put("korisnik2", narudzbe2);

    Field field = PosluziteljPartner.class.getDeclaredField("placeneNarudzbe");
    field.setAccessible(true);
    field.set(posluziteljPartner, placeneNarudzbe);

    String nazivDatoteke = this.getClass().getName() + "_id.txt";
    Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
    konfig.spremiPostavku("id", "1");
    konfig.spremiKonfiguraciju();

    Method ucitajMethod =
        PosluziteljPartner.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
    ucitajMethod.setAccessible(true);
    ucitajMethod.invoke(posluziteljPartner, nazivDatoteke);

    Method method = PosluziteljPartner.class.getDeclaredMethod("kreirajObracun");
    method.setAccessible(true);

    List<?> obracuni = (List<?>) method.invoke(posluziteljPartner);

    assertEquals(2, obracuni.size(), "Broj stavki u obračunu nije ispravan");

    float ukupnoJelo = 0.0f;
    float ukupnoPice = 0.0f;

    for (Object o : obracuni) {
      Method mId = o.getClass().getMethod("id");
      Method mJelo = o.getClass().getMethod("jelo");
      Method mKolicina = o.getClass().getMethod("kolicina");

      String id = (String) mId.invoke(o);
      boolean jelo = (boolean) mJelo.invoke(o);
      float kolicina = (float) mKolicina.invoke(o);

      if (id.equals("ITA_1") && jelo) {
        ukupnoJelo += kolicina;
      } else if (id.equals("P_1") && !jelo) {
        ukupnoPice += kolicina;
      }
    }

    assertEquals(4.0f, ukupnoJelo, 0.001f, "Ukupna količina jela ITA_1 nije ispravna");
    assertEquals(2.0f, ukupnoPice, 0.001f, "Ukupna količina pića P_1 nije ispravna");

    Files.delete(Path.of(nazivDatoteke));
  }

  @Test
  @Order(8)
  void testProvejiKomandu() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter out = new PrintWriter(stringWriter);

    Method method = PosluziteljPartner.class.getDeclaredMethod("provjeriKomandu", String.class,
        PrintWriter.class);
    method.setAccessible(true);

    boolean rezultatPrazno = (boolean) method.invoke(posluziteljPartner, "", out);
    assertFalse(rezultatPrazno);
    assertTrue(stringWriter.toString().contains("ERROR 40"));

    stringWriter = new StringWriter();
    out = new PrintWriter(stringWriter);

    boolean rezultatIspravno =
        (boolean) method.invoke(posluziteljPartner, "JELOVNIK korisnik1", out);
    assertTrue(rezultatIspravno);

    rezultatIspravno = (boolean) method.invoke(posluziteljPartner, "jelovnik korisnik1", out);
    assertFalse(rezultatIspravno);

    stringWriter = new StringWriter();
    out = new PrintWriter(stringWriter);
    boolean rezultatBezArgumenata = (boolean) method.invoke(posluziteljPartner, "JELOVNIK", out);
    assertFalse(rezultatBezArgumenata);
    assertTrue(stringWriter.toString().contains("ERROR 40"));
  }

  @Test
  @Order(9)
  void testZatvoriVezu() throws Exception {}

  @Test
  @Order(10)
  void testKreirajKomanduZaRegistraciju() throws Exception {
    String nazivDatoteke = this.getClass().getName() + "_reg.txt";
    Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
    konfig.spremiPostavku("id", "1");
    konfig.spremiPostavku("naziv", "Test Restoran");
    konfig.spremiPostavku("kuhinja", "ITA");
    konfig.spremiPostavku("adresa", "localhost");
    konfig.spremiPostavku("mreznaVrata", "12345");
    konfig.spremiPostavku("gpsSirina", "45.815399");
    konfig.spremiPostavku("gpsDuzina", "15.966568");
    konfig.spremiKonfiguraciju();

    Method ucitajMethod =
        PosluziteljPartner.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
    ucitajMethod.setAccessible(true);
    ucitajMethod.invoke(posluziteljPartner, nazivDatoteke);

    Method method = PosluziteljPartner.class.getDeclaredMethod("kreirajKomanduZaRegistraciju");
    method.setAccessible(true);

    String komanda = (String) method.invoke(posluziteljPartner);

    assertTrue(komanda.startsWith("PARTNER 1 \"Test Restoran\" ITA localhost 12345"),
        "Komanda ne započinje s očekivanim formatom: " + komanda);

    assertTrue(komanda.contains("45."), "Komanda ne sadrži GPS širinu: " + komanda);
    assertTrue(komanda.contains("15."), "Komanda ne sadrži GPS dužinu: " + komanda);

    Files.delete(Path.of(nazivDatoteke));
  }
}
