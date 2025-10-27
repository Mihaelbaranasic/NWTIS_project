package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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
    @Consumes(MediaType.APPLICATION_JSON)
    Response postKorisnik(Korisnik korisnik);

    @GET
    @Path("korisnik")
    Response getKorisnici();

    @GET
    @Path("korisnik/{id}")
    Response getKorisnik(@PathParam("id") String id);

    @GET
    @Path("jelovnik")
    Response getJelovnik(@HeaderParam("korisnik") String korisnik,
                        @HeaderParam("lozinka") String lozinka);

    @GET
    @Path("kartapica")
    Response getKartaPica(@HeaderParam("korisnik") String korisnik,
                         @HeaderParam("lozinka") String lozinka);

    @POST
    @Path("narudzba")
    @Consumes(MediaType.APPLICATION_JSON)
    Response postNarudzba(@HeaderParam("korisnik") String korisnik,
                         @HeaderParam("lozinka") String lozinka,
                         Narudzba narudzba);

    @GET
    @Path("narudzba")
    Response getNarudzba(@HeaderParam("korisnik") String korisnik,
                        @HeaderParam("lozinka") String lozinka);

    @POST
    @Path("jelo")
    @Consumes(MediaType.APPLICATION_JSON)
    Response postJelo(@HeaderParam("korisnik") String korisnik,
                     @HeaderParam("lozinka") String lozinka,
                     Narudzba narudzba);

    @POST
    @Path("pice")
    @Consumes(MediaType.APPLICATION_JSON)
    Response postPice(@HeaderParam("korisnik") String korisnik,
                     @HeaderParam("lozinka") String lozinka,
                     Narudzba narudzba);

    @POST
    @Path("racun")
    Response postRacun(@HeaderParam("korisnik") String korisnik,
                      @HeaderParam("lozinka") String lozinka);

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