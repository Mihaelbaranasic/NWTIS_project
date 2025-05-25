package edu.unizg.foi.nwtis.mbaranasi21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource za upravljanje partnerom.
 */
@Path("api/partner")
public class PartnerResource {

  @Inject
  @ConfigProperty(name = "adresaPartner")
  private String partnerAdresa;
  @Inject
  @ConfigProperty(name = "mreznaVrataKrajPartner")
  private String mreznaVrataKrajPartner;
  @Inject
  @ConfigProperty(name = "mreznaVrataRadPartner")
  private String mreznaVrataRadPartner;
  @Inject
  @ConfigProperty(name = "kodZaAdminPartnera")
  private String kodZaAdminPartnera;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;
  @Inject
  @ConfigProperty(name = "idPartner")
  private String idPartner;

  @Inject
  RestConfiguration restConfiguration;

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  // ==================== HEAD METODE ====================

  /**
   * Provjera statusa poslužitelja partner.
   */
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_headPosluziteljPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPartner", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPartner() {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "STATUS " + this.kodZaAdminPartnera + " 1");
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Provjera statusa dijela poslužitelja partner.
   */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPartnerStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPartnerStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPartnerStatus(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "STATUS " + this.kodZaAdminPartnera + " " + id);
    if (status != null && status.startsWith("OK")) {
      String[] dijelovi = status.split(" ");
      if (dijelovi.length == 2) {
        int statusVrijednost = Integer.parseInt(dijelovi[1]);
        if (statusVrijednost == 1) {
          return Response.status(Response.Status.OK).build();
        } else {
          return Response.status(Response.Status.CONFLICT).build();
        }
      }
    }
    return Response.status(Response.Status.CONFLICT).build();
  }

  /**
   * Postavljanje dijela poslužitelja partner u pauzu.
   */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partner u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPartnerPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPartnerPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPartnerPauza(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "PAUZA " + this.kodZaAdminPartnera + " " + id);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  /**
   * Postavljanje dijela poslužitelja partner u rad.
   */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partner u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPartnerStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPartnerStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPartnerStart(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "START " + this.kodZaAdminPartnera + " " + id);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }

  /**
   * Zaustavljanje poslužitelja partner.
   */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljPartnerKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPartnerKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPartnerKraj() {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "KRAJ " + this.kodZaKraj);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.CONFLICT).build();
    }
  }
  
  /**
   * Dohvat jelovnika partnera.
   */
  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Neautorizirani pristup"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnikPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnikPartner", description = "Vrijeme trajanja metode")
  public Response getJelovnikPartner(@HeaderParam("korisnik") String korisnik, 
                                    @HeaderParam("lozinka") String lozinka) {
    if (!provjeriAutentikaciju(korisnik, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var jelovnik = dohvatiJelovnikSPartnera(korisnik);
    if (jelovnik != null) {
      return Response.ok(jelovnik).status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat karte pića partnera.
   */
  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pića partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Neautorizirani pristup"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPicaPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPicaPartner", description = "Vrijeme trajanja metode")
  public Response getKartaPicaPartner(@HeaderParam("korisnik") String korisnik, 
                                     @HeaderParam("lozinka") String lozinka) {
    if (!provjeriAutentikaciju(korisnik, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var kartaPica = dohvatiKartuPicaSPartnera(korisnik);
    if (kartaPica != null) {
      return Response.ok(kartaPica).status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat stavki otvorene narudžbe.
   */
  @Path("narudzba")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat stavki otvorene narudžbe")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Neautorizirani pristup"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getNarudzbaPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getNarudzbaPartner", description = "Vrijeme trajanja metode")
  public Response getNarudzbaPartner(@HeaderParam("korisnik") String korisnik, 
                                    @HeaderParam("lozinka") String lozinka) {
    if (!provjeriAutentikaciju(korisnik, lozinka)) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    var narudzba = dohvatiStanjeNarudzbe(korisnik);
    if (narudzba != null) {
      return Response.ok(narudzba).status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat svih korisnika.
   */
  @Path("korisnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisniciPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisniciPartner", description = "Vrijeme trajanja metode")
  public Response getKorisniciPartner() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnici = korisnikDAO.dohvatiSve();
      
      if (korisnici == null) {
        korisnici = new ArrayList<>();
      }
      
      return Response.ok(korisnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat određenog korisnika.
   */
  @Path("korisnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat određenog korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnikPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnikPartner", description = "Vrijeme trajanja metode")
  public Response getKorisnikPartner(@PathParam("id") String korisnickoIme) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnik = korisnikDAO.dohvati(korisnickoIme, null, false);
      
      if (korisnik != null) {
        return Response.ok(korisnik).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Spavanje dretve.
   */
  @Path("spava")
  @GET
  @Operation(summary = "Spavanje dretve")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpavanjePartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpavanjePartner", description = "Vrijeme trajanja metode")
  public Response getSpavanjePartner(@QueryParam("vrijeme") int trajanje) {
    var status = posaljiKomandu(mreznaVrataKrajPartner, "SPAVA " + this.kodZaAdminPartnera + " " + trajanje);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Šalje komandu na poslužitelj partner.
   */
  private String posaljiKomandu(String mreznaVrata, String komanda) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, Integer.parseInt(mreznaVrata));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Šalje komandu i čita više linija odgovora.
   */
  private String posaljiViseLinija(String mreznaVrata, String komanda) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, Integer.parseInt(mreznaVrata));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      
      StringBuilder odgovor = new StringBuilder();
      String linija;
      while ((linija = in.readLine()) != null) {
        odgovor.append(linija).append("\n");
      }
      
      mreznaUticnica.close();
      return odgovor.toString().trim();
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Provjerava autentikaciju korisnika.
   */
  private boolean provjeriAutentikaciju(String korisnik, String lozinka) {
    if (korisnik == null || lozinka == null) {
      return false;
    }

    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var korisnikObj = korisnikDAO.dohvati(korisnik, lozinka, true);
      return korisnikObj != null;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Dohvaća jelovnik s partnera.
   */
  private List<Jelovnik> dohvatiJelovnikSPartnera(String korisnik) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, Integer.parseInt(this.mreznaVrataRadPartner));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("JELOVNIK " + korisnik + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      
      String statusLinija = in.readLine();
      if (statusLinija != null && statusLinija.startsWith("OK")) {
        StringBuilder jsonBuilder = new StringBuilder();
        String linija;
        while ((linija = in.readLine()) != null) {
          jsonBuilder.append(linija);
        }
        
        String jsonJelovnik = jsonBuilder.toString();
        List<Jelovnik> jelovnik = gson.fromJson(jsonJelovnik, new TypeToken<List<Jelovnik>>() {}.getType());
        
        mreznaUticnica.close();
        return jelovnik;
      }
      
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Dohvaća kartu pića s partnera.
   */
  private List<KartaPica> dohvatiKartuPicaSPartnera(String korisnik) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, Integer.parseInt(this.mreznaVrataRadPartner));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("KARTAPIĆA " + korisnik + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      
      String statusLinija = in.readLine();
      if (statusLinija != null && statusLinija.startsWith("OK")) {
        StringBuilder jsonBuilder = new StringBuilder();
        String linija;
        while ((linija = in.readLine()) != null) {
          jsonBuilder.append(linija);
        }
        
        String jsonKartaPica = jsonBuilder.toString();
        List<KartaPica> kartaPica = gson.fromJson(jsonKartaPica, new TypeToken<List<KartaPica>>() {}.getType());
        
        mreznaUticnica.close();
        return kartaPica;
      }
      
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Dohvaća stanje narudžbe s partnera.
   */
  private List<Narudzba> dohvatiStanjeNarudzbe(String korisnik) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, Integer.parseInt(this.mreznaVrataRadPartner));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("STANJE " + korisnik + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      
      String statusLinija = in.readLine();
      if (statusLinija != null && statusLinija.startsWith("OK")) {
        StringBuilder jsonBuilder = new StringBuilder();
        String linija;
        while ((linija = in.readLine()) != null) {
          jsonBuilder.append(linija);
        }
        
        String jsonNarudzba = jsonBuilder.toString();
        List<Narudzba> narudzba = gson.fromJson(jsonNarudzba, new TypeToken<List<Narudzba>>() {}.getType());
        
        mreznaUticnica.close();
        return narudzba;
      }
      
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return null;
  }
}