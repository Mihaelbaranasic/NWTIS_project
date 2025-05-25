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

}