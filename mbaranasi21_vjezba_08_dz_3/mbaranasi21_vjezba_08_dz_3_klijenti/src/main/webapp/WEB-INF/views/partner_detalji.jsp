<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Detalji partnera</title>
    <link rel="stylesheet" href="../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Detalji partnera/restorana</h1>
    
    <c:if test="${status == 200}">
        <div class="partner-detalji">
            <h2>${partner.naziv()}</h2>
            <table class="partner-info">
                <tr>
                    <td><strong>ID:</strong></td>
                    <td>${partner.id()}</td>
                </tr>
                <tr>
                    <td><strong>Vrsta kuhinje:</strong></td>
                    <td>${partner.vrstaKuhinje()}</td>
                </tr>
                <tr>
                    <td><strong>Adresa:</strong></td>
                    <td>${partner.adresa()}</td>
                </tr>
                <tr>
                    <td><strong>Mrežna vrata:</strong></td>
                    <td>${partner.mreznaVrata()}</td>
                </tr>
                <tr>
                    <td><strong>Mrežna vrata kraj:</strong></td>
                    <td>${partner.mreznaVrataKraj()}</td>
                </tr>
                <tr>
                    <td><strong>GPS koordinate:</strong></td>
                    <td>${partner.gpsSirina()}, ${partner.gpsDuzina()}</td>
                </tr>
                <tr>
                    <td><strong>Sigurnosni kod:</strong></td>
                    <td>${partner.sigurnosniKod()}</td>
                </tr>
                <tr>
                    <td><strong>Admin kod:</strong></td>
                    <td>${partner.adminKod()}</td>
                </tr>
            </table>
        </div>
    </c:if>
    
    <c:if test="${status != 200}">
        <div class="error">
            <p>Greška pri dohvaćanju podataka o partneru. Status: ${status}</p>
        </div>
    </c:if>
    
    <div class="navigation">
        <a href="../partner">← Povratak na popis partnera</a>
    </div>
</body>
</html>