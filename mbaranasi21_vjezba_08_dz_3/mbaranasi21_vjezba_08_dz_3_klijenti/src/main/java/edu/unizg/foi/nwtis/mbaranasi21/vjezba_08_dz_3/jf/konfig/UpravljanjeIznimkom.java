package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.jf.konfig;

import java.io.IOException;
import java.util.Iterator;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;

public class UpravljanjeIznimkom extends ExceptionHandlerWrapper {

  private final ExceptionHandler wrapped;

  public UpravljanjeIznimkom(ExceptionHandler wrapped) {
    super(wrapped);
    this.wrapped = wrapped;
  }

  @Override
  public ExceptionHandler getWrapped() {
    return wrapped;
  }

  @Override
  public void handle() throws FacesException {
    for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i
        .hasNext();) {
      ExceptionQueuedEvent event = i.next();
      Throwable t = event.getContext().getException();

      if (t instanceof ViewExpiredException) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        try {
          ec.redirect(ec.getRequestContextPath() + "/index.xhtml");
          fc.responseComplete();
          i.remove();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    wrapped.handle();
  }
}
