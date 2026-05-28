# Hotel Reservation Application

This application provides a backend for hotel room reservations. Guests can search for available rooms and book them online. Hotel staff manage the on-site stay process. Administrators control staff assignments.

Guests can browse active hotels filtered by city, view hotel details and service offerings, check room availability through a calendar view, search for rooms by dates with support for guest composition and pets, create reservations with additional services, view their reservation history, and cancel upcoming reservations.

Hotel staff can list all reservations, perform check-in and check-out, mark reservations as no-show, and update the operational status of individual rooms. Administrators can assign and remove staff members from hotels.

The goal of the application is to cover the complete guest journey from room discovery to check-out through a structured and secured REST API.

# Zber poziadaviek

- **RQ01** System bude rozlisovat roly pouzivatelov (`GUEST`, `STAFF`, `ADMIN`).
- **RQ02** System zobrazi zoznam aktivnych hotelov.
- **RQ03** System umozni filtrovat hotely podla mesta.
- **RQ04** System zobrazi detail hotela vratane nazvu, mesta, krajiny, hviezdiciek a popisu.
- **RQ05** System zobrazi zoznam sluzieb dostupnych v hoteli.
- **RQ06** System zobrazi zoznam typov izieb pre vybrany hotel.
- **RQ07** System bude evidovat typ izby vratane kapacity, zakladnej ceny, typu postele, vyhladov a vybavenia.
- **RQ08** System bude evidovat politiku obsadenosti a politiku domacich zvierat pre kazdy typ izby.
- **RQ09** System zobrazi kalendar dostupnosti pre vybrany typ izby.
- **RQ10** System umozni vyhladavat dostupne izby podla datumu prichodu a odchodu.
- **RQ11** System umozni filtrovat vyhladavanie izieb podla zlozenia hostov (dospeli a deti s ich vekovymi skupinami).
- **RQ12** System umozni filtrovat vyhladavanie izieb podla domacich zvierat (typ a velkost).
- **RQ13** System umozni filtrovat vyhladavanie izieb podla pozadovanych sluzieb hotela.
- **RQ14** System umozni hostovi vytvorit rezervaciu pre vybrany typ izby a datumove obdobie.
- **RQ15** System umozni hostovi pridat sluzby hotela k rezervacii pri jej vytvoreni.
- **RQ16** System bude evidovat kontaktne udaje a specialne poziadavky v rezervacii.
- **RQ17** System bude evidovat snimku ceny a vybranych sluzieb v case vytvorenia rezervacie.
- **RQ18** System bude evidovat stav rezervacie (`PENDING`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`).
- **RQ19** System umozni hostovi zobrazit zoznam vlastnych rezervacii.
- **RQ20** System umozni hostovi zobrazit detail rezervacie.
- **RQ21** System umozni hostovi zrusit vlastnu rezervaciu.
- **RQ22** System umozni vytvorit rezervaciu bez prihlasenia.
- **RQ23** System umozni personalu zobrazit zoznam vsetkych rezervacii.
- **RQ24** System umozni personalu vykonat check-in hosta na zaklade rezervacie.
- **RQ25** System umozni personalu vykonat check-out hosta.
- **RQ26** System umozni personalu oznacit rezervaciu ako no-show.
- **RQ27** System bude evidovat prevadzkovy stav izby (`AVAILABLE`, `OCCUPIED`, `CLEANING`, `MAINTENANCE`, `OUT_OF_SERVICE`).
- **RQ28** System umozni personalu aktualizovat prevadzkovy stav izby.
- **RQ29** System umozni personalu zobrazit zoznam izieb.
- **RQ30** System umozni personalu vytvorit rezervaciu v mene hosta.
- **RQ31** System umozni adminovi priradit clena personalu k hotelu.
- **RQ32** System umozni adminovi odvolat clena personalu z hotela.
- **RQ33** System bude evidovat audit log klucovych akcii v systeme.

# Slovnik pojmov

| Pojem | Anglicky nazov | Definicia |
|-------|----------------|-----------|
| **Host** | Guest | Prihlaseny pouzivatel, ktory moze vyhladavat izby, vytvarat rezervacie a spravovat vlastne rezervacie. |
| **Clen personalu** | Staff Member | Pouzivatel s opravnenim spravovat priebeh pobytu, vykonavat check-in a check-out hostov a aktualizovat stav izieb. |
| **Administrator** | Administrator | Pouzivatel s opravnenim priradovat a odvolavat clenov personalu z hotelov. |
| **Hotel** | Hotel | Ubytovaci objekt evidovany v systeme s nazvom, mestom, krajinou, poctom hviezdiciek, popisom a zoznamom izieb a sluzieb. |
| **Izba** | Room | Fyzicka izba v hoteli priradena k typu izby a evidovana s aktualnym prevadzkovym stavom. |
| **Typ izby** | Room Type | Kategoria izieb v hoteli definovana kapacitou, zakladnou cenou, typom postele a dalsimi vlastnostami. |
| **Rezervacia** | Reservation | Zaznam o objednani ubytovania obsahujuci hosta, typ izby, datumove obdobie, zlozenie hostov a vybrane sluzby. |
| **Pobyt** | Stay | Zaznam o skutocnom pobyte hosta v hoteli vytvoreny pri check-in. |
| **Sluzba hotela** | Service Offering | Doplnkova sluzba ponukana hotelom, ktoru si host moze pridat k rezervacii. |
| **Check-in** | Check-in | Proces registracie prichodu hosta na zaklade existujucej rezervacie. |
| **Check-out** | Check-out | Proces odchodu hosta a ukoncenia pobytu v hoteli. |
| **Stav rezervacie** | Reservation Status | Informacia o aktualnom stave rezervacie, napriklad cakajuca, potvrdena, prebehajuca, zrusena alebo no-show. |
| **Prevadzkovy stav izby** | Room Status | Informacia o aktualnom stave izby, napriklad dostupna, obsadena, v uprate alebo mimo prevadzky. |
| **Cenova snimka** | Price Snapshot | Zaznam ceny a vybranych sluzieb zachyteny v case vytvorenia rezervacie. |
| **Zlozenie hostov** | Accommodation Party | Informacia o pocte dospelych, veku deti a domacich zvieratach v ramci rezervacie. |
| **Auditny zaznam** | Audit Log | Zaznam klucovych akcii vykonanych v systeme obsahujuci typ akcie, aktora a casovu peciatku. |
| **OpenAPI** | OpenAPI | Strojovo citatelna specifikacia REST API pouzivana na generovanie serverovych rozhrani a datovych tried. |
| **Hexagonalna architektura** | Hexagonal Architecture | Architektonicky vzor, v ktorom je domenova logika nezavisla od frameworku, databazy a transportnej vrstvy. |

# Pripady pouzitia

- **UC-01** Zobrazenie zoznamu hotelov
- **UC-02** Filtrovanie hotelov podla mesta
- **UC-03** Zobrazenie detailu hotela
- **UC-04** Zobrazenie sluzieb hotela
- **UC-05** Zobrazenie typov izieb hotela
- **UC-06** Zobrazenie kalendara dostupnosti izby
- **UC-07** Vyhladavanie dostupnych izieb
- **UC-08** Vytvorenie rezervacie
- **UC-09** Vytvorenie rezervacie bez prihlasenia
- **UC-10** Zobrazenie vlastnych rezervacii
- **UC-11** Zobrazenie detailu rezervacie
- **UC-12** Zrusenie rezervacie
- **UC-13** Zobrazenie zoznamu vsetkych rezervacii
- **UC-14** Vytvorenie rezervacie personalom
- **UC-15** Vykonanie check-in hosta
- **UC-16** Vykonanie check-out hosta
- **UC-17** Oznacenie rezervacie ako no-show
- **UC-18** Zobrazenie zoznamu izieb
- **UC-19** Aktualizacia prevadzkoveho stavu izby
- **UC-20** Zobrazenie zoznamu personalu
- **UC-21** Priradenie clena personalu k hotelu
- **UC-22** Odvolanie clena personalu z hotela

## UC-07 Vyhladavanie dostupnych izieb

**Ucel**  
Najst dostupne izby zodpovedajuce zadanym kriteriom pobytu.

**Pouzivatel**  
Host

**Vstupne podmienky**  
System obsahuje aspon jeden aktivny hotel s dostupnymi izbami.

**Vystup**  
Host vidi zoznam izieb zodpovedajucich zadanym kriteriom.

**Postup**

1. Host zada datum prichodu a datum odchodu.
2. System overi platnost zadaneho obdobia.
3. Host zada pocet dospelych a volitelne deti s ich vekom a domace zvierata.
4. Host potvrdi vyhladavanie.
5. System vyhlada dostupne izby zodpovedajuce zadanym kriteriom.
6. System zobrazi zoznam vysledkov.

**Alternativny scenar**

2a. Datum odchodu nie je neskorsi ako datum prichodu.  
System odmietne poziadavku a informuje hosta o neplatnom obdobi.

6a. Ziaden typ izby nevyhovuje zadanym kriteriom.  
System zobrazi prazdny zoznam vysledkov.

## UC-08 Vytvorenie rezervacie

**Ucel**  
Zarezervovat vybrany typ izby pre zadane datumove obdobie.

**Pouzivatel**  
Host

**Vstupne podmienky**  
Host je prihlaseny. Vybrany typ izby je dostupny v zadanom obdobi.

**Vystup**  
V systeme vznikne nova rezervacia so stavom `PENDING`.

**Postup**

1. Host vyberie typ izby z vysledkov vyhladavania.
2. Host zada kontaktne udaje a volitelne specialne poziadavky.
3. Host vyberie doplnkove sluzby hotela.
4. Host potvrdi vytvorenie rezervacie.
5. System overi dostupnost vybraneho typu izby v zadanom obdobi.
6. System zaznamena cenovu snimku v case vytvorenia rezervacie.
7. System ulozi rezervaciu so stavom `PENDING`.
8. System zobrazi potvrdenie o uspesnom vytvoreni.

**Alternativny scenar**

5a. Vybrany typ izby uz nie je dostupny v zadanom obdobi.  
System informuje hosta a rezervaciu neulozi.

## UC-12 Zrusenie rezervacie

**Ucel**  
Zrusit vlastnu rezervaciu hosta.

**Pouzivatel**  
Host

**Vstupne podmienky**  
Host je prihlaseny. Rezervacia patri prihlasenemu hostovi a je v stave `PENDING` alebo `CONFIRMED`.

**Vystup**  
Stav rezervacie sa zmeni na `CANCELLED`.

**Postup**

1. Host otvori zoznam vlastnych rezervacii.
2. System zobrazi rezervacie prihlasenemu hostovi.
3. Host vyberie rezervaciu a zvoli moznost zrusit.
4. System overi, ze rezervacia patri prihlasenemu hostovi.
5. System zmeni stav rezervacie na `CANCELLED`.
6. System ulozi aktualizovanu rezervaciu.

**Alternativny scenar**

4a. Rezervacia nepatri prihlasenemu hostovi.  
System akciu nepovoli.

5a. Rezervacia je v stave `CHECKED_IN` alebo `CHECKED_OUT`.  
System akciu nepovoli.

## UC-15 Vykonanie check-in hosta

**Ucel**  
Zaregistrovat prichod hosta na zaklade existujucej rezervacie.

**Pouzivatel**  
Clen personalu

**Vstupne podmienky**  
Clen personalu je prihlaseny. Rezervacia je v stave `CONFIRMED`.

**Vystup**  
Stav rezervacie sa zmeni na `CHECKED_IN` a v systeme vznikne zaznam o pobyte.

**Postup**

1. Clen personalu otvori zoznam rezervacii.
2. System zobrazi zoznam vsetkych rezervacii.
3. Clen personalu vyberie rezervaciu a zvoli check-in.
4. System overi aktualny stav rezervacie.
5. System priradi izbu k rezervacii.
6. System zmeni stav rezervacie na `CHECKED_IN`.
7. System vytvori zaznam o pobyte hosta.

**Alternativny scenar**

4a. Rezervacia nie je v stave `CONFIRMED`.  
System akciu nepovoli.

## UC-16 Vykonanie check-out hosta

**Ucel**  
Ukoncit pobyt hosta a uzatvorit rezervaciu.

**Pouzivatel**  
Clen personalu

**Vstupne podmienky**  
Clen personalu je prihlaseny. Rezervacia je v stave `CHECKED_IN`.

**Vystup**  
Stav rezervacie sa zmeni na `CHECKED_OUT`.

**Postup**

1. Clen personalu otvori zoznam rezervacii.
2. System zobrazi zoznam vsetkych rezervacii.
3. Clen personalu vyberie rezervaciu a zvoli check-out.
4. System overi aktualny stav rezervacie.
5. System zmeni stav rezervacie na `CHECKED_OUT`.
6. System aktualizuje stav izby na `CLEANING`.
7. System ulozi aktualizovanu rezervaciu.

**Alternativny scenar**

4a. Rezervacia nie je v stave `CHECKED_IN`.  
System akciu nepovoli.

# Technicka realizacia backendu

Backend je rozdeleny na moduly:

| Modul | Zodpovednost |
|-------|--------------|
| `application/domain` | Domenova logika, entity, hodnotove objekty, fasady, sluzby, repository porty, factories, predicates a domenove vynimky. |
| `application/api-spec` | OpenAPI specifikacia a z nej generovane serverove rozhrania a DTO triedy. |
| `application/inbound-controller-rest` | REST controllery, MapStruct mappers, bezpecnostna konfiguracia a spracovanie chybovych stavov. |
| `application/outbound-repository-jpa` | JPA adaptery, Spring Data repozitare a ORM mapovanie domenovych entit. |
| `application/springboot` | Spustenie aplikacie, konfiguracia beanov, transakcie, Liquibase migracne skripty a ArchUnit testy. |

# UML diagramy

PlantUML zdrojove kody diagramov sa nachadzaju v priecinku:

```text
docs/diagram
```

| Subor | Obsah |
|-------|-------|
| `hexagonal-architecture.puml` | Moduly backendu a smer ich zavislosti v hexagonalnej architekture. |
| `domain-model.puml` | Domenovy model so vztahmi medzi hlavnymi entitami systemu. |

# OpenAPI

Specifikacia REST API sa nachadza v subore:

```text
application/api-spec/src/main/resources/openapi/hotel-reservation.yaml
```

# Spustenie

## Lokalne zavislosti

```sh
docker compose up -d
```

## Backend aplikacia

```sh
mvn spring-boot:run -pl application/springboot -am
```

## Testy

```sh
mvn clean test
```

# Testovanie

System je overeny pomocou:

- domenovych testov entit (`RoomTest`, `GuestTest`, `StayTest`, `AuditLogEntryTest`, `ServiceOfferingTest`),
- testov domenovych sluzieb (`ReservationServiceTest`, `SearchAvailabilityServiceTest`, `RoomOperationsServiceTest`),
- testov factory (`ReservationFactoryTest`),
- testov predicate pravidiel (`DomainPredicateTest`, `ReservationPredicateTest`),
- testov pristupovych politik (`ReservationAccessPolicyTest`, `HotelScopePolicyTest`),
- testov REST controllerov (`HotelsControllerTest`, `ReservationsControllerTest`, `StaffReservationsControllerTest`),
- testov bezpecnostnej vrstvy (`JwtConverterTest`, `SpringSecurityCurrentUserAdapterTest`),
- integracnych testov s Testcontainers (`JpaRepositoryAdapterIntegrationTest`, `CreateReservationFlowIntegrationTest`),
- ArchUnit testu hexagonalnej architektury.

