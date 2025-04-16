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
import java.util.Set;
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
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.Partner;

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
        PosluziteljTvrtka.main(new String[]{});
    }
    
    @Test
    @Order(5)
    void testPripremiKreni() {
        posluziteljTvrtka.pripremiKreni("nepostojeca_datoteka.txt");
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
    @Order(8)
    void testPronadiIndekseNaziva() throws Exception {
        
        String linija = "PARTNER 1 \"Restoran ABC\" ITA localhost 12345 45.815399 15.966568";
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("pronadiIndekseNaziva", String.class);
        method.setAccessible(true);
        int[] indeksi = (int[]) method.invoke(posluziteljTvrtka, linija);
        
        assertEquals(10, indeksi[0]); 
        assertEquals(23, indeksi[1]); 
    }
    
    @Test
    @Order(9)
    void testPartnerPostoji() throws Exception {
        List<Partner> partneri = new ArrayList<>();
        partneri.add(new Partner(1, "Test Partner", "ITA", "localhost", 12345, 45.0f, 15.0f, "abcdef"));
        
        Field field = PosluziteljTvrtka.class.getDeclaredField("partneri");
        field.setAccessible(true);
        field.set(posluziteljTvrtka, partneri);
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("partnerPostoji", int.class);
        method.setAccessible(true);
        
        boolean postojiPartner = (boolean) method.invoke(posluziteljTvrtka, 1);
        assertTrue(postojiPartner);
        
        boolean nePostojiPartner = (boolean) method.invoke(posluziteljTvrtka, 999);
        assertFalse(nePostojiPartner);
    }
    
    @Test
    @Order(10)
    void testPronadiPartnera() throws Exception {
        List<Partner> partneri = new ArrayList<>();
        Partner testPartner = new Partner(1, "Test Partner", "ITA", "localhost", 12345, 45.0f, 15.0f, "abcdef");
        partneri.add(testPartner);
        
        Field field = PosluziteljTvrtka.class.getDeclaredField("partneri");
        field.setAccessible(true);
        field.set(posluziteljTvrtka, partneri);
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("pronadiPartnera", int.class);
        method.setAccessible(true);
        
        Partner pronadjenPartner = (Partner) method.invoke(posluziteljTvrtka, 1);
        assertNotNull(pronadjenPartner);
        assertEquals(1, pronadjenPartner.id());
        assertEquals("Test Partner", pronadjenPartner.naziv());
        
        Partner nepostojeciPartner = (Partner) method.invoke(posluziteljTvrtka, 999);
        assertNull(nepostojeciPartner);
    }
    
    @Test
    @Order(11)
    void testDohvatiJelovnikPartnera() throws Exception {
        Partner partner = new Partner(1, "Test Partner", "ITA", "localhost", 12345, 45.0f, 15.0f, "abcdef");
        
        List<Jelovnik> jelovnici = new ArrayList<>();
        jelovnici.add(new Jelovnik("ITA_1", "Pizza Margherita", 8.0f));
        jelovnici.add(new Jelovnik("ITA_2", "Pasta Carbonara", 7.5f));
        jelovnici.add(new Jelovnik("MEX_1", "Taco", 6.0f));
        
        Field fieldJelovnici = PosluziteljTvrtka.class.getDeclaredField("jelovnici");
        fieldJelovnici.setAccessible(true);
        fieldJelovnici.set(posluziteljTvrtka, jelovnici);
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("dohvatiJelovnikPartnera", Partner.class);
        method.setAccessible(true);
        
        List<Jelovnik> jelovnikPartnera = (List<Jelovnik>) method.invoke(posluziteljTvrtka, partner);
        
        assertEquals(2, jelovnikPartnera.size());
        boolean sviITA = true;
        boolean nijedanMEX = true;
        
        for (Jelovnik j : jelovnikPartnera) {
            if (!j.id().startsWith("ITA")) {
                sviITA = false;
            }
            if (j.id().startsWith("MEX")) {
                nijedanMEX = false;
            }
        }
        
        assertTrue(sviITA);
        assertTrue(nijedanMEX);
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
    
    @Test
    @Order(12)
    void testKuhinjaPostoji() throws Exception {
        String nazivDatoteke = this.getClass().getName() + "_kuhinje.txt";
        Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
        konfig.spremiPostavku("kuhinja_1", "ITA;Talijanska");
        konfig.spremiPostavku("kuhinja_2", "MEX;Meksička");
        konfig.spremiKonfiguraciju();
        
        posluziteljTvrtka.ucitajKonfiguraciju(nazivDatoteke);
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("kuhinjaPostoji", String.class);
        method.setAccessible(true);
        
        boolean postojiKuhinja = (boolean) method.invoke(posluziteljTvrtka, "ITA");
        assertTrue(postojiKuhinja);
        
        boolean nePostojiKuhinja = (boolean) method.invoke(posluziteljTvrtka, "CHN");
        assertFalse(nePostojiKuhinja);
        
        Files.delete(Path.of(nazivDatoteke));
    }
    
    @Test
    @Order(13)
    void testDohvatiPostojeceKuhinje() throws Exception {
        String nazivDatoteke = this.getClass().getName() + "_kuhinje.txt";
        Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
        konfig.spremiPostavku("kuhinja_1", "ITA;Talijanska");
        konfig.spremiPostavku("kuhinja_2", "MEX;Meksička");
        konfig.spremiKonfiguraciju();
        
        posluziteljTvrtka.ucitajKonfiguraciju(nazivDatoteke);
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod("dohvatiPostojeceKuhinje");
        method.setAccessible(true);
        
        Set<String> kuhinje = (Set<String>) method.invoke(posluziteljTvrtka);
        
        assertEquals(2, kuhinje.size());
        assertTrue(kuhinje.contains("ITA"));
        assertTrue(kuhinje.contains("MEX"));
        assertFalse(kuhinje.contains("CHN"));
        
        Files.delete(Path.of(nazivDatoteke));
    }
    
    @Test
    @Order(14)
    void testProvjeriKomandaKraj() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter out = new PrintWriter(stringWriter);
        
        Field field = PosluziteljTvrtka.class.getDeclaredField("kodZaKraj");
        field.setAccessible(true);
        field.set(posluziteljTvrtka, "tajna");
        
        Method method = PosluziteljTvrtka.class.getDeclaredMethod(
                "provjeriKomandaKraj", String.class, PrintWriter.class);
        method.setAccessible(true);
        
        boolean rezultatNull = (boolean) method.invoke(posluziteljTvrtka, null, out);
        assertFalse(rezultatNull);
        assertTrue(stringWriter.toString().contains("ERROR 19"));
        
        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
        
        boolean rezultatBezRazmaka = (boolean) method.invoke(posluziteljTvrtka, "KRAJtajna", out);
        assertFalse(rezultatBezRazmaka);
        assertTrue(stringWriter.toString().contains("ERROR 10"));
        
        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
        
        boolean rezultatPogresnKod = (boolean) method.invoke(posluziteljTvrtka, "KRAJ kriva_tajna", out);
        assertFalse(rezultatPogresnKod);
        assertTrue(stringWriter.toString().contains("ERROR 10"));
        
        stringWriter = new StringWriter();
        out = new PrintWriter(stringWriter);
        
        boolean rezultatIspravan = (boolean) method.invoke(posluziteljTvrtka, "KRAJ tajna", out);
        assertTrue(rezultatIspravan);
    }
}