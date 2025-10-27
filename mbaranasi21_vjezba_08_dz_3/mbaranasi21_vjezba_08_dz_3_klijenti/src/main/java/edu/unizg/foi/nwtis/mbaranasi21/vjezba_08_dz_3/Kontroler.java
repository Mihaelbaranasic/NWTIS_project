package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import java.text.SimpleDateFormat;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.Obracun;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;

/**
 * Kontroler za Jakarta MVC dio aplikacije.
 * Pokriva javni, privatni i administracijski dio za tvrtku.
 * Omogućuje pristup RESTful servisima i upravljanje korisničkim sučeljem.
 * 
 * @author mbaranasi21
 */
@Controller
@Path("")
@RequestScoped
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @Inject
  @RestClient
  ServisTvrtkaKlijent servisTvrtka;

  /**
   * Prikazuje početnu stranicu MVC aplikacije.
   * Služi kao glavni ulazna točka u sustav.
   */
  @GET
  @Path("/")
  @View("index.jsp")
  public void pocetnaStranina() {}

  /**
   * Prikazuje početnu stranicu Tvrtka dijela aplikacije s navigacijskim linkovima.
   * Omogućuje pristup svim funkcionalnostima tvrtke.
   */
  @GET
  @Path("tvrtka/pocetak")
  @View("index.jsp")
  public void pocetak() {}

  /**
   * Provjerava status poslužitelja tvrtke.
   * Koristi REST api/tvrtka HEAD metodu za provjeru dostupnosti.
   * Dohvaća statusne informacije o glavnom poslužitelju i njegovim dijelovima.
   */
  @GET
  @Path("tvrtka/status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }

  /**
   * Prikazuje pregled svih partnera/restorana u sustavu.
   * Koristi REST api/tvrtka GET/partner metodu za dohvaćanje liste partnera.
   * U slučaju greške prikazuje odgovarajuću poruku korisniku.
   */
  @GET
  @Path("tvrtka/partner")
  @View("partneri.jsp")
  public void partneri() {
      try {
          var odgovor = this.servisTvrtka.getPartneri();
          var status = odgovor.getStatus();
          this.model.put("status", status);
          
          if (status == 200) {
              var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
              this.model.put("partneri", partneri);
          } else {
              this.model.put("greska", "REST servis nije dostupan. Status: " + status);
          }
      } catch (Exception e) {
          this.model.put("greska", "Greška pri komunikaciji s REST servisom: " + e.getMessage());
          this.model.put("status", 500);
      }
  }

  /**
   * Alternativna putanja za pristup popisu partnera.
   * Omogućuje pristup istoj funkcionalnosti putem drugačije URL putanje.
   */
  @GET
  @Path("tvrtka/partneri")
  @View("partneri.jsp")
  public void partneriAlias() {
    partneri();
  }

  /**
   * Prikazuje detaljne informacije o određenom partneru/restoranu.
   * Koristi REST api/tvrtka GET/partner/{id} metodu za dohvaćanje podataka.
   * 
   * @param id identifikator partnera za koji se dohvaćaju podaci
   */
  @GET
  @Path("tvrtka/partner/{id}")
  @View("partner_detalji.jsp")
  public void partnerDetalji(@PathParam("id") int id) {
    var odgovor = this.servisTvrtka.getPartner(id);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var partner = odgovor.readEntity(Partner.class);
      this.model.put("partner", partner);
    }
  }

  /**
   * Prikazuje pregled obračuna za jelo i piće u određenom vremenskom razdoblju.
   * Konvertira datume iz korisničkog sučelja u timestamp format za REST servis.
   * Koristi REST api/tvrtka GET/obracun metodu s vremenskim filterom.
   * 
   * @param od početni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   * @param doVrijeme završni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   */
  @GET
  @Path("tvrtka/obracun")
  @View("obracun_pregled.jsp")
  public void obracunPregled(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    System.out.println("=== OBRACUN DEBUG ===");
    System.out.println("Od parametar: " + od);
    System.out.println("Do parametar: " + doVrijeme);
    
    String odTimestamp = konvertirajDatum(od);
    String doTimestamp = konvertirajDatum(doVrijeme);
    
    System.out.println("Od timestamp: " + odTimestamp);
    System.out.println("Do timestamp: " + doTimestamp);
    
    var odgovor = this.servisTvrtka.getObracuni(odTimestamp, doTimestamp);
    var status = odgovor.getStatus();
    
    System.out.println("REST servis status: " + status);
    
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      System.out.println("Broj obračuna: " + (obracuni != null ? obracuni.size() : 0));
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Svi (jelo i piće)");
  }

  /**
   * Prikazuje pregled obračuna samo za jela u određenom vremenskom razdoblju.
   * Koristi REST api/tvrtka GET/obracun/jelo metodu s vremenskim filterom.
   * 
   * @param od početni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   * @param doVrijeme završni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   */
  @GET
  @Path("tvrtka/obracun/jelo")
  @View("obracun_pregled.jsp")
  public void obracunJelo(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    String odTimestamp = konvertirajDatum(od);
    String doTimestamp = konvertirajDatum(doVrijeme);
    
    var odgovor = this.servisTvrtka.getObracuniJelo(odTimestamp, doTimestamp);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Jelo");
  }

  /**
   * Prikazuje pregled obračuna samo za pića u određenom vremenskom razdoblju.
   * Koristi REST api/tvrtka GET/obracun/pice metodu s vremenskim filterom.
   * 
   * @param od početni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   * @param doVrijeme završni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   */
  @GET
  @Path("tvrtka/obracun/pice")
  @View("obracun_pregled.jsp")
  public void obracunPice(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    String odTimestamp = konvertirajDatum(od);
    String doTimestamp = konvertirajDatum(doVrijeme);
    
    var odgovor = this.servisTvrtka.getObracuniPice(odTimestamp, doTimestamp);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Piće");
  }

  /**
   * Prikazuje pregled obračuna određenog partnera u vremenskom razdoblju.
   * Koristi REST api/tvrtka GET/obracun/{id} metodu s vremenskim filterom.
   * 
   * @param partnerId identifikator partnera za koji se dohvaćaju obračuni
   * @param od početni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   * @param doVrijeme završni datum i vrijeme za filter (format: yyyy-MM-dd HH:mm:ss)
   */
  @GET
  @Path("tvrtka/obracun/partner/{id}")
  @View("obracun_partner.jsp")
  public void obracunPartner(@PathParam("id") int partnerId, 
                            @QueryParam("od") String od, 
                            @QueryParam("do") String doVrijeme) {
      String odTimestamp = konvertirajDatum(od);
      String doTimestamp = konvertirajDatum(doVrijeme);
      
      var odgovor = this.servisTvrtka.getObracuniPartner(partnerId, odTimestamp, doTimestamp);
      var status = odgovor.getStatus();
      this.model.put("status", status);
      if (status == 200) {
          var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
          this.model.put("obracuni", obracuni);
      }
      this.model.put("partnerId", partnerId);
  }

  /**
   * Prikazuje obrazac za dodavanje novog partnera u sustav.
   * Omogućuje administratorima unos podataka o novom partneru.
   */
  @GET
  @Path("tvrtka/admin/partner/novi")
  @View("novi_partner.jsp")
  public void noviPartnerObrazac() {}

  /**
   * Prikazuje obrazac za dodavanje novog partnera - kratka putanja.
   * Alternativni pristup istom obrascu putem kraće URL putanje.
   */
  @GET
  @Path("tvrtka/admin/partner")
  @View("novi_partner.jsp")
  public void noviPartnerKratko() {}

  /**
   * Obrađuje zahtjev za dodavanje novog partnera u sustav.
   * Koristi REST api/tvrtka POST/partner metodu za kreiranje novog partnera.
   * Validira unesene podatke i prikazuje odgovarajuću poruku korisniku.
   * 
   * @param naziv naziv partnera/restorana
   * @param vrstaKuhinje tip kuhinje koji partner nudi
   * @param adresa fizička adresa partnera
   * @param mreznaVrata mrežna vrata za komunikaciju s partnerom
   * @param mreznaVrataKraj mrežna vrata za administrativne komande
   * @param gpsSirina GPS koordinata - geografska širina
   * @param gpsDuzina GPS koordinata - geografska dužina
   * @param sigurnosniKod sigurnosni kod za autentifikaciju partnera
   * @param adminKod administrativni kod za upravljanje partnerom
   */
  @POST
  @Path("tvrtka/admin/partner/novi")
  @View("novi_partner.jsp")
  public void dodajPartnera(@FormParam("naziv") String naziv,
                           @FormParam("vrstaKuhinje") String vrstaKuhinje,
                           @FormParam("adresa") String adresa,
                           @FormParam("mreznaVrata") int mreznaVrata,
                           @FormParam("mreznaVrataKraj") int mreznaVrataKraj,
                           @FormParam("gpsSirina") float gpsSirina,
                           @FormParam("gpsDuzina") float gpsDuzina,
                           @FormParam("sigurnosniKod") String sigurnosniKod,
                           @FormParam("adminKod") String adminKod) {
    try {
      Partner noviPartner = new Partner(0, naziv, vrstaKuhinje, adresa, 
                                       mreznaVrata, mreznaVrataKraj, 
                                       gpsSirina, gpsDuzina, 
                                       sigurnosniKod, adminKod);
      
      var odgovor = this.servisTvrtka.postPartner(noviPartner);
      var status = odgovor.getStatus();
      
      this.model.put("status", status);
      if (status == 201) {
        this.model.put("poruka", "Partner je uspješno dodan!");
      } else {
        this.model.put("poruka", "Greška pri dodavanju partnera.");
      }
    } catch (Exception e) {
      this.model.put("poruka", "Greška: " + e.getMessage());
    }
  }

  /**
   * Obrađuje zahtjev za dodavanje novog partnera - kratka putanja.
   * Alternativni pristup za dodavanje partnera putem kraće URL putanje.
   * Poziva glavnu metodu za dodavanje partnera s istim parametrima.
   * 
   * @param naziv naziv partnera/restorana
   * @param vrstaKuhinje tip kuhinje koji partner nudi
   * @param adresa fizička adresa partnera
   * @param mreznaVrata mrežna vrata za komunikaciju s partnerom
   * @param mreznaVrataKraj mrežna vrata za administrativne komande
   * @param gpsSirina GPS koordinata - geografska širina
   * @param gpsDuzina GPS koordinata - geografska dužina
   * @param sigurnosniKod sigurnosni kod za autentifikaciju partnera
   * @param adminKod administrativni kod za upravljanje partnerom
   */
  @POST
  @Path("tvrtka/admin/partner")
  @View("novi_partner.jsp")
  public void dodajPartneraKratko(@FormParam("naziv") String naziv,
                                 @FormParam("vrstaKuhinje") String vrstaKuhinje,
                                 @FormParam("adresa") String adresa,
                                 @FormParam("mreznaVrata") int mreznaVrata,
                                 @FormParam("mreznaVrataKraj") int mreznaVrataKraj,
                                 @FormParam("gpsSirina") float gpsSirina,
                                 @FormParam("gpsDuzina") float gpsDuzina,
                                 @FormParam("sigurnosniKod") String sigurnosniKod,
                                 @FormParam("adminKod") String adminKod) {
    dodajPartnera(naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj,
                 gpsSirina, gpsDuzina, sigurnosniKod, adminKod);
  }

  /**
   * Prikazuje obrazac za aktiviranje spavanja poslužitelja.
   * Omogućuje administratorima postavljanje dretve u spavanje na određeno vrijeme.
   */
  @GET
  @Path("tvrtka/admin/spavanje")
  @View("spavanje.jsp")
  public void spavanje() {}

  /**
   * Aktivira spavanje poslužitelja na određeno vrijeme.
   * Koristi REST api/tvrtka GET/spava&vrijeme=trajanje metodu.
   * Prikazuje rezultat operacije korisniku.
   * 
   * @param vrijeme trajanje spavanja u sekundama
   */
  @POST
  @Path("tvrtka/admin/spavanje")
  @View("spavanje.jsp")
  public void aktivirajSpavanje(@FormParam("vrijeme") int vrijeme) {
    var odgovor = this.servisTvrtka.getSpavanje(vrijeme);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    this.model.put("poruka", status == 200 ? 
        "Spavanje je aktivirano na " + vrijeme + " sekundi" : 
        "Greška pri aktiviranju spavanja");
  }

  /**
   * Prikazuje administracijsku konzolu za upravljanje poslužiteljem tvrtke.
   * Dohvaća statusne informacije o svim dijelovima poslužitelja.
   * Omogućuje administratorima nadzor nad radom sustava.
   */
  @GET
  @Path("tvrtka/admin/konzola")
  @View("admin_konzola.jsp")
  public void adminKonzola() {
    dohvatiStatuse();
  }

  /**
   * Prikazuje naprednu nadzornu konzolu za upravljanje poslužiteljem tvrtke.
   * Omogućuje detaljni nadzor i upravljanje svim aspektima poslužitelja.
   */
  @GET
  @Path("tvrtka/admin/nadzornaKonzolaTvrtka")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {}

  /**
   * Inicijalizira završetak rada poslužitelja tvrtke.
   * Koristi REST api/tvrtka HEAD/kraj metodu za sigurno zatvaranje sustava.
   * Dohvaća finalne statusne informacije prije zatvaranja.
   */
  @GET
  @Path("tvrtka/kraj")
  @View("status.jsp")
  public void kraj() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }

  /**
   * Pokreće određeni dio poslužitelja tvrtke.
   * Koristi REST api/tvrtka HEAD/start/{id} metodu za aktivaciju dijela sustava.
   * 
   * @param id identifikator dijela poslužitelja koji se pokreće
   */
  @GET
  @Path("tvrtka/start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  /**
   * Postavlja određeni dio poslužitelja tvrtke u pauzu.
   * Koristi REST api/tvrtka HEAD/pauza/{id} metodu za privremeno zaustavljanje dijela sustava.
   * 
   * @param id identifikator dijela poslužitelja koji se postavlja u pauzu
   */
  @GET
  @Path("tvrtka/pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  /**
   * Konvertira datum iz korisničkog sučelja u timestamp format za REST servis.
   * Podržava više formata datuma: "yyyy-MM-dd HH:mm:ss" i "yyyy-MM-dd".
   * 
   * @param datum datum u string formatu za konverziju
   * @return timestamp kao string ili null ako konverzija nije uspješna
   */
  private String konvertirajDatum(String datum) {
    if (datum == null || datum.trim().isEmpty()) {
        return null;
    }
    try {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long timestamp = inputFormat.parse(datum).getTime();
        return String.valueOf(timestamp);
    } catch (Exception e) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long timestamp = dateFormat.parse(datum).getTime();
            return String.valueOf(timestamp);
        } catch (Exception ex) {
            System.err.println("Greška pri parsiranju datuma: " + datum);
            return null;
        }
    }
  }

  /**
   * Pomoćna metoda za dohvaćanje statusa svih dijelova poslužitelja tvrtke.
   * Poziva REST servise za provjeru stanja glavnog poslužitelja i njegovih komponenti.
   * Postavlja statusne informacije u model za prikaz u korisničkom sučelju.
   */
  private void dohvatiStatuse() {
    this.model.put("samoOperacija", false);
    var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
    this.model.put("statusT", statusT);
    var statusT1 = this.servisTvrtka.headPosluziteljStatus(1).getStatus();
    this.model.put("statusT1", statusT1);
    var statusT2 = this.servisTvrtka.headPosluziteljStatus(2).getStatus();
    this.model.put("statusT2", statusT2);
  }
}