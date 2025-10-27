INSERT INTO korisnici (korisnik, ime, prezime, lozinka, email) VALUES
	 ('jdean','James','Dean','123456','jdean@foi.hr'),
	 ('pgreen','Peter','Green','123456','pgreen@foi.hr'),
	 ('tjones','Tom','Jones','123456','tjones@foi.hr'),
	 ('jkostelic','Janica','Kostelic','123456','janica@foi.hr'),
	 ('jlennon','John','Lennon','123456','john@foi.hr'),
	 ('pmccartney','Paul','McCartney','123456','paul@foi.hr'),
	 ('gharrison','George','Harrison','123456','george@foi.hr'),
	 ('rstar','Ringo','Star','123456','ringo@foi.hr'),
	 ('joplin','Janis','Joplin','123456','janis@foi.hr'),
	 ('jlopez','Jennifer','Lopez','123456','jlo@foi.hr'),
	 ('pkos','Pero','Kos','123456','pero@foi.hr'),
	 ('mmedved','Mato','Medved','123456','mmedved@foi.hr'),
	 ('ivuk','Ivo','Vuk','123456','ivuk@foi.hr'),
	 ('fvrana','Fran','Vrana','123456','fvrana@foi.hr'),
	 ('hgavran','Helga','Gavran','123456','hgavran@foi.hr'),
	 ('lris','Lara','Ris','123456','lris@foi.hr'),
	 ('ssokol','Sonja','Sokol','123456','ssokol@foi.hr'),
	 ('dlasta','Dunja','Lasta','123456','dlasta@foi.hr'),
	 ('lfazan','Luka','Fazan','123456','lfazan@foi.hr'),
	 ('vtovar','Vice','Tovar','123456','ssokol@foi.hr');

INSERT INTO grupe (grupa, naziv) VALUES
	 ('manager-gui','Manager GUI'),
	 ('manager-script','Manager Script'),
	 ('manager-jmx','Manager JMX'),
	 ('manager-status','Manager Status'),
	 ('admin-gui','Admin GUI'),
	 ('admin-script','Admin Script'),
	 ('nwtis','NWTiS korisnik'),
	 ('admin','NWTiS Asmin');
	 
INSERT INTO uloge (korisnik, grupa) VALUES
	 ('pkos', 'manager-gui'),
	 ('pkos', 'manager-script'),
	 ('pkos', 'manager-jmx'),
	 ('pkos', 'manager-status'),
	 ('mmedved', 'admin-gui'),
	 ('mmedved', 'admin-script'),
	 ('pkos', 'admin'),
	 ('mmedved', 'nwtis'),
	 ('ivuk', 'nwtis'),
	 ('fvrana', 'nwtis'),
	 ('hgavran', 'nwtis'),
	 ('lris', 'nwtis'),
	 ('ssokol', 'nwtis'),
	 ('dlasta', 'nwtis'),
	 ('lfazan', 'nwtis'),
	 ('vtovar', 'nwtis');
	 

INSERT INTO partneri (id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod) VALUES
	 (1,'FOLT 1 - Varaždin, Pavlinska 9','MK','202.24.5.3', 8010, 8011, 46.30803, 16.34009, 'BABECABACC', 'CACEDACEAA'),
	 (2,'FOLT 2 - Varaždin, Ul. Julija Merlića 9','KK','202.24.5.3', 8020, 8021, 46.30835, 16.347809, 'BABECABACC', 'CACEDACEBB'),
	 (3,'FOLT 3 - Varaždin, Varaždin, Ul. Braće Radić 102','VK','202.24.5.3', 8030, 8031, 46.3015, 16.32023, 'BABECABACC', 'CACEDACECC'),
	 (4,'FOLT 4 - Varaždin, Trg Ivana Perkovca 39','KK','202.24.5.3', 8040, 8041, 46.28918, 16.33620, 'BABECABACC', 'CACEDACEDD');
