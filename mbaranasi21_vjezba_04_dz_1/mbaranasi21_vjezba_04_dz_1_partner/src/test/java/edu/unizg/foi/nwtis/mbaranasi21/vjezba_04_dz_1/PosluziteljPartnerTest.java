package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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


class PosluziteljPartnerTest {

  private PosluziteljPartner posluziteljPartner;
  
  @BeforeAll
  static void setUpBeforeClass() throws Exception {
  }
  
  @AfterAll
  static void tearDownAfterClass() throws Exception {
  }
  
  @BeforeEach
  void setUp() throws Exception {
      posluziteljPartner = new PosluziteljPartner();
  }
  
  @AfterEach
  void tearDown() throws Exception {
      posluziteljPartner = null;
  }

  @Test
  void testMain() {
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
          
          java.lang.reflect.Method method = PosluziteljPartner.class.getDeclaredMethod("ucitajKonfiguraciju", String.class);
          method.setAccessible(true);
          boolean result = (boolean) method.invoke(posluziteljPartner, nazivDatoteke);
          
          assertTrue(result, "Problem kod učitavanja datoteke.");
          
          java.lang.reflect.Field field = PosluziteljPartner.class.getDeclaredField("konfig");
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
      
      java.lang.reflect.Field field = PosluziteljPartner.class.getDeclaredField("jelovnici");
      field.setAccessible(true);
      field.set(posluziteljPartner, jelovnici);
      
      java.lang.reflect.Method method = PosluziteljPartner.class.getDeclaredMethod("pronadiJelo", String.class);
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
      
      java.lang.reflect.Field field = PosluziteljPartner.class.getDeclaredField("kartaPica");
      field.setAccessible(true);
      field.set(posluziteljPartner, kartaPica);
      
      java.lang.reflect.Method method = PosluziteljPartner.class.getDeclaredMethod("pronadiPice", String.class);
      method.setAccessible(true);
      
      KartaPica pronadjenoPice = (KartaPica) method.invoke(posluziteljPartner, "P_1");
      assertNotNull(pronadjenoPice);
      assertEquals(testPice.id(), pronadjenoPice.id());
      assertEquals(testPice.naziv(), pronadjenoPice.naziv());
      
      KartaPica nepostojecePice = (KartaPica) method.invoke(posluziteljPartner, "P_999");
      assertNull(nepostojecePice);
  }

}
