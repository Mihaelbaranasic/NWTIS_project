package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint za komunikaciju s partnerima
 * Krajnja točka: /ws/partneri
 */
@ServerEndpoint("/ws/partneri")
public class WebSocketPartneri {
  
  private static final Logger logger = Logger.getLogger(WebSocketPartneri.class.getName());
  
  private static Set<Session> sesije = Collections.synchronizedSet(new HashSet<>());

  /**
   * Statička metoda za slanje poruke svim povezanim klijentima
   * Poziva se iz REST servisa
   *
   * @param poruka poruka za slanje
   */
  public static void send(String poruka) {
    logger.info("Šalje se poruka svim klijentima partnera: " + poruka);
    
    synchronized (sesije) {
      Set<Session> kopijaSkupa = new HashSet<>(sesije);
      
      for (Session sesija : kopijaSkupa) {
        if (sesija.isOpen()) {
          try {
            sesija.getBasicRemote().sendText(poruka);
          } catch (IOException e) {
            logger.log(Level.WARNING, "Greška pri slanju poruke klijentu partnera: " + sesija.getId(), e);
            sesije.remove(sesija);
          }
        } else {
          sesije.remove(sesija);
        }
      }
    }
  }

  /**
   * Poziva se kada se otvori nova WebSocket konekcija
   *
   * @param session WebSocket sesija
   * @param conf konfiguracija endpoint-a
   */
  @OnOpen
  public void openConnection(Session session, EndpointConfig conf) {
    sesije.add(session);
    logger.info("Nova WebSocket konekcija za partnere. ID sesije: " + session.getId());
    logger.info("Ukupno aktivnih sesija partnera: " + sesije.size());
  }

  /**
   * Poziva se kada se zatvori WebSocket konekcija
   *
   * @param session WebSocket sesija
   * @param reason razlog zatvaranja
   */
  @OnClose
  public void closedConnection(Session session, CloseReason reason) {
    sesije.remove(session);
    logger.info("Zatvorena WebSocket konekcija za partnere. ID sesije: " + session.getId());
    logger.info("Razlog zatvaranja: " + reason.getReasonPhrase());
    logger.info("Ukupno aktivnih sesija partnera: " + sesije.size());
  }

  /**
   * Poziva se kada se primi poruka od klijenta
   *
   * @param session WebSocket sesija
   * @param poruka primljena poruka
   */
  @OnMessage
  public void Message(Session session, String poruka) {
    logger.info("Primljena poruka od klijenta partnera: " + poruka);
    send(poruka);
  }

  /**
   * Poziva se kada dođe do greške
   *
   * @param session WebSocket sesija
   * @param t greška koja se dogodila
   */
  @OnError
  public void error(Session session, Throwable t) {
    sesije.remove(session);
    logger.log(Level.SEVERE, "WebSocket greška za sesiju partnera: " + session.getId(), t);
  }

  /**
   * Vraća broj aktivnih sesija partnera
   *
   * @return broj aktivnih sesija
   */
  public static int getBrojAktivnihSesija() {
    return sesije.size();
  }

  /**
   * Čisti neaktivne sesije iz skupa
   */
  public static void ocistiNeaktivneSesije() {
    synchronized (sesije) {
      sesije.removeIf(sesija -> !sesija.isOpen());
    }
    logger.info("Očišćene neaktivne sesije partnera. Aktivnih sesija: " + sesije.size());
  }
}