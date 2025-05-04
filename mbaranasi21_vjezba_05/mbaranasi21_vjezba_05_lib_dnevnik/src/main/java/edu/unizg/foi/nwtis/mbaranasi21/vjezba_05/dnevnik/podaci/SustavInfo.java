package edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik.podaci;

import java.net.InetAddress;
import java.net.UnknownHostException;

public record SustavInfo(String adresaRacunala, String ipAdresaRacunala, String nazivOS,
    String proizvodacVM, String verzijaVM, String direktorijVM, String direktorijRadni,
    String direktorijKorisnik) {

  public static SustavInfo preuzmiPodatke() throws UnknownHostException {
    var p_adresaRacunala = InetAddress.getLocalHost().getHostName();
    var p_ipAdresaRacunala = InetAddress.getLocalHost().getHostAddress();
    var p_nazivOS = System.getProperty("os.name");
    var p_proizvodacVM = System.getProperty("java.vm.vendor");
    var p_verzijaVM = System.getProperty("java.vm.version");
    var p_direktorijVM = System.getProperty("java.home");
    var p_direktorijRadni = System.getProperty("java.io.tmpdir");
    var p_direktorijKorisnik = System.getProperty("user.dir");

    return new SustavInfo(p_adresaRacunala, p_ipAdresaRacunala, p_nazivOS, p_proizvodacVM,
        p_verzijaVM, p_direktorijVM, p_direktorijRadni, p_direktorijKorisnik);
  }

}
