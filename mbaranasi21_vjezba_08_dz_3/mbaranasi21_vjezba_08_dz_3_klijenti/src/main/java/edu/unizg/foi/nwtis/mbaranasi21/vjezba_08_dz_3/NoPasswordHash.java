package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import java.util.Map;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * Password hash implementacija koja ne hash-ira lozinke
 * Koristi se za lozinke koje su već u plain text formatu u bazi
 * 
 * @author mbaranasi21
 */
public class NoPasswordHash implements Pbkdf2PasswordHash {

    @Override
    public String generate(char[] password) {
        // ISPRAVKA: new String(password) umjesto password.toString()
        return new String(password);
    }

    @Override
    public boolean verify(char[] password, String hashedPassword) {
        // ISPRAVKA: new String(password) umjesto password.toString()
        String npassword = new String(password);
        return npassword.trim().equals(hashedPassword.trim());
    }

    @Override
    public void initialize(Map<String, String> parameters) {
        // Prazno - nije potrebno za plain text lozinke
    }
}