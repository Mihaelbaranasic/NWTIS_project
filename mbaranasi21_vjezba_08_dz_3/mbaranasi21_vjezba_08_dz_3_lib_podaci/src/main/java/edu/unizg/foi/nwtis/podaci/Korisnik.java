package edu.unizg.foi.nwtis.podaci;

public record Korisnik (String korisnik, String lozinka, String prezime, String ime, String email) {
  public Korisnik korisnikBezLozinke() {
    return new Korisnik(korisnik, "******", prezime, ime, email);
  }

}
