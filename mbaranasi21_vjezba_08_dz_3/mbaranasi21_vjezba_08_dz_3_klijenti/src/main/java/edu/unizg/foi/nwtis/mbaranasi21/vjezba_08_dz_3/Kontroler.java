package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

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
 * Kontroler za Jakarta MVC dio aplikacije
 * Pokriva javni, privatni i administracijski dio za tvrtku
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

  @GET
  @Path("tvrtka/pocetak")
  @View("index.jsp")
  public void pocetak() {}

  /**
   * Provjera rada poslužitelja (koristi REST api/tvrtka metodu HEAD)
   */
  @GET
  @Path("tvrtka/status")
  @View("status.jsp")
  public void status() {
    dohvatiStatuse();
  }

  /**
   * Pregled naziva partnera/restorana (koristi REST api/tvrtka metodu GET/partner)
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
   * Pregled odabranog partnera/restorana (koristi REST api/tvrtka metodu GET/partner/{id})
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
   * Pregled obračuna u razdoblju - jelo i piće
   */
  @GET
  @Path("tvrtka/privatno/obracun")
  @View("obracun_pregled.jsp")
  public void obracunPregled(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    System.out.println("Pristup obracun pregled - korisnik autentificiran");
    var odgovor = this.servisTvrtka.getObracuni(od, doVrijeme);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Svi (jelo i piće)");
  }

  /**
   * Pregled obračuna u razdoblju - samo jelo
   */
  @GET
  @Path("tvrtka/privatno/obracun/jelo")
  @View("obracun_pregled.jsp")
  public void obracunJelo(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    var odgovor = this.servisTvrtka.getObracuniJelo(od, doVrijeme);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Jelo");
  }

  /**
   * Pregled obračuna u razdoblju - samo piće
   */
  @GET
  @Path("tvrtka/privatno/obracun/pice")
  @View("obracun_pregled.jsp")
  public void obracunPice(@QueryParam("od") String od, @QueryParam("do") String doVrijeme) {
    var odgovor = this.servisTvrtka.getObracuniPice(od, doVrijeme);
    var status = odgovor.getStatus();
    this.model.put("status", status);
    if (status == 200) {
      var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
      this.model.put("obracuni", obracuni);
    }
    this.model.put("tipObracuna", "Piće");
  }

  /**
   * Pregled obračuna određenog partnera u razdoblju
   */
  @GET
  @Path("tvrtka/privatno/obracun/partner/{id}")
  @View("obracun_partner.jsp")
  public void obracunPartner(@PathParam("id") int partnerId, 
                            @QueryParam("od") String od, 
                            @QueryParam("do") String doVrijeme) {
      var odgovor = this.servisTvrtka.getObracuniPartner(partnerId, od, doVrijeme);
      var status = odgovor.getStatus();
      this.model.put("status", status);
      if (status == 200) {
          var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {});
          this.model.put("obracuni", obracuni);
      }
      this.model.put("partnerId", partnerId);
  }

  /**
   * Prikaz obrasca za dodavanje novog partnera
   */
  @GET
  @Path("tvrtka/admin/partner/novi")
  @View("novi_partner.jsp")
  public void noviPartnerObrazac() {}

  /**
   * Dodavanje novog partnera (koristi REST api/tvrtka metodu POST/partner)
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
   * Prikaz obrasca za aktiviranje spavanja
   */
  @GET
  @Path("tvrtka/admin/spavanje")
  @View("spavanje.jsp")
  public void spavanje() {}

  /**
   * Aktiviranje spavanja (koristi REST api/tvrtka metodu GET/spava&vrijeme=trajanje)
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
   * Konzola za upravljanje poslužiteljem Tvrtka
   */
  @GET
  @Path("tvrtka/admin/konzola")
  @View("admin_konzola.jsp")
  public void adminKonzola() {
    dohvatiStatuse();
  }

  @GET
  @Path("tvrtka/admin/nadzornaKonzolaTvrtka")
  @View("nadzornaKonzolaTvrtka.jsp")
  public void nadzornaKonzolaTvrtka() {}

  @GET
  @Path("tvrtka/kraj")
  @View("status.jsp")
  public void kraj() {
    var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
    this.model.put("statusOperacije", status);
    dohvatiStatuse();
  }

  @GET
  @Path("tvrtka/start/{id}")
  @View("status.jsp")
  public void startId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

  @GET
  @Path("tvrtka/pauza/{id}")
  @View("status.jsp")
  public void pauzatId(@PathParam("id") int id) {
    var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
    this.model.put("status", status);
    this.model.put("samoOperacija", true);
  }

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