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

}
