package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Producer for injectable EntityManager
 *
 */
public abstract class EntityManagerProducer implements Serializable {

  private static final long serialVersionUID = -8963852717889659294L;

  @PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
  private EntityManager em;

  public EntityManager getEntityManager() {
    return em;
  }
}
