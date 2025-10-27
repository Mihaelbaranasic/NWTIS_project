<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Vježba 8 - zadaća 3 - Početna stranica</title>
</head>
<body>
    <h1>Vježba 8 - zadaća 3 - Početna stranica</h1>
    
    <!-- Informacije o autentifikaciji -->
    <div class="auth-info">
        <% if (request.getUserPrincipal() != null) { %>
            <strong>Prijavljeni ste kao:</strong> <%= request.getUserPrincipal().getName() %>
            <% if (request.isUserInRole("admin")) { %>
                <span style="color: green;">(Administrator)</span>
            <% } %>
        <% } else { %>
            <strong>Niste prijavljeni.</strong> 
            <a href="${pageContext.servletContext.contextPath}/prijavaKorisnika.xhtml">Prijavite se ovdje</a>
        <% } %>
    </div>

    <div class="section">
        <h2>MVC - Tvrtka</h2>
        
        <h3>Javni dio</h3>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica Tvrtka</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Provjera rada poslužitelja</a></li>
            <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled naziva partnera/restorana</a></li>
        </ul>

        <% if (request.getUserPrincipal() != null) { %>
            <h3>Privatni dio</h3>
            <ul>
                <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/obracun">Pregled obračuna (jelo i piće)</a></li>
                <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/obracun/jelo">Pregled obračuna (jelo)</a></li>
                <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/obracun/pice">Pregled obračuna (piće)</a></li>
            </ul>

            <% if (request.isUserInRole("admin")) { %>
                <h3>Administracijski dio</h3>
                <ul>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/partner/novi">Dodavanje novog partnera</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">Aktiviranje spavanja</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/konzola">Konzola za upravljanje poslužiteljem Tvrtka</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna konzola Tvrtka</a></li>
                </ul>
                
                <h3>Administracija poslužitelja</h3>
                <ul>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/kraj">Kraj rada poslužitelja Tvrtka</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/1">Start poslužitelja Tvrtka - registracija</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/1">Pauza poslužitelja Tvrtka - registracija</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/2">Start poslužitelja Tvrtka - za partnere</a></li>
                    <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/2">Pauza poslužitelja Tvrtka - za partnere</a></li>
                </ul>
            <% } %>
        <% } %>
    </div>

    <div class="section">
        <h2>Jakarta Faces - Partner</h2>
        <ul>
            <li><a href="${pageContext.servletContext.contextPath}/index.xhtml">Početna stranica Partner</a></li>
        </ul>
    </div>
    
    <% if (request.getUserPrincipal() != null) { %>
        <div class="section">
            <h2>Odjava</h2>
            <ul>
                <li><a href="${pageContext.servletContext.contextPath}/privatno/odjavaKorisnika.xhtml">Odjava korisnika</a></li>
            </ul>
        </div>
    <% } %>
</body>
</html>