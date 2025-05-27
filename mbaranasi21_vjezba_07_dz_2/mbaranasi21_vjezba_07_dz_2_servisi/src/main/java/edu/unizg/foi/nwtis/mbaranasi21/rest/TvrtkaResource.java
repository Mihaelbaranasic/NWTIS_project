package edu.unizg.foi.nwtis.mbaranasi21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.dao.ObracunDAO;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.dao.PartnerDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/tvrtka")
public class TvrtkaResource {

  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private String mreznaVrataKraj;
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private String mreznaVrataRegistracija;
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private String mreznaVrataRad;
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  @Inject
  RestConfiguration restConfiguration;

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();


  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_headPosluzitelj", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    var status = posaljiKomandu(mreznaVrataKraj, "STATUS " + this.kodZaAdminTvrtke + " 1");
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("status/{id}")
  @HEAD
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKraj, "STATUS " + this.kodZaAdminTvrtke + " " + id);
    if (status != null && status.startsWith("OK")) {
      String[] dijelovi = status.split(" ");
      if (dijelovi.length == 2) {
        int statusVrijednost = Integer.parseInt(dijelovi[1]);
        if (statusVrijednost == 1) {
          return Response.status(Response.Status.OK).build();
        } else {
          return Response.status(Response.Status.NO_CONTENT).build();
        }
      }
    }
    return Response.status(Response.Status.NOT_FOUND).build(); 
  }

  @Path("pauza/{id}")
  @HEAD
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKraj, "PAUZA " + this.kodZaAdminTvrtke + " " + id);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build(); 
    }
  }

  @Path("start/{id}")
  @HEAD
  public Response headPosluziteljStart(@PathParam("id") int id) {
    var status = posaljiKomandu(mreznaVrataKraj, "START " + this.kodZaAdminTvrtke + " " + id);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @Path("kraj")
  @HEAD
  public Response headPosluziteljKraj() {
    var status = posaljiKomandu(mreznaVrataKraj, "KRAJWS " + this.kodZaKraj);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @Path("kraj/info")
  @HEAD
  public Response headPosluziteljKrajInfo() {
    return Response.status(Response.Status.NOT_FOUND).build();
  }


  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih jelovnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnici", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnici", description = "Vrijeme trajanja metode")
  public Response getJelovnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);
      
      List<Jelovnik> sviJelovnici = new ArrayList<>();
      
      for (Partner partner : partneri) {
        var jelovnik = dohvatiJelovnikPartnera(partner.id(), partner.sigurnosniKod());
        if (jelovnik != null) {
          sviJelovnici.addAll(jelovnik);
        }
      }
      
      return Response.ok(sviJelovnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("jelovnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, false);
      
      if (partner == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      
      var jelovnik = dohvatiJelovnikPartnera(id, partner.sigurnosniKod());
      if (jelovnik != null) {
        return Response.ok(jelovnik).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pića")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPica", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
  public Response getKartaPica() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);
      
      if (!partneri.isEmpty()) {
        Partner partner = partneri.get(0);
        var kartaPica = dohvatiKartuPicaPartnera(partner.id(), partner.sigurnosniKod());
        if (kartaPica != null) {
          return Response.ok(kartaPica).status(Response.Status.OK).build();
        }
      }
      
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(true);
      return Response.ok(partneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner/provjera")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat partnera koji se nalaze u tablici i na poslužitelju")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneriProjerica", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneriProverica", description = "Vrijeme trajanja metode")
  public Response getPartneriProverica() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);
      
      var popisSPosluzitelja = dohvatiPopisPartnera();
      List<Partner> aktivniPartneri = new ArrayList<>();
      
      if (popisSPosluzitelja != null) {
        for (Partner partner : partneri) {
          boolean postojiNaPosluzitelju = popisSPosluzitelja.stream()
              .anyMatch(pp -> pp.id() == partner.id());
          
          if (postojiNaPosluzitelju) {
            aktivniPartneri.add(partner.partnerBezKodova());
          }
        }
      }
      
      return Response.ok(aktivniPartneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {
        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje novog partnera")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var status = partnerDAO.dodaj(partner);
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("obracun")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna s vremenskim filterom")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuni", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuni", description = "Vrijeme trajanja metode")
  public Response getObracuni(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obracuni;
      
      if (vrijemeOd != null || vrijemeDo != null) {
        obracuni = obracunDAO.dohvatiSVremenskomFilterom(vrijemeOd, vrijemeDo);
      } else {
        obracuni = obracunDAO.dohvatiSve();
      }
      
      if (obracuni == null) {
        obracuni = new ArrayList<>();
      }
      
      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/jelo")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna za jela s vremenskim filterom")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuniJelo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuniJelo", description = "Vrijeme trajanja metode")
  public Response getObracuniJelo(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      var obracuni = obracunDAO.dohvatiJelaSVremenskomFilterom(vrijemeOd, vrijemeDo);
      
      if (obracuni == null) {
        obracuni = new ArrayList<>();
      }
      
      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/pice")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna za piće s vremenskim filterom")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuniPice", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuniPice", description = "Vrijeme trajanja metode")
  public Response getObracuniPice(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      var obracuni = obracunDAO.dohvatiPiceSVremenskomFilterom(vrijemeOd, vrijemeDo);
      
      if (obracuni == null) {
        obracuni = new ArrayList<>();
      }
      
      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna za partnera s vremenskim filterom")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuniPartner", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuniPartner", description = "Vrijeme trajanja metode")
  public Response getObracuniPartner(@PathParam("id") int id, @QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      var obracuni = obracunDAO.dohvatiZaPartneraSVremenskomFilterom(id, vrijemeOd, vrijemeDo);
      
      if (obracuni == null) {
        obracuni = new ArrayList<>();
      }
      
      return Response.ok(obracuni).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("obracun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obračuna")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracun", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
  public Response postObracun(List<Obracun> obracuni) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      boolean status = obracunDAO.dodajVise(obracuni);
      
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path("obracun/ws")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje obračuna i slanje na poslužitelj")
  @APIResponses(value = {@APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracunWS", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracunWS", description = "Vrijeme trajanja metode")
  public Response postObracunWS(List<Obracun> obracuni) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      boolean status = obracunDAO.dodajVise(obracuni);
      
      if (status) {
        Map<Integer, List<Obracun>> grupirani = obracuni.stream()
            .collect(Collectors.groupingBy(Obracun::partner));
        
        var partnerDAO = new PartnerDAO(vezaBP);
        boolean sviUspjesni = true;
        
        for (Map.Entry<Integer, List<Obracun>> entry : grupirani.entrySet()) {
          int partnerId = entry.getKey();
          List<Obracun> obracuniPartnera = entry.getValue();
          
          var partner = partnerDAO.dohvati(partnerId, false);
          if (partner != null) {
            String jsonObracuni = gson.toJson(obracuniPartnera);
            var odgovor = posaljiObracunNaPosluzitelj(partnerId, partner.sigurnosniKod(), jsonObracuni);
            
            if (odgovor == null || !odgovor.startsWith("OK")) {
              sviUspjesni = false;
            }
          } else {
            sviUspjesni = false;
          }
        }
        
        if (sviUspjesni) {
          return Response.status(Response.Status.CREATED).build();
        } else {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  @Path("spava")
  @GET
  @Operation(summary = "Spavanje dretve")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
  public Response getSpava(@QueryParam("vrijeme") int trajanje) {
    var status = posaljiKomandu(mreznaVrataKraj, "SPAVA " + this.kodZaAdminTvrtke + " " + trajanje);
    if (status != null && status.startsWith("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  /**
   * Šalje komandu na poslužitelj tvrtka.
   */
  private String posaljiKomandu(String mreznaVrata, String komanda) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(mreznaVrata));
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
   * Dohvaća jelovnik partnera s poslužitelja.
   */
  private List<Jelovnik> dohvatiJelovnikPartnera(int partnerId, String sigurnosniKod) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("JELOVNIK " + partnerId + " " + sigurnosniKod + "\n");
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
   * Dohvaća kartu pića partnera s poslužitelja.
   */
  private List<KartaPica> dohvatiKartuPicaPartnera(int partnerId, String sigurnosniKod) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("KARTAPIĆA " + partnerId + " " + sigurnosniKod + "\n");
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
   * Dohvaća popis partnera s poslužitelja.
   */
  private List<PartnerPopis> dohvatiPopisPartnera() {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRegistracija));
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      
      out.write("POPIS\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      
      String statusLinija = in.readLine();
      if (statusLinija != null && statusLinija.startsWith("OK")) {
        StringBuilder jsonBuilder = new StringBuilder();
        String linija;
        while ((linija = in.readLine()) != null) {
          jsonBuilder.append(linija);
        }
        
        String jsonPopis = jsonBuilder.toString();
        List<PartnerPopis> popis = gson.fromJson(jsonPopis, new TypeToken<List<PartnerPopis>>() {}.getType());
        
        mreznaUticnica.close();
        return popis;
      }
      
      mreznaUticnica.close();
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Šalje obračun na poslužitelj tvrtka.
   */
  private String posaljiObracunNaPosluzitelj(int partnerId, String sigurnosniKod, String jsonObracuni) {
	  try {
	    var mreznaUticnica = new Socket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad));
	    BufferedReader in =
	        new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
	    PrintWriter out =
	        new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
	    
	    out.write("OBRAČUNWS " + partnerId + " " + sigurnosniKod + "\n");
	    out.write(jsonObracuni + "\n");
	    out.flush();
	    mreznaUticnica.shutdownOutput();
	    
	    String statusLinija = in.readLine();
	    mreznaUticnica.close();
	    return statusLinija;
	    
	  } catch (IOException e) {
	  }
	  return null;
	}
}