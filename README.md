# FOLT — Multi-Server Food & Beverage Delivery System

## Overview
FOLT is a multi-tier food and beverage delivery system built progressively over a semester coursework series ("Advanced Web Technologies and Services", FOI Varaždin). It started as a raw socket-based client/server system and evolved into a full Jakarta EE web application with REST services, JPA persistence, WebSockets, and session-based security.

## Architecture
- **PosluziteljTvrtka** — the central company server. Manages registered partners, their menus (Jelovnik), drink lists (KartaPica), and billing (Obracun). Runs three internal listeners: shutdown, partner registration, and partner request handling.
- **PosluziteljPartner** — represents a restaurant/partner. Registers with the company server, serves customer orders, and can request a graceful shutdown.
- **KorisnikKupac** — a customer client that reads commands from a CSV file and sends order requests over sockets.
- Communication uses a custom text-based socket protocol with defined OK/ERROR response codes.

## Evolution across the coursework
1. **Vježba 1–3:** Configuration file parsing (JSON/XML/binary/TXT) via a shared `Konfiguracija` abstraction
2. **Vježba 4:** Core socket-based client/server implementation (Tvrtka/Partner/Kupac)
3. **Vježba 5:** Shared logging library (dnevnik)
4. **Vježba 6:** Docker containerization of the server components
5. **Vježba 7:** REST services (Jakarta REST + MicroProfile Config), H2/HSQLDB databases via Docker Compose
6. **Vježba 8 (final):** Full Jakarta Faces (JSF) + Jakarta MVC web client, JPA persistence (Grupe/Korisnici/Obracuni/Partneri/Zapisi entities), WebSocket live updates, session-based login and an admin console for managing users, partners, and transaction logs

## How to run
Each server takes a single argument — the path to a configuration file (key/value settings: host, port, shutdown code, admin code, etc.):
```bash
java -cp target/classes edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.PosluziteljTvrtka config-tvrtka.txt
```
For the Dockerized/database-backed versions (vježba 7 onward):
```bash
docker compose -f compose.yaml up
```
This launches the configured H2 or HSQLDB instance alongside the service containers. SQL schema scripts are provided under `Scripts/` and `tablice_*.sql`.

The final web application (vježba 8) needs a Jakarta EE server (Payara) and is deployed as multiple WAR/module artifacts (`_klijenti`, `_servisi`, `_partner`, `_tvrtka`).

## Author
Mihael Baranašić — Advanced Web Technologies and Services course, FOI Varaždin.
