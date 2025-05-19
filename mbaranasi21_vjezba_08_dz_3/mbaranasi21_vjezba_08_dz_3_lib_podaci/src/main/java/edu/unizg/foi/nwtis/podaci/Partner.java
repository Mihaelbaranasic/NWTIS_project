package edu.unizg.foi.nwtis.podaci;

public record Partner(int id, String naziv, String vrstaKuhinje, String adresa, int mreznaVrata, int mreznaVrataKraj, float gpsSirina, float gpsDuzina, String sigurnosniKod, String adminKod) {
  public Partner partnerBezKodova() {
    return new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, "******", "******");
  }
}
