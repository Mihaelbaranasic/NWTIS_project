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
                <td class="<%= (Integer.valueOf(200).equals(request.getAttribute("statusT"))) ? "status-ok" : "status-error" %>">
                    <%= (Integer.valueOf(200).equals(request.getAttribute("statusT"))) ? "RADI" : "NE RADI" %>
                </td>
                <td>
                    <button onclick="pauzaPosluzitelj(0)" class="btn-warning">Pauza</button>
                    <button onclick="startPosluzitelj(0)" class="btn-success">Start</button>
                </td>
            </tr>
            <tr>
                <td><strong>Dio 1:</strong></td>
                <td class="<%= (Integer.valueOf(200).equals(request.getAttribute("statusT1"))) ? "status-ok" : "status-error" %>">
                    <%= (Integer.valueOf(200).equals(request.getAttribute("statusT1"))) ? "RADI" : "NE RADI" %>
                </td>
                <td>
                    <button onclick="pauzaPosluzitelj(1)" class="btn-warning">Pauza</button>
                    <button onclick="startPosluzitelj(1)" class="btn-success">Start</button>
                </td>
            </tr>
            <tr>
                <td><strong>Dio 2:</strong></td>
                <td class="<%= (Integer.valueOf(200).equals(request.getAttribute("statusT2"))) ? "status-ok" : "status-error" %>">
                    <%= (Integer.valueOf(200).equals(request.getAttribute("statusT2"))) ? "RADI" : "NE RADI" %>
                </td>
                <td>
                    <button onclick="pauzaPosluzitelj(2)" class="btn-warning">Pauza</button>
                    <button onclick="startPosluzitelj(2)" class="btn-success">Start</button>
                </td>
            </tr>
        </table>
        
        <div class="danger-zone">
            <a href="../kraj" class="btn-danger" 
               onclick="return confirm('Jeste li sigurni da želite zaustaviti poslužitelj?')">
               🛑 Zaustavi poslužitelj
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
    
    <!-- WebSocket status -->
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
                
                // Ažuriraj status rada
                var statusElem = document.getElementById("statusRada");
                statusElem.innerHTML = status;
                statusElem.className = status === "RADI" ? "status-ok" : "status-error";
                
                // Ažuriraj broj obračuna
                document.getElementById("brojObracuna").innerHTML = brojObracuna;
                
                // Ažuriraj internu poruku ako postoji
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
        
        // AJAX funkcije za pauzu i start
        function pauzaPosluzitelj(id) {
            fetch('../pauza/' + id, { method: 'GET' })
                .then(response => {
                    if (response.ok) {
                        showMessage('Pauza aktivirana za dio ' + id, 'success');
                        setTimeout(refreshStatus, 1000);
                    } else {
                        showMessage('Greška pri aktiviranju pauze', 'error');
                    }
                })
                .catch(error => {
                    showMessage('Greška pri komunikaciji s poslužiteljem', 'error');
                });
        }

        function startPosluzitelj(id) {
            fetch('../start/' + id, { method: 'GET' })
                .then(response => {
                    if (response.ok) {
                        showMessage('Start aktiviran za dio ' + id, 'success');
                        setTimeout(refreshStatus, 1000);
                    } else {
                        showMessage('Greška pri aktiviranju starta', 'error');
                    }
                })
                .catch(error => {
                    showMessage('Greška pri komunikaciji s poslužiteljem', 'error');
                });
        }

        function refreshStatus() {
            location.reload();
        }

        function showMessage(message, type) {
            var messageDiv = document.createElement('div');
            messageDiv.className = 'message ' + type;
            messageDiv.textContent = message;
            
            var container = document.querySelector('.status-section');
            container.insertBefore(messageDiv, container.firstChild);
            
            setTimeout(function() {
                messageDiv.remove();
            }, 3000);
        }
        
        // Omogući slanje poruke pritiskom na Enter
        document.addEventListener("DOMContentLoaded", function() {
            document.getElementById("novaPoruka").addEventListener("keypress", function(event) {
                if (event.key === "Enter") {
                    posaljiPoruku();
                }
            });
        });

        window.addEventListener("load", connect, false);
    </script>
</body>
</html>