package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

/**
 * Konfiguracija sigurnosti aplikacije
 * 
 * @author mbaranasi21
 */
@ApplicationScoped
@FormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/prijavaKorisnika.xhtml", 
        errorPage = "/prijavaKorisnika.xhtml",
        useForwardToLogin = false
    )
)
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:app/jdbc/nwtis_hsqldb",
    callerQuery = "select lozinka from korisnici where korisnik = ?",
    groupsQuery = "select grupa from uloge where korisnik = ?",
    hashAlgorithm = NoPasswordHash.class
)
@DeclareRoles({"admin", "nwtis"})
public class AplikacijskaSigurnost {
}