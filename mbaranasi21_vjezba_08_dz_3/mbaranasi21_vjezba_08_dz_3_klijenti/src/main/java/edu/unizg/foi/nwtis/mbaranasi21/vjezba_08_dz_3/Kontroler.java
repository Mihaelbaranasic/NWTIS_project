/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 *
 * @author NWTiS
 */
@Controller
@Path("")
@RequestScoped
public class Kontroler {

  @Inject
  private Models model;

  @Inject
  private BindingResult bindingResult;

  @GET
  @Path("pocetak")
  @View("index.jsp")
  public void pocetak() {}

//  @GET
//  @Path("noviKorisnik")
//  @View("noviKorisnik.jsp")
//  public void noviKorisnik() {}
//
//  @GET
//  @Path("ispisKorisnika")
//  @View("korisnici.jsp")
//  public void json() {
//    RestKlijentKorisnici k = new RestKlijentKorisnici();
//    List<Korisnik> korisnici = k.getKorisniciJSON();
//    model.put("korisnici", korisnici);
//  }
//
//  @POST
//  @Path("pretrazivanjeKorisnika")
//  @View("korisnici.jsp")
//  public void json_pi(@FormParam("prezime") String prezime, @FormParam("ime") String ime) {
//    RestKlijentKorisnici k = new RestKlijentKorisnici();
//    List<Korisnik> korisnici;
//    if ((ime == null || ime.length() == 0) && (prezime == null || prezime.length() == 0)) {
//      korisnici = k.getKorisniciJSON(prezime, ime);
//    } else if (ime == null || ime.length() == 0) {
//      korisnici = k.getKorisniciJSON(prezime, "%");
//    } else if (prezime == null || prezime.length() == 0) {
//      korisnici = k.getKorisniciJSON("%", ime);
//    } else {
//      korisnici = k.getKorisniciJSON(prezime, ime);
//    }
//    model.put("korisnici", korisnici);
//  }
//
//  @POST
//  @Path("dodajKorisnika")
//  public String json_id(@MvcBinding @FormParam("korisnik") String korId,
//      @FormParam("lozinka") String lozinka, @FormParam("prezime") String prezime,
//      @FormParam("ime") String ime, @FormParam("email") String email) {
//    if (bindingResult.isFailed() || korId == null || korId.trim().length() == 0 || prezime == null
//        || lozinka.trim().length() == 0 || lozinka == null || prezime.trim().length() == 0
//        || ime == null || ime.trim().length() == 0 || email == null || email.trim().length() == 0) {
//      model.put("poruka", "Nisu upisani potrebni podaci.");
//      model.put("pogreska", true);
//      if (korId != null) {
//        model.put("korId", korId);
//      } else {
//        model.put("korId", "");
//      }
//      model.put("lozinka", "");
//      if (prezime != null) {
//        model.put("prezime", prezime);
//      } else {
//        model.put("prezime", "");
//      }
//      if (prezime != null) {
//        model.put("ime", ime);
//      } else {
//        model.put("ime", "");
//      }
//      if (email != null) {
//        model.put("email", email);
//      } else {
//        model.put("email", "");
//      }
//      return "noviKorisnik.jsp";
//    }
//    var korisnik = new Korisnik(korId, lozinka, prezime, ime, email, null, null);
//    RestKlijentKorisnici k = new RestKlijentKorisnici();
//    var odgovor = k.postKorisnikJSON(korisnik);
//    if (odgovor) {
//      model.put("poruka",
//          "Uspješno dodan korisnik: " + korisnik.getIme() + " " + korisnik.getPrezime());
//    } else {
//      model.put("poruka", "Problem kod upisa korisnika.");
//      model.put("pogreska", true);
//    }
//    return "noviKorisnik.jsp";
//  }
}
