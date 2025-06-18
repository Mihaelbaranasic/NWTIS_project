<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Administracijska konzola</title>
    <link rel="stylesheet" href="../../css/nwtis.css" type="text/css">
</head>
<body>
    <h1>Konzola za upravljanje poslužiteljem Tvrtka</h1>
    
    <!-- Status informacije -->
    <div class="status-section">
        <h2>Status dijelova poslužitelja</h2>
        <table class="status-table">
            <tr>
                <td><strong>Glavni poslužitelj:</strong></td>
                <td class="${statusT == 200 ? 'status-ok' : 'status-error'}">
                    ${statusT == 200 ? 'RADI' : 'NE RADI'}
                </td>
                <td>
                    <a href="../pauza/0" class="btn-warning">Pauza</a>
                    <a href="../start/0" class="btn-success">Start</a>
                </td>
            </tr>
            <tr>
                <td><strong>Dio 1:</strong></td>
                <td class="${statusT1 == 200 ? 'status-ok' : 'status-error'}">
                    ${statusT1 == 200 ? 'RADI' : 'NE RADI'}
                </td>
                <td>
                    <a href="../pauza/1" class="btn-warning">Pauza</a>
                    <a href="../start/1" class="btn-success">Start</a>
                </td>
            </tr>
            <tr>
                <td><strong>Dio 2:</strong></td>
                <td class="${statusT2 == 200 ? 'status-ok' : 'status-error'}">
                    ${statusT2 == 200 ? 'RADI' : 'NE RADI'}
                </td>
                <td>
                    <a href="../pauza/2" class="btn-warning">Pauza</a>
                    <a href="../start/2" class="btn-success">Start</a>
                </td>
            </tr>
        </table>
        
        <div class="danger-zone">
            <a href="../kraj" class="btn-danger" 
               onclick="return confirm('Jeste li sigurni da želite zaustaviti poslužitelj?')">
               Zaustavi poslužitelj
            </a>
        </div>
    </div>
    
    <!-- Admin funkcije -->
    <div class="admin-functions">
        <h2>Administracijske funkcije</h2>
        
        <div class="function-grid">
            <div class="function-card">
                <h3>Dodaj novog partnera</h3>
                <p>Dodavanje novog partnera/restorana u sustav</p>
                <a href="partner/novi" class="btn-primary">Dodaj partnera</a>
            </div>
            
            <div class="function-card">
                <h3>Aktiviraj spavanje</h3>
                <p>Postavi poslužitelj u stanje spavanja</p>
                <a href="spavanje" class="btn-secondary">Aktiviraj spavanje</a>
            </div>
            
            <div class="function-card">
                <h3>Nadzorna konzola</h3>
                <p>WebSocket konzola za real-time informacije</p>
                <a href="nadzornaKonzolaTvrtka" class="btn-info">Otvori konzolu</a>
            </div>
        </div>
    </div>
    
    <!-- WebSocket status - kao na nadzornoj konzoli -->
    <div class="websocket-section">
        <h2>Real-time informacije</h2>
        <div class="websocket-info">
            <p><strong>Status rada:</strong> 
               <span id="statusRada" class="status-indicator">Učitava...</span></p>
            <p><strong>Broj obračuna:</strong> 
               <span id="brojObracuna">-</span></p>
            <p><strong>Interna poruka:</strong> 
               <span id="internaPoruka">-</span></p>
        </div>
        
        <!-- Obrazac za slanje interne poruke -->
        <div class="poruka-form">
            <h3>Pošalji internu poruku</h3>
            <div class="input-group">
                <input type="text" id="novaPoruka" placeholder="Unesite poruku..." maxlength="100">
                <button onclick="posaljiPoruku()" class="btn-primary">Pošalji</button>
            </div>
        </div>
    </div>
    
    <div class="navigation">
        <a href="../pocetak">← Povratak na početak</a>
    </div>

    <!-- WebSocket JavaScript -->
    <script type="text/javascript">
        var wsocket;
        
        function connect() {
            var adresa = window.location.pathname;
            var dijelovi = adresa.split("/");
            adresa = "ws://" + window.location.hostname + ":" 
                    + window.location.port + "/" + dijelovi[1] + "/ws/tvrtka";
            
            if ('WebSocket' in window) {
                wsocket = new WebSocket(adresa);
            } else if ('MozWebSocket' in window) {
                wsocket = new MozWebSocket(adresa);
            } else {
                alert('WebSocket nije podržan od web preglednika.');
                return;
            }
            
            wsocket.onmessage = onMessage;
        }

        function onMessage(evt) {
            var poruka = evt.data;
            var dijelovi = poruka.split(";");
            
            if (dijelovi.length >= 3) {
                var status = dijelovi[0];
                var brojObracuna = dijelovi[1];
                var internaPoruka = dijelovi[2];
                
                var statusElem = document.getElementById("statusRada");
                statusElem.innerHTML = status;
                statusElem.className = status === "RADI" ? "status-ok" : "status-error";
                
                document.getElementById("brojObracuna").innerHTML = brojObracuna;
                
                if (internaPoruka && internaPoruka.trim() !== "") {
                    document.getElementById("internaPoruka").innerHTML = internaPoruka;
                }
            }
        }
        
        function posaljiPoruku() {
            var poruka = document.getElementById("novaPoruka").value;
            if (poruka.trim() !== "" && wsocket && wsocket.readyState === WebSocket.OPEN) {
                wsocket.send(poruka);
                document.getElementById("novaPoruka").value = "";
            }
        }
        
        document.getElementById("novaPoruka").addEventListener("keypress", function(event) {
            if (event.key === "Enter") {
                posaljiPoruku();
            }
        });

        window.addEventListener("load", connect, false);
    </script>
</body>
</html>