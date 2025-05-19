<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Dodavanje korisnik</title>
        <style type="text/css">
.poruka {
	color: red;
}
        </style>
    </head>
    <body>
        <h1>Dodavanje korisnika</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            <%
            if(request.getAttribute("poruka") != null) {
              String poruka = (String) request.getAttribute("poruka");
              Object oPogreska = request.getAttribute("pogreska");
              boolean pogreska = false;
              System.out.println(oPogreska);
              if(oPogreska != null) {
                pogreska = (Boolean) oPogreska;
              }
              if(poruka.length() > 0) {
                String klasa = "";
                if(pogreska) {
                  klasa = "poruka";
                }
                %>
                <li>
                <p class="<%= klasa%>">${poruka}</p>
                </li>
                <%
              }
            }
            %>  
            <li><p>Podaci korisnia:</p>          
                <form method="post" action="${pageContext.servletContext.contextPath}/mvc/korisnici/dodajKorisnika">
                    <table>
                        <tr>
                            <td>Korisnik: </td>
                            <td><input name="korisnik" size="20" value="${korisnik}"/>
                                <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Lozinka: </td>
                            <td><input type="password" name="lozinka" size="20"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Ime: </td>
                            <td><input name="ime" size="20" value="${ime}"/></td>
                        </tr>
                        <tr>
                            <td>Prezime: </td>
                            <td><input name="prezime" size="20" value="${prezime}"/>
                            </td>
                        </tr>
                        <tr>
                            <td>Email: </td>
                            <td><input name="email" size="30" value="${email}"/></td>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td><input type="submit" value=" Dodaj korisnika "></td>
                        </tr>                        
                    </table>
                </form>
            </li>                     
        </ul>   
    </body>
</html>
