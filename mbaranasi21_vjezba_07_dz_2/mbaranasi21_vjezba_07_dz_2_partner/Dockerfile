FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8010
EXPOSE 8011

CMD ["/usr/app/bin/java", "-m", "mbaranasi21_vjezba_07_dz_2_partner/edu.unizg.foi.nwtis.mbaranasi21.vjezba_07_dz_2.PosluziteljPartner", "NWTiS_04_partner_1.txt", "PARTNER"]