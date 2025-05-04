package edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik;

import java.util.List;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik.podaci.DnevnikRada;

public interface ServisDnevnikRada {
  boolean pripremiResurs() throws Exception;

  boolean otpustiResurs() throws Exception;

  boolean upisiDnevnik(DnevnikRada dnevnikRada) throws Exception;

  List<DnevnikRada> dohvatiDnevnik(long vrijemeOd, long vrijemeDo, String korisnickoIme)
      throws Exception;

  boolean koristiBazuPodataka();

  boolean koristiDatoteku();
}
