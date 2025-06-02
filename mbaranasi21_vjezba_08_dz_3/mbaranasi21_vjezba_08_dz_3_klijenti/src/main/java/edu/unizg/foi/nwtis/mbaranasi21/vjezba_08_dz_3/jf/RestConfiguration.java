/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf;

import java.sql.Connection;
import java.sql.DriverManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 *
 * @author mbaranasi21
 */
@ApplicationScoped
public class RestConfiguration {
  @Inject
  @ConfigProperty(name = "korisnickoImeBazaPodataka")
  private String korisnickoImeBazaPodataka;
  @Inject
  @ConfigProperty(name = "lozinkaBazaPodataka")
  private String lozinkaBazaPodataka;
  @Inject
  @ConfigProperty(name = "upravljacBazaPodataka")
  private String upravljacBazaPodataka;
  @Inject
  @ConfigProperty(name = "urlBazaPodataka")
  private String urlBazaPodataka;

  public Connection dajVezu() throws Exception {
    Class.forName(this.upravljacBazaPodataka);
    var vezaBazaPodataka = DriverManager.getConnection(this.urlBazaPodataka,
        this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka);
    return vezaBazaPodataka;
  }    
}
