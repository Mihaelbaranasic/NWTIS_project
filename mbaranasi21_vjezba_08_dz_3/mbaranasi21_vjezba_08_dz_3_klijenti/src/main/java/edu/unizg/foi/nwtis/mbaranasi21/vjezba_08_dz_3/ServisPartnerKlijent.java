package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * REST klijent za komunikaciju s Partner servisom
 * 
 * @author mbaranasi21
 */
@RegisterRestClient(configKey = "klijentPartner")
@Path("api/partner")
public interface ServisPartnerKlijent {

    @HEAD
    Response headPosluzitelj();

    @POST
    @Path("korisnik")
    Response postKorisnik(Korisnik korisnik);

    @GET
    @Path("korisnik")
    Response getKorisnici();

    @GET
    @Path("korisnik/{id}")
    Response getKorisnik(@PathParam("id") int id);

    @GET
    @Path("jelovnik")
    Response getJelovnik();

    @GET
    @Path("kartapica")
    Response getKartaPica();

    @POST
    @Path("narudzba")
    Response postNarudzba(Narudzba narudzba);

    @GET
    @Path("narudzba")
    Response getNarudzba();

    @POST
    @Path("jelo")
    Response postJelo(@FormParam("jeloId") int jeloId, 
                      @FormParam("kolicina") int kolicina);

    @POST
    @Path("pice")
    Response postPice(@FormParam("piceId") int piceId, 
                      @FormParam("kolicina") int kolicina);

    @POST
    @Path("racun")
    Response postRacun();

    @GET
    @Path("spava")
    Response getSpavanje(@QueryParam("vrijeme") int vrijeme);

    @HEAD
    @Path("status/{id}")
    Response headPosluziteljStatus(@PathParam("id") int id);

    @HEAD
    @Path("pauza/{id}")
    Response headPosluziteljPauza(@PathParam("id") int id);

    @HEAD
    @Path("start/{id}")
    Response headPosluziteljStart(@PathParam("id") int id);

    @HEAD
    @Path("kraj")
    Response headPosluziteljKraj();
}