package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import jakarta.enterprise.context.ApplicationScoped;
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
    
    // Ukupan broj primljenih obračuna
    private int brojObracuna = 0;
    
    // Broj otvorenih narudžbi pojedinog partnera
    // Key: ID partnera, Value: broj otvorenih narudžbi
    private Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
    
    // Broj plaćenih računa pojedinog partnera  
    // Key: ID partnera, Value: broj plaćenih računa
    private Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();

    /**
     * Dohvaća ukupan broj primljenih obračuna
     * @return broj obračuna
     */
    public int getBrojObracuna() {
        return brojObracuna;
    }

    /**
     * Postavlja ukupan broj primljenih obračuna
     * @param brojObracuna broj obračuna
     */
    public void setBrojObracuna(int brojObracuna) {
        this.brojObracuna = brojObracuna;
    }

    /**
     * Dohvaća mapu otvorenih narudžbi po partnerima
     * @return mapa otvorenih narudžbi
     */
    public Map<Integer, Integer> getBrojOtvorenihNarudzbi() {
        return brojOtvorenihNarudzbi;
    }

    /**
     * Postavlja mapu otvorenih narudžbi po partnerima
     * @param brojOtvorenihNarudzbi mapa otvorenih narudžbi
     */
    public void setBrojOtvorenihNarudzbi(Map<Integer, Integer> brojOtvorenihNarudzbi) {
        this.brojOtvorenihNarudzbi = brojOtvorenihNarudzbi;
    }

    /**
     * Dohvaća mapu plaćenih računa po partnerima
     * @return mapa plaćenih računa
     */
    public Map<Integer, Integer> getBrojRacuna() {
        return brojRacuna;
    }

    /**
     * Postavlja mapu plaćenih računa po partnerima
     * @param brojRacuna mapa plaćenih računa
     */
    public void setBrojRacuna(Map<Integer, Integer> brojRacuna) {
        this.brojRacuna = brojRacuna;
    }

    /**
     * Povećava broj obračuna za 1
     */
    public synchronized void povecajBrojObracuna() {
        this.brojObracuna++;
    }

    /**
     * Dohvaća broj otvorenih narudžbi za određenog partnera
     * @param partnerId ID partnera
     * @return broj otvorenih narudžbi
     */
    public int getBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        return brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
    }

    /**
     * Povećava broj otvorenih narudžbi za određenog partnera za 1
     * @param partnerId ID partnera
     */
    public synchronized void povecajBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        int trenutniBroj = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
        brojOtvorenihNarudzbi.put(partnerId, trenutniBroj + 1);
    }

    /**
     * Smanjuje broj otvorenih narudžbi za određenog partnera za 1
     * @param partnerId ID partnera
     */
    public synchronized void smaniBrojOtvorenihNarudzbiPartnera(Integer partnerId) {
        int trenutniBroj = brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
        if (trenutniBroj > 0) {
            brojOtvorenihNarudzbi.put(partnerId, trenutniBroj - 1);
        }
    }

    /**
     * Dohvaća broj plaćenih računa za određenog partnera
     * @param partnerId ID partnera
     * @return broj plaćenih računa
     */
    public int getBrojRacunaPartnera(Integer partnerId) {
        return brojRacuna.getOrDefault(partnerId, 0);
    }

    /**
     * Povećava broj plaćenih računa za određenog partnera za 1
     * @param partnerId ID partnera
     */
    public synchronized void povecajBrojRacunaPartnera(Integer partnerId) {
        int trenutniBroj = brojRacuna.getOrDefault(partnerId, 0);
        brojRacuna.put(partnerId, trenutniBroj + 1);
    }
}