package edu.unizg.foi.nwtis.mbaranasi21.vjezba_03;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SkupPodatakaPretrazivanja {
  private Queue podaci = new ConcurrentLinkedQueue();

  public Queue getPodaci() {
    return this.podaci;
  }

  public boolean dodajPodatak(PodaciPretrazivanja noviPodatak) {
    return this.podaci.dodajnovi();
  }
}
