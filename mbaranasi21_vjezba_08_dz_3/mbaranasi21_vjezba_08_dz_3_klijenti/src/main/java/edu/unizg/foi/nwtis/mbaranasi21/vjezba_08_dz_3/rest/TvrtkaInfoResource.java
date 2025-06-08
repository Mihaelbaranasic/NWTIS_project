package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.rest;

import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws.WebSocketTvrtka;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * RESTful servis za primanje informacija od drugih servisa
 * i prosljeđivanje putem WebSocket-a
 */
@RequestScoped
@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {
  
  private static final Logger logger = Logger.getLogger(TvrtkaInfoResource.class.getName());
  
  /**
   * GET /kraj/info
   * Prima informaciju za kraj rada poslužitelja PoslužiteljTvrtka
   * Šalje WebSocket poruku putem WebSocketTvrtka.send()
   */
  @GET
  @Path("kraj/info")
  @Produces(MediaType.APPLICATION_JSON)
  public Response krajInfo() {
    logger.info("REST: Primljena informacija za kraj rada poslužitelja Tvrtka");
    
    try {
      String webSocketPoruka = "NE RADI;0;Poslužitelj Tvrtka se zaustavlja";
      
      WebSocketTvrtka.send(webSocketPoruka);
      
      logger.info("Poslana WebSocket poruka: " + webSocketPoruka);
      
      return Response.ok()
          .entity("{\"status\":\"success\",\"message\":\"Informacija o kraju rada je prosliješena\"}")
          .build();
          
    } catch (Exception e) {
      logger.severe("Greška pri slanju WebSocket poruke za kraj rada: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("{\"status\":\"error\",\"message\":\"Greška pri slanju informacije\"}")
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
    logger.info("REST: Primljena informacija o novom obračunu");
    
    try {
      int brojObracuna = 1;
      
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