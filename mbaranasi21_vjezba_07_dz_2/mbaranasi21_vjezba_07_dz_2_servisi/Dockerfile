FROM payara/micro:6.2025.4-jdk21

COPY target/mbaranasi21_vjezba_07_dz_2_servisi-1.0.0.war /opt/payara/deployments/app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/payara/payara-micro.jar", "--deploy", "/opt/payara/deployments/app.war", "--contextroot", ""]