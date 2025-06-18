<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="edu.unizg.foi.nwtis.podaci.Obracun"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Pregled obračuna</title>
    <link rel="stylesheet" href="../../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Pregled obračuna - ${tipObracuna}</h1>
    
    <!-- Obrazac za odabir razdoblja -->
    <div class="filter-form">
        <form method="get">
            <label for="od">Od (yyyy-MM-dd HH:mm:ss):</label>
            <input type="text" id="od" name="od" value="${param.od}" placeholder="2024-01-01 00:00:00">
            
            <label for="do">Do (yyyy-MM-dd HH:mm:ss):</label>
            <input type="text" id="do" name="do" value="${param.do}" placeholder="2024-12-31 23:59:59">
            
            <input type="submit" value="Filtriraj">
        </form>
        
        <div class="filter-links">
            <a href="obracun?od=${param.od}&do=${param.do}">Svi obračuni</a> |
            <a href="obracun/jelo?od=${param.od}&do=${param.do}">Samo jelo</a> |
            <a href="obracun/pice?od=${param.od}&do=${param.do}">Samo piće</a>
        </div>
    </div>
    
    <!-- Prikaz rezultata samo ako su zadani parametri -->
    <% 
    String od = request.getParameter("od");
    String doVrijeme = request.getParameter("do");
    if (od != null && !od.trim().isEmpty() && doVrijeme != null && !doVrijeme.trim().isEmpty()) {
        Integer status = (Integer) request.getAttribute("status");
        if (status != null && status == 200) {
            List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
            if (obracuni != null && !obracuni.isEmpty()) {
    %>
                <table class="obracuni-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Partner ID</th>
                            <th>Tip</th>
                            <th>Količina</th>
                            <th>Cijena</th>
                            <th>Vrijeme</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Obracun obracun : obracuni) { %>
                            <tr>
                                <td><%= obracun.id() %></td>
                                <td><%= obracun.partner() %></td>
                                <td><%= obracun.jelo() ? "Jelo" : "Piće" %></td>
                                <td><%= obracun.kolicina() %></td>
                                <td><%= obracun.cijena() %></td>
                                <td><%= new java.util.Date(obracun.vrijeme()) %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
    <%      } else { %>
                <p>Nema obračuna za zadano razdoblje.</p>
    <%      }
        } else { %>
            <div class="error">
                <p>Greška pri dohvaćanju obračuna. Status: <%= status %></p>
            </div>
    <%  }
    } else { %>
        <div class="info">
            <p>Molimo unesite razdoblje (od-do) i pritisnite "Filtriraj" za pregled obračuna.</p>
        </div>
    <% } %>
    
    <div class="navigation">
        <a href="../pocetak">← Povratak na početak</a>
    </div>
</body>
</html>