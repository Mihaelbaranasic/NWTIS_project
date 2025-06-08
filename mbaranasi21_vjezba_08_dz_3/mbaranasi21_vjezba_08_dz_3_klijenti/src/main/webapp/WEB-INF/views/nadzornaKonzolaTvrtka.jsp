<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Zadaća 3 - Nadzorna konzola Tvrtka</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        .status-panel {
            border: 1px solid #ccc;
            padding: 15px;
            margin: 10px 0;
            border-radius: 5px;
        }
        .status-radi {
            background-color: #d4edda;
            border-color: #c3e6cb;
            color: #155724;
        }
        .status-ne-radi {
            background-color: #f8d7da;
            border-color: #f5c6cb;
            color: #721c24;
        }
        #poruka {
            font-weight: bold;
        }
        #statusTvrtka {
            font-size: 18px;
            font-weight: bold;
        }
        #brojObracuna {
            font-size: 16px;
            color: #007bff;
        }
        #internaPoruka {
            font-style: italic;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <h1>Nadzorna konzola Tvrtka</h1>
    <br />
    
    <div class="status-panel" id="glavniPanel">
        <h2>Status poslužitelja Tvrtka</h2>
        <p>
            Status: <span id="statusTvrtka">-</span>
        </p>
        <p>
            Broj primljenih obračuna: <span id="brojObracuna">0</span>
        </p>
        <div id="internaPoruka"></div>
    </div>
    
    <div class="status-panel">
        <h3>WebSocket komunikacija</h3>
        <p>
            Zadnja poruka: <span id="poruka">Nema poruka</span>
        </p>
        <p>
            Status konekcije: <span id="konekcijaStatus">Spajanje...</span>
        </p>
    </div>

    <script type="text/javascript">
        var wsocket;
        
        function connect() {
            var adresa = window.location.pathname;
            var dijelovi = adresa.split("/");
            adresa = "ws://" + window.location.hostname + ":" 
                   + window.location.port + "/" + dijelovi[1] 
                   + "/ws/tvrtka";
            
            console.log("Spajanje na WebSocket: " + adresa);
            
            if ('WebSocket' in window) {
                wsocket = new WebSocket(adresa);
            } else if ('MozWebSocket' in window) {
                wsocket = new MozWebSocket(adresa);
            } else {
                alert('WebSocket nije podržan od web preglednika.');
                return;
            }
            
            wsocket.onopen = function(evt) {
                console.log("WebSocket konekcija otvorena");
                document.getElementById("konekcijaStatus").innerHTML = "Spojeno";
            };
            
            wsocket.onmessage = onMessage;
            
            wsocket.onclose = function(evt) {
                console.log("WebSocket konekcija zatvorena");
                document.getElementById("konekcijaStatus").innerHTML = "Zatvoreno";
            };
            
            wsocket.onerror = function(evt) {
                console.log("WebSocket greška: " + evt.data);
                document.getElementById("konekcijaStatus").innerHTML = "Greška";
            };
        }

        function onMessage(evt) {
            var poruka = evt.data;
            console.log("Primljena WebSocket poruka: " + poruka);
            
            document.getElementById("poruka").innerHTML = poruka;
            
            var dijelovi = poruka.split(";");
            if (dijelovi.length >= 3) {
                var status = dijelovi[0];
                var brojObracuna = dijelovi[1];
                var internaPoruka = dijelovi[2];
                
                var statusElem = document.getElementById("statusTvrtka");
                var glavniPanel = document.getElementById("glavniPanel");
                
                statusElem.innerHTML = status;
                
                if (status === "RADI") {
                    glavniPanel.className = "status-panel status-radi";
                } else if (status === "NE RADI") {
                    glavniPanel.className = "status-panel status-ne-radi";
                }
                
                document.getElementById("brojObracuna").innerHTML = brojObracuna;
                
                if (internaPoruka && internaPoruka.trim() !== "") {
                    document.getElementById("internaPoruka").innerHTML = 
                        "<strong>Interna poruka:</strong> " + internaPoruka;
                } else {
                    document.getElementById("internaPoruka").innerHTML = "";
                }
            }
        }

        window.addEventListener("load", connect, false);
    </script>
</body>
</html>