# NWTIS_project — Advanced Web Technologies and Services Coursework

## Overview
A series of weekly exercises building a distributed restaurant-ordering system ("Tvrtka" company server + "Partner" restaurant servers + "Kupac" customer clients), progressing from raw file/socket handling to a full Jakarta EE web application with JPA persistence, REST services, WebSockets, and session-based authentication.

## Progression
- **Vježba 1–3:** Config file parsing (JSON/XML/binary/TXT), socket-based data exchange
- **Vježba 4–6:** Multi-threaded client/server architecture (Partner ↔ Tvrtka), Docker containerization
- **Vježba 7:** REST services (Jakarta REST/MicroProfile), Dockerized H2/HSQLDB databases
- **Vježba 8 (final, DZ3):** Full JSF (Jakarta Faces) web client — JPA entities, REST + WebSocket integration, login/session security, admin console for managing users, partners, and transaction logs

## How to run (final assignment — vježba_08_dz_3)
This is a multi-module Jakarta EE application requiring an application server (Payara) and a database (H2 or HSQLDB, provided via Docker):
```bash
cd mbaranasi21_vjezba_08_dz_3
docker compose -f Dockerfile.h2 up   # or Dockerfile.hsql
```
Then build and deploy the `_klijenti` (web client), `_servisi` (REST services), `_partner` and `_tvrtka` modules to a Payara server, or run each module's Docker image. Database schema scripts are in `Scripts/` and `tablice_*.sql`.

Earlier exercises (vježba_01–06) are standalone Maven modules runnable directly via `mvn exec:java` or `java -cp target/classes <MainClass>`.

## Note
This repository documents weekly coursework rather than a single deployable product — later weeks supersede earlier ones in scope and architecture.

## Author
Mihael Baranašić — Advanced Web Technologies and Services course, FOI Varaždin.
