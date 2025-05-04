package edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import edu.unizg.foi.nwtis.mbaranasi21.vjezba_05.dnevnik.podaci.DnevnikRada;

public class CsvDatotekaDnevnik implements ServisDnevnikRada {
  public enum Stupci {vrijeme, korisnickoIme, adresaRacunala, ipAdresaRacunala, nazivOS, verzijaVM, opisRada};
  private String nazivDatoteke;
  private boolean obrisatiDnevnik;
  private CSVFormat csvFormat;
  
  public CsvDatotekaDnevnik() {    
  }
  
  public CsvDatotekaDnevnik(String nazivDatoteke, boolean obrisatiDnevnik) {
    this.nazivDatoteke = nazivDatoteke;
    this.obrisatiDnevnik = obrisatiDnevnik;
  }
  
  @Override
  public boolean pripremiResurs() throws Exception {   
    this.csvFormat = CSVFormat.EXCEL.builder()
        .setDelimiter(';')
        .setHeader(CsvDatotekaDnevnik.Stupci.class)
        .setSkipHeaderRecord(true).get();

    return true;
  }

  @Override
  public boolean otpustiResurs() throws Exception {
    this.csvFormat = null;
    return true;
  }

  @Override
  public boolean upisiDnevnik(DnevnikRada dnevnikRada) throws Exception {
    var putanja = Path.of(this.nazivDatoteke);
    if(this.obrisatiDnevnik) {
      Files.delete(putanja);
    }
    var nemaDatoteke = Files.exists(putanja);
    Writer out = new FileWriter(this.nazivDatoteke, Charset.forName("UTF-8"), true);
    CSVPrinter printer = new CSVPrinter(out, this.csvFormat);
    if(! nemaDatoteke) {
      printer.printRecord((Object[]) CsvDatotekaDnevnik.Stupci.values());
    }
    printer.printRecord(dnevnikRada.vrijeme(), dnevnikRada.korisnickoIme(), dnevnikRada.adresaRacunala(), dnevnikRada.ipAdresaRacunala(), dnevnikRada.nazivOS(), dnevnikRada.verzijaVM(), dnevnikRada.opisRada());
    printer.close();
    return true;
  }

  @Override
  public List<DnevnikRada> dohvatiDnevnik(long vrijemeOd, long vrijemeDo, String kIme)
      throws Exception {
    Reader in = new FileReader(this.nazivDatoteke);
    var dnevnici = new ArrayList<DnevnikRada>();
    Iterable<CSVRecord> zapisi = this.csvFormat.parse(in);
    for (CSVRecord zapis : zapisi) {
        long vrijeme = Long.parseLong(zapis.get(CsvDatotekaDnevnik.Stupci.vrijeme));
        String korisnickoIme = zapis.get(CsvDatotekaDnevnik.Stupci.korisnickoIme);
        if(vrijemeOd >= vrijeme && vrijemeDo <= vrijeme && kIme.equals(korisnickoIme)) {
          var dnevnik = new DnevnikRada(vrijeme, korisnickoIme, zapis.get(CsvDatotekaDnevnik.Stupci.adresaRacunala), zapis.get(CsvDatotekaDnevnik.Stupci.ipAdresaRacunala), zapis.get(CsvDatotekaDnevnik.Stupci.nazivOS), zapis.get(CsvDatotekaDnevnik.Stupci.verzijaVM), zapis.get(CsvDatotekaDnevnik.Stupci.opisRada));
          dnevnici.add(dnevnik);
        }
    }
    return dnevnici;
  }

  @Override
  public boolean koristiDatoteku() {
    return true;
  }

  @Override
  public boolean koristiBazuPodataka() {
    return false;
  }

}
