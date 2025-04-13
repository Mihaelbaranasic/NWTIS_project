package edu.unizg.foi.nwtis.mbaranasi21.vjezba_04_dz_1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class KorisnikKupac {
    /** Konfiguracijski podaci */
    private Konfiguracija konfig;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Broj argumenata nije 2. Očekivano: datoteka_konfiguracije.txt datoteka_podataka.csv");
            return;
        }
        
        String nazivDatoteke = args[0];
        String datotekaPodataka = args[1];
        
        KorisnikKupac program = new KorisnikKupac();
        program.pokreni(nazivDatoteke, datotekaPodataka);
    }
    
    /**
     * Pokreće klijenta kupca s učitanom konfiguracijom i podacima.
     * 
     * @param nazivDatoteke naziv konfiguracijske datoteke
     * @param datotekaPodataka naziv datoteke s komandama
     */
    private void pokreni(String nazivDatoteke, String datotekaPodataka) {
        if (!ucitajKonfiguraciju(nazivDatoteke)) {
            System.out.println("Greška pri učitavanju konfiguracije.");
            return;
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(datotekaPodataka));
            
            String linija;
            while ((linija = reader.readLine()) != null) {
                try {
                    String[] dijelovi = linija.split(";");
                    if (dijelovi.length != 5) {
                        System.out.println("Nevažeći format linije: " + linija);
                        continue;
                    }
                    
                    String korisnik = dijelovi[0];
                    String adresa = dijelovi[1];
                    int mreznaVrata = Integer.parseInt(dijelovi[2]);
                    int spavanje = Integer.parseInt(dijelovi[3]);
                    String komanda = dijelovi[4];
                    
                    Thread.sleep(spavanje);
                    
                    posaljiKomandu(adresa, mreznaVrata, komanda);
                    
                } catch (Exception e) {
                    System.out.println("Greška pri obradi linije: " + linija);
                    e.printStackTrace();
                }
            }
            
            reader.close();
            
        } catch (IOException e) {
            System.out.println("Greška pri čitanju datoteke s komandama: " + e.getMessage());
        }
    }
    
    /**
     * Učitava konfiguraciju iz datoteke.
     * 
     * @param nazivDatoteke naziv konfiguracijske datoteke
     * @return true ako je učitavanje uspjelo, inače false
     */
    private boolean ucitajKonfiguraciju(String nazivDatoteke) {
        try {
            this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            return true;
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    /**
     * Šalje komandu na određenu adresu i port.
     * 
     * @param adresa adresa poslužitelja
     * @param mreznaVrata port poslužitelja
     * @param komanda komanda za slanje
     */
    private void posaljiKomandu(String adresa, int mreznaVrata, String komanda) {
        try {
            Socket socket = new Socket(adresa, mreznaVrata);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"));
            
            out.write(komanda + "\n");
            out.flush();
            
            String odgovor = in.readLine();
            System.out.println("Komanda: " + komanda);
            System.out.println("Odgovor: " + odgovor);
            
            if (odgovor != null && odgovor.equals("OK") && 
                (komanda.startsWith("JELOVNIK") || komanda.startsWith("KARTAPIĆA") || komanda.startsWith("RAČUN") || komanda.startsWith("POPIS"))) {
                
                StringBuilder jsonBuilder = new StringBuilder();
                String red;
                while ((red = in.readLine()) != null) {
                    jsonBuilder.append(red);
                    System.out.println(red);
                }
            }
            
            socket.close();
            
        } catch (IOException e) {
            System.out.println("Greška pri slanju komande: " + e.getMessage());
        }
    }
}