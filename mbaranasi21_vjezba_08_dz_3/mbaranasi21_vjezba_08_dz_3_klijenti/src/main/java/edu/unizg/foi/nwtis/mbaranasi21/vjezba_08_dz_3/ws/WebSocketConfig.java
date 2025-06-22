package edu.unizg.foi.nwtis.mbaranasi21.vjezba_08_dz_3.ws;

import java.util.HashSet;
import java.util.Set;

import jakarta.websocket.Endpoint;
import jakarta.websocket.server.ServerApplicationConfig;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Konfiguracija za WebSocket servere
 */
public class WebSocketConfig implements ServerApplicationConfig {

    /**
     * Vraća skup endpoint klasa koje treba registrirati
     *
     * @param scanned skenirane klase
     * @return skup endpoint klasa
     */
    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        Set<Class<?>> endpoints = new HashSet<>();
        endpoints.add(WebSocketTvrtka.class);
        endpoints.add(WebSocketPartneri.class);
        return endpoints;
    }

    /**
     * Vraća skup programskih endpoint konfiguracija
     *
     * @param endpointClasses klase endpoint-ova
     * @return skup konfiguracija
     */
    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        return new HashSet<>();
    }
}