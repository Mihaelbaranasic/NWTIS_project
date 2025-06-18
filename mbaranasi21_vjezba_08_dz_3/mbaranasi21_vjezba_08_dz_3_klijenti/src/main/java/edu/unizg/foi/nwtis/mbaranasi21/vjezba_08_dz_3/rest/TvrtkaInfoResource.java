package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.rest;

import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws.WebSocketTvrtka;
import edu.unizg.foi.nwtis.podaci.Obracun;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * RESTful servis za primanje informacija od drugih servisa
 * i prosljeđivanje putem WebSocket-a
 * 
 * @author mbaranasi21
 */
@RequestScoped
@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {
  
  private static final Logger logger = Logger.getLogger(TvrtkaInfoResource.class.getName());
  
  @Inject
  private GlobalniPodaci globalniPodaci;
  
  /**
   * HEAD /kraj/info
   * Prima informaciju za kraj rada poslužitelja PoslužiteljTvrtka i svih njegovih dijelova
   * Šalje WebSocket poruku putem WebSocketTvrtka.send()
   */
  @HEAD
  @Path("kraj/info")
  public Response krajInfo() {
    logger.info("REST HEAD: Primljena informacija za kraj rada poslužitelja Tvrtka");
    
    try {
      String webSocketPoruka = "NE RADI;" + globalniPodaci.getBrojObracuna() + ";Poslužitelj Tvrtka se zaustavlja";
      
      WebSocketTvrtka.send(webSocketPoruka);
      
      logger.info("Poslana WebSocket poruka: " + webSocketPoruka);
      
      return Response.ok().build();
          
    } catch (Exception e) {
      logger.severe("Greška pri slanju WebSocket poruke za kraj rada: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * POST /obracun
   * Prima obračun od poslužitelja PoslužiteljTvrtka i dodaje novi obračun
   * Ako je bilo uspješno šalje WebSocket poruku
   */
  @POST
  @Path("obracun")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response dodajObracun(Obracun obracun) {
    logger.info("REST POST: Primljen novi obračun");
    
    try {
      // Ovdje bi trebalo dodati obračun u bazu podataka
      // Zasad samo povećavamo brojač
      globalniPodaci.povecajBrojObracuna();
      
      int brojObracuna = globalniPodaci.getBrojObracuna();
      
      String webSocketPoruka = "RADI;" + brojObracuna + ";Novi obračun je dodan";
      
      WebSocketTvrtka.send(webSocketPoruka);
      
      logger.info("Dodan obračun. Ukupno obračuna: " + brojObracuna);
      logger.info("Poslana WebSocket poruka: " + webSocketPoruka);
      
      return Response.status(Response.Status.CREATED)
          .entity("{\"status\":\"success\",\"message\":\"Obračun je uspješno dodan\",\"brojObracuna\":" + brojObracuna + "}")
          .build();
          
    } catch (Exception e) {
      logger.severe("Greška pri dodavanju obračuna: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"status\":\"error\",\"message\":\"Greška pri dodavanju obračuna\"}")
          .build();
    }
  }
  
  /**
   * GET /obracun/ws
   * Prima informaciju da je stigao novi obračun
   * Povećava broj primljenih obračuna u GlobalniPodaci
   * Šalje WebSocket poruku putem WebSocketTvrtka.send()
   */
  @GET
  @Path("obracun/ws")
  @Produces(MediaType.APPLICATION_JSON)
  public Response obracunWs() {
    logger.info("REST GET: Primljena informacija o novom obračunu");
    
    try {
      globalniPodaci.povecajBrojObracuna();
      int brojObracuna = globalniPodaci.getBrojObracuna();
      
      String webSocketPoruka = "RADI;" + brojObracuna + ";Novi obračun je primljen";
      
      WebSocketTvrtka.send(webSocketPoruka);
      
      logger.info("Poslana WebSocket poruka: " + webSocketPoruka);
      
      return Response.ok()
          .entity("{\"status\":\"success\",\"message\":\"Obračun je obrađen\",\"brojObracuna\":" + brojObracuna + "}")
          .build();
          
    } catch (Exception e) {
      logger.severe("Greška pri obradi obračuna: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"status\":\"error\",\"message\":\"Greška pri obradi obračuna\"}")
          .build();
    }
  }
}