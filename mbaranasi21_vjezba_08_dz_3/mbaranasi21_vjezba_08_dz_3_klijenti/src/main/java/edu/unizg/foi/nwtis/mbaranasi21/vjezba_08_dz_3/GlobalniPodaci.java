package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import jakarta.enterprise.context.ApplicationScoped;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws.WebSocketPartneri;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;

/**
 * Klasa za pohranu globalnih podataka aplikacije
 * 
 * @author mbaranasi21
 */
@ApplicationScoped
public class GlobalniPodaci implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private int brojObracuna = 0;
    private Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
    private Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();

    public int getBrojObracuna() {
        return brojObracuna;
    }

    public void setBrojObracuna(int brojObracuna) {
        this.brojObracuna = brojObracuna;
    }

    public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
        return brojOtvorenihNarudzbi;
    }

    public void setBrojOtvorenihNarudzbi(Map<Integer, Integer> brojOtvorenihNarudzbi) {
        this.brojOtvorenihNarudzbi = brojOtvorenihNarudzbi;
    }

    public Map<Integer, Integer> getBrojRacuna() {
        return brojRacuna;
    }

    public void setBrojRacuna(Map<Integer, Integer> brojRacuna) {
        this.brojRacuna = brojRacuna;
    }

    public synchronized void povecajBrojObracuna() {
        this.brojObracuna++;
        posaljiWebSocketPorukuTvrtka();
    }

    public int getBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        return brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    }

    public synchronized void povecajBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        int trenutniBroj = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
        brojOtvorenihNarudzbi.put(partnerId, trenutniBroj + 1);
        posaljiWebSocketPorukuPartneri();
    }

    public synchronized void smaniBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        int trenutniBroj = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
        if (trenutniBroj > 0) {
            brojOtvorenihNarudzbi.put(partnerId, trenutniBroj - 1);
        }
        posaljiWebSocketPorukuPartneri();
    }

    public int getBrojRacunaPartnera(Integer partnerId) {
        return brojRacuna.getOrDefault(partnerId, 0);
    }

    public synchronized void povecajBrojRacunaPartnera(Integer partnerId) {
        int trenutniBroj = brojRacuna.getOrDefault(partnerId, 0);
        brojRacuna.put(partnerId, trenutniBroj + 1);
        posaljiWebSocketPorukuPartneri();
    }

    /**
     * Šalje WebSocket poruku za Tvrtka konzolu
     * Format: "RADI/NE RADI;brojObracuna;internaPoruka"
     */
    private void posaljiWebSocketPorukuTvrtka() {
        try {
            String status = "RADI";
            String poruka = status + ";" + brojObracuna + ";";
            edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws.WebSocketTvrtka.send(poruka);
        } catch (Exception e) {
            System.err.println("Greška pri slanju WebSocket poruke Tvrtka: " + e.getMessage());
        }
    }

    /**
     * Šalje WebSocket poruku za Partner konzolu
     * Format: "RADI/NE RADI;brojOtvorenihNarudzbi;brojRacuna"
     */
    private void posaljiWebSocketPorukuPartneri() {
        try {
            String status = "RADI";
            int ukupnoOtvorenih = brojOtvorenihNarudzbi.values().stream().mapToInt(Integer::intValue).sum();
            int ukupnoRacuna = brojRacuna.values().stream().mapToInt(Integer::intValue).sum();
            String poruka = status + ";" + ukupnoOtvorenih + ";" + ukupnoRacuna;
            WebSocketPartneri.send(poruka);
        } catch (Exception e) {
            System.err.println("Greška pri slanju WebSocket poruke Partneri: " + e.getMessage());
        }
    }
}