<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Aktiviranje spavanja</title>
    <link rel="stylesheet" href="../../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Aktiviranje spavanja poslužitelja</h1>
    
    <% 
    String poruka = (String) request.getAttribute("poruka");
    Integer status = (Integer) request.getAttribute("status");
    if (poruka != null) { 
    %>
        <div class="message <%= (status != null && status == 200) ? "success" : "error" %>">
            <%= poruka %>
        </div>
    <% } %>
    
    <form method="post" class="spavanje-form">
        <div class="form-group">
            <label for="vrijeme">Vrijeme spavanja (sekunde):</label>
            <input type="number" id="vrijeme" name="vrijeme" required 
                   min="1" max="3600" value="${param.vrijeme}"
                   placeholder="Unesite broj sekundi (1-3600)">
        </div>
        
        <div class="form-actions">
            <input type="submit" value="Aktiviraj spavanje" class="btn-primary">
        </div>
    </form>
    
    <div class="info">
        <h3>Napomena:</h3>
        <p>Spavanje će zaustaviti poslužitelj na zadano vrijeme. Koristite s oprezom!</p>
        <p>Preporučeno vrijeme: 5-30 sekundi za testiranje.</p>
    </div>
    
    <div class="navigation">
        <a href="../konzola">← Povratak na admin konzolu</a>
    </div>
</body>
</html>