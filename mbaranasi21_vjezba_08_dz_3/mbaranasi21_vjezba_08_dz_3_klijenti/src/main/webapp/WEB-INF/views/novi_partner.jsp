<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Dodavanje novog partnera</title>
    <link rel="stylesheet" href="../../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Dodavanje novog partnera</h1>
    
    <% 
    String poruka = (String) request.getAttribute("poruka");
    Integer status = (Integer) request.getAttribute("status");
    if (poruka != null) { 
    %>
        <div class="message <%= (status != null && status == 201) ? "success" : "error" %>">
            <%= poruka %>
        </div>
    <% } %>
    
    <form method="post" class="partner-form">
        <div class="form-group">
            <label for="naziv">Naziv partnera/restorana:</label>
            <input type="text" id="naziv" name="naziv" required 
                   placeholder="Unesite naziv partnera" value="${param.naziv}">
        </div>
        
        <div class="form-group">
            <label for="vrstaKuhinje">Vrsta kuhinje:</label>
            <input type="text" id="vrstaKuhinje" name="vrstaKuhinje" required 
                   placeholder="Unesite vrstu kuhinje" value="${param.vrstaKuhinje}">
        </div>
        
        <div class="form-group">
            <label for="adresa">Adresa:</label>
            <input type="text" id="adresa" name="adresa" required 
                   placeholder="Unesite adresu partnera" value="${param.adresa}">
        </div>
        
        <div class="form-group">
            <label for="mreznaVrata">Mrežna vrata:</label>
            <input type="number" id="mreznaVrata" name="mreznaVrata" required 
                   min="1024" max="65535" placeholder="8080" value="${param.mreznaVrata}">
        </div>
        
        <div class="form-group">
            <label for="mreznaVrataKraj">Mrežna vrata kraj:</label>
            <input type="number" id="mreznaVrataKraj" name="mreznaVrataKraj" required 
                   min="1024" max="65535" placeholder="8090" value="${param.mreznaVrataKraj}">
        </div>
        
        <div class="form-group">
            <label for="gpsSirina">GPS širina:</label>
            <input type="number" id="gpsSirina" name="gpsSirina" step="0.000001" required 
                   placeholder="45.815011" value="${param.gpsSirina}">
        </div>
        
        <div class="form-group">
            <label for="gpsDuzina">GPS dužina:</label>
            <input type="number" id="gpsDuzina" name="gpsDuzina" step="0.000001" required 
                   placeholder="15.981919" value="${param.gpsDuzina}">
        </div>
        
        <div class="form-group">
            <label for="sigurnosniKod">Sigurnosni kod:</label>
            <input type="text" id="sigurnosniKod" name="sigurnosniKod" required 
                   placeholder="Unesite sigurnosni kod" value="${param.sigurnosniKod}">
        </div>
        
        <div class="form-group">
            <label for="adminKod">Admin kod:</label>
            <input type="text" id="adminKod" name="adminKod" required 
                   placeholder="Unesite admin kod" value="${param.adminKod}">
        </div>
        
        <div class="form-actions">
            <input type="submit" value="Dodaj partnera" class="btn-primary">
            <a href="../konzola" class="btn-secondary">Odustani</a>
        </div>
    </form>
    
    <div class="navigation">
        <a href="../konzola">← Povratak na admin konzolu</a>
    </div>
</body>
</html>