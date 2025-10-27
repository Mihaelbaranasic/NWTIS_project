package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint za komunikaciju s tvrtkom
 * Krajnja točka: /ws/tvrtka
 */
@ServerEndpoint("/ws/tvrtka")
public class WebSocketTvrtka {
  
  private static final Logger logger = Logger.getLogger(WebSocketTvrtka.class.getName());
  
  private static Set<Session> sesije = Collections.synchronizedSet(new HashSet<>());

  /**
   * Poziva se kada se otvori nova WebSocket konekcija
   */
  @OnOpen
  public void onOpen(Session sesija) {
    sesije.add(sesija);
    logger.info("Nova WebSocket konekcija za tvrtku. ID sesije: " + sesija.getId());
    logger.info("Ukupno aktivnih sesija: " + sesije.size());
  }

  /**
   * Poziva se kada se zatvori WebSocket konekcija
   */
  @OnClose
  public void onClose(Session sesija) {
    sesije.remove(sesija);
    logger.info("Zatvorena WebSocket konekcija za tvrtku. ID sesije: " + sesija.getId());
    logger.info("Ukupno aktivnih sesija: " + sesije.size());
  }

  /**
   * Poziva se kada se primi poruka od klijenta
   */
  @OnMessage
  public void onMessage(String poruka, Session sesija) {
    logger.info("Primljena poruka od klijenta: " + poruka);
    
    posaljiSvimKlijentima(poruka);
  }

  /**
   * Poziva se kada dođe do greške
   */
  @OnError
  public void onError(Session sesija, Throwable greska) {
    logger.log(Level.SEVERE, "WebSocket greška za sesiju: " + sesija.getId(), greska);
  }

  /**
   * Statička metoda za slanje poruke svim povezanim klijentima
   * Poziva se iz REST servisa
   */
  public static void send(String poruka) {
    logger.info("Šalje se poruka svim klijentima: " + poruka);
    
    synchronized (sesije) {
      Set<Session> kopijaSkupa = new HashSet<>(sesije);
      
      for (Session sesija : kopijaSkupa) {
        if (sesija.isOpen()) {
          try {
            sesija.getBasicRemote().sendText(poruka);
          } catch (IOException e) {
            logger.log(Level.WARNING, "Greška pri slanju poruke klijentu: " + sesija.getId(), e);
            sesije.remove(sesija);
          }
        } else {
          sesije.remove(sesija);
        }
      }
    }
  }

  /**
   * Privatna metoda za slanje poruke svim klijentima
   */
  private void posaljiSvimKlijentima(String poruka) {
    send(poruka);
  }
  
  /**
   * Vraća broj aktivnih sesija
   */
  public static int getBrojAktivnihSesija() {
    return sesije.size();
  }
}