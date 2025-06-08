/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import java.util.List;

import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.GenericType;

/**
*
* @author NWTiS
*/
@Controller
@Path("tvrtka")
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
 @Path("pocetak")
 @View("index.jsp")
 public void pocetak() {}

 @GET
 @Path("kraj")
 @View("status.jsp")
 public void kraj() {
   var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
   this.model.put("statusOperacije", status);
   dohvatiStatuse();
 }

 @GET
 @Path("status")
 @View("status.jsp")
 public void status() {
   dohvatiStatuse();
 }

 @GET
 @Path("start/{id}")
 @View("status.jsp")
 public void startId(@PathParam("id") int id) {
   var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
   this.model.put("status", status);
   this.model.put("samoOperacija", true);
 }

 @GET
 @Path("pauza/{id}")
 @View("status.jsp")
 public void pauzatId(@PathParam("id") int id) {
   var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
   this.model.put("status", status);
   this.model.put("samoOperacija", true);
 }

 @GET
 @Path("partner")
 @View("partneri.jsp")
 public void partneri() {
   var odgovor = this.servisTvrtka.getPartneri();
   var status = odgovor.getStatus();
   if (status == 200) {
     var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {});
     this.model.put("status", status);
     this.model.put("partneri", partneri);
   }
 }

 @GET
 @Path("nadzornaKonzolaTvrtka")
 @View("nadzornaKonzolaTvrtka.jsp")
 public void nadzornaKonzolaTvrtka() {
   
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
