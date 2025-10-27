<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Partner" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Pregled partnera</title>
        <style type="text/css">
table, th, td {
  border: 1px solid;
}       
th {
	text-align: center;
	font-weight: bold;
} 
.desno {
	text-align: right;
}
        </style>
    </head>
    <body>
        <h1>REST MVC - Pregled partnera</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>R.br. <th>Korisnik</th><th>Naziv</th><th>Adresa</th><th>Mrežna vrata</th><th>Mrežna vrata za kraj</th><th>Admin kod</th></tr>
	<%
	int i=0;
	List<Partner> partneri = (List<Partner>) request.getAttribute("partneri");
	for(Partner p: partneri) {
	  i++;
	  %>
       <tr><td class="desno"><%= i %></td><td><%= p.id() %></td><td><%= p.naziv() %></td><td><%= p.adresa() %></td><td><%= p.mreznaVrata() %></td><td><%= p.mreznaVrataKraj() %></td><td><%= p.adminKod() %></td></tr>	  
	  <%
	}
	%>	
        </table>	        
    </body>
</html>
