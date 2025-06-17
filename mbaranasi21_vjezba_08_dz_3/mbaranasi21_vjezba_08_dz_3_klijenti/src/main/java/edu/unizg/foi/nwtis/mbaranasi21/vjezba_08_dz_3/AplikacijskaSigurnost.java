/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

/**
 *
 * @author dkermek
 */
@ApplicationScoped
@FormAuthenticationMechanismDefinition(loginToContinue = @LoginToContinue(
    loginPage = "/prijavaKorisnika.xhtml", errorPage = "/prijavaKorisnikaPogreska.xhtml"))
@DatabaseIdentityStoreDefinition(dataSourceLookup = "java:app/jdbc/nwtis_hsqldb",
    callerQuery = "select lozinka from korisnici where korisnik = ?",
    groupsQuery = "select grupa from uloge where korisnik = ?",
    hashAlgorithm = NoPasswordHash.class)
@DeclareRoles({"admin", "nwtis"})
public class AplikacijskaSigurnost {
}
