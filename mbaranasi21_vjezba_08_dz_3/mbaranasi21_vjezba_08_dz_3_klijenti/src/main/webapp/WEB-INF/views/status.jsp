<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Zadaća 2 - status Tvrtka</title>
    </head>
    <body>
        <h1>Zadaća 2 - status Tvrtka</h1>
        <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
            </li>
<%
if(request.getAttribute("status") != null) {
%>
            <li>
               <p>Status operacije: <%= request.getAttribute("status") %> </p>
            </li>
<%
}
if(request.getAttribute("samoOperacija") != null && ! (Boolean) request.getAttribute("samoOperacija")) {
%>
            <li>
               <p>Status poslužitelja: <%= request.getAttribute("statusT") %> </p>
            </li>
            <li>
               <p>Status poslužitelja za registraciju: <%= request.getAttribute("statusT1") %> </p>
            </li>
            <li>
               <p>Status poslužitelja za partnere: <%= request.getAttribute("statusT2") %> </p>
            </li>
<%
}
%>            
        </ul>          
    </body>
</html>
