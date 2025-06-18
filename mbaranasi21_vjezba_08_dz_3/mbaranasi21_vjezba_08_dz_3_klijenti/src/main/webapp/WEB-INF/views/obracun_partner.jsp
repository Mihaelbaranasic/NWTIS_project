<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="edu.unizg.foi.nwtis.podaci.Obracun"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Obračuni partnera</title>
    <link rel="stylesheet" href="../../../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Obračuni partnera ${partnerId}</h1>
    
    <div class="filter-form">
        <form method="get" action="?">
            <label for="partnerId">ID partnera:</label>
            <input type="number" id="partnerId" name="partnerId" value="${param.partnerId}" placeholder="Unesite ID partnera" required>
            
            <label for="od">Od (yyyy-MM-dd HH:mm:ss):</label>
            <input type="text" id="od" name="od" value="${param.od}" placeholder="2024-01-01 00:00:00" required>
            
            <label for="do">Do (yyyy-MM-dd HH:mm:ss):</label>
            <input type="text" id="do" name="do" value="${param.do}" placeholder="2024-12-31 23:59:59" required>
            
            <input type="submit" value="Prikaži obračune">
        </form>
    </div>
    
    <% 
    String partnerId = request.getParameter("partnerId");
    String od = request.getParameter("od");
    String doVrijeme = request.getParameter("do");
    if (partnerId != null && !partnerId.trim().isEmpty() && 
        od != null && !od.trim().isEmpty() && 
        doVrijeme != null && !doVrijeme.trim().isEmpty()) {
        
        Integer status = (Integer) request.getAttribute("status");
        if (status != null && status == 200) {
            List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
            if (obracuni != null && !obracuni.isEmpty()) {
    %>
                <table class="obracuni-table">
                    <thead>
                        <tr>
                            <th>ID</th>
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
                                <td><%= obracun.jelo() ? "Jelo" : "Piće" %></td>
                                <td><%= obracun.kolicina() %></td>
                                <td><%= obracun.cijena() %></td>
                                <td><%= new java.util.Date(obracun.vrijeme()) %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
    <%      } else { %>
                <p>Nema obračuna za partnera <%= partnerId %> u zadanom razdoblju.</p>
    <%      }
        } else { %>
            <div class="error">
                <p>Greška pri dohvaćanju obračuna. Status: <%= status %></p>
            </div>
    <%  }
    } else { %>
        <div class="info">
            <p>Molimo unesite ID partnera i razdoblje (od-do), zatim pritisnite "Prikaži obračune".</p>
        </div>
    <% } %>
    
    <div class="navigation">
        <a href="../../pocetak">← Povratak na početak</a>
    </div>
</body>
</html>">← Povratak na početak</a>
    </div>
</body>
</html>