# Hotel Reservation Application

This application will allow users to search for hotel rooms available for reservation in different cities or hotels. Hotel guests will be able to filter available rooms by date of stay, number of guests or price and create reservations online.

Hotel staff will be able to manage reservations, perform check-in and check-out of guests and update the status of rooms (available, occupied, cleaning or maintenance). The system will help hotel staff keep track of room availability and current hotel occupancy.

The system may also support dynamic price adjustments during holidays or periods of high demand.

The goal of the application is to simplify hotel reservation management and improve the experience of guests when searching for accommodation.


# Zber požiadaviek

- **RQ01** Systém umožní vyhľadávať hotely podľa mesta alebo názvu.
- **RQ02** Systém umožní zobraziť dostupné izby v hoteli.
- **RQ03** Systém umožní filtrovať izby podľa dátumu pobytu, počtu hostí a ceny.
- **RQ04** Systém umožní zobraziť detail izby.
- **RQ05** Systém umožní vytvoriť rezerváciu izby.
- **RQ06** Systém zaeviduje k rezervácii hosťa, dátum príchodu, dátum odchodu a izbu.
- **RQ07** Systém zabezpečí, aby jedna izba nebola rezervovaná viackrát v rovnakom termíne.
- **RQ08** Systém umožní zobraziť detail rezervácie.
- **RQ09** Systém umožní spravovať rezervácie.
- **RQ10** Systém umožní vykonať check-in hosťa.
- **RQ11** Systém umožní vykonať check-out hosťa.
- **RQ12** Systém umožní meniť stav izby.
- **RQ13** Systém eviduje stav izby (AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE).
- **RQ14** Systém umožní sledovať dostupnosť izieb.
- **RQ15** Systém umožní sledovať obsadenosť hotela.
- **RQ16** Systém umožní evidovať platbu za rezerváciu.
- **RQ17** Systém eviduje stav rezervácie (CREATED, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED).
- **RQ18** Systém umožní vytvárať a upravovať hotely.
- **RQ19** Systém umožní pridávať izby do hotela.
- **RQ20** Systém môže upravovať cenu izieb počas zvýšeného dopytu.

# Slovník pojmov

| Pojem           | Anglický názov     | Definícia |
|-----------------|--------------------|------------|
| Hotel           | Hotel              | Ubytovacie zariadenie, ktoré poskytuje služby krátkodobého ubytovania pre hostí. |
| Izba            | Room               | Samostatný priestor v hoteli určený na ubytovanie jednej alebo viacerých osôb. |
| Hosť            | Guest              | Osoba, ktorá využíva alebo plánuje využiť služby ubytovania v hoteli. |
| Rezervácia      | Reservation        | Záznam o pridelení konkrétnej izby hosťovi na vopred určené časové obdobie. |
| Platba          | Payment            | Finančné vyrovnanie ceny rezervácie alebo pobytu hosťa.|
| Personál        | Staff              | Zamestnanci hotela, ktorí zabezpečujú prevádzku hotela a komunikáciu s hosťami. |
| Administrátor   | Administrator      | Používateľ systému, ktorý spravuje údaje o hoteloch, izbách a ďalších častiach systému. |
| Stav izby       | Room Status        | Označenie aktuálneho prevádzkového stavu izby v systéme. |
| Stav rezervácie | Reservation Status | Označenie aktuálneho stavu rezervácie v systéme. |
| Dostupnosť      | Availability       | Informácia o tom, či je izba voľná na ubytovanie v konkrétnom časovom období. |
| Obsadenosť      | Occupancy          | Počet alebo podiel izieb, ktoré sú v danom čase obsadené hosťami. |
| Check-in        | Check-in           | Proces príchodu hosťa do hotela, pri ktorom hosť začína svoj pobyt. |
| Check-out       | Check-out          | Proces odchodu hosťa z hotela, pri ktorom sa pobyt ukončuje. |


# Prípady použitia

## UC01 – Vytvorenie rezervácie izby

**Účel**  
Vytvoriť novú rezerváciu izby pre zvolené obdobie pobytu.

**Používateľ**  
Hosť

**Vstupné podmienky**  
V systéme sú zaevidované hotely a izby.  
Používateľ má prístup k systému.

**Výstup**  
V systéme je vytvorená nová rezervácia pre vybranú izbu a zadaný termín pobytu.

**Postup**
1. Používateľ zadá mesto alebo názov hotela. Systém vyhľadá zodpovedajúce hotely.  

2. Používateľ vyberie konkrétny hotel. Systém zobrazí izby patriace do hotela a zobrazí pri každej izbe základné informácie o kapacite, cene a stave.  

3. Používateľ zadá dátum príchodu, dátum odchodu a počet hostí. Systém vyfiltruje vhodné izby podľa zadaných podmienok.  

4. Používateľ vyberie konkrétnu izbu. Systém zobrazí detail izby, pripraví rezerváciu pre zadaný termín a umožní zadať údaje hosťa.  

5. Používateľ zadá potrebné údaje a potvrdí rezerváciu. Systém overí dostupnosť izby v zadanom termíne, vytvorí rezerváciu a nastaví jej stav.

**Alternatívy**
- 1a. Nebol zadaný žiadny vyhľadávací údaj.  
- 1a1. Systém vyzve používateľa na zadanie mesta alebo názvu hotela.  

- 3a. Pre zadané podmienky nie sú dostupné žiadne izby.  
- 3a1. Systém zobrazí informáciu o nedostupnosti izieb. UC končí.  

- 5a. Izba už nie je dostupná v zadanom termíne.  
- 5a1. Systém informuje používateľa a umožní mu zvoliť inú izbu alebo iný termín. UC končí.  

- 5b. Používateľ nevyplní povinné údaje.  
- 5b1. Systém zobrazí chybové hlásenie a vyžiada doplnenie údajov.


## UC02 – Check-in hosťa

**Účel**  
Zaznamenať príchod hosťa a začať jeho pobyt.

**Používateľ**  
Personál hotela

**Vstupné podmienky**  
V systéme existuje rezervácia pre hosťa.  
Rezervácia nie je zrušená ani ukončená.

**Výstup**  
Rezervácia má stav CHECKED_IN a izba má stav OCCUPIED.

**Postup**
1. Používateľ otvorí evidenciu rezervácií. Systém zobrazí zoznam rezervácií.  

2. Používateľ vyhľadá konkrétnu rezerváciu. Systém zobrazí zodpovedajúce rezervácie a zobrazí pri nich základné údaje o hosťovi, termíne pobytu a stave rezervácie.  

3. Používateľ vyberie jednu rezerváciu. Systém zobrazí detail rezervácie vrátane údajov o hosťovi a priradenej izbe.  

4. Používateľ zvolí vykonanie check-in. Systém overí stav rezervácie, overí pripravenosť izby na ubytovanie a vyhodnotí, či je check-in možné vykonať.  

5. Používateľ potvrdí check-in hosťa. Systém zmení stav rezervácie na CHECKED_IN a zmení stav izby na OCCUPIED.

**Alternatívy**
- 2a. Vyhľadávaná rezervácia neexistuje.  
- 2a1. Systém informuje používateľa, že rezervácia nebola nájdená. UC končí.  

- 4a. Rezervácia je v stave CANCELLED alebo CHECKED_OUT.  
- 4a1. Systém informuje používateľa, že check-in nie je možné vykonať. UC končí.  

- 4b. Izba je v stave CLEANING alebo MAINTENANCE.  
- 4b1. Systém informuje používateľa, že izba nie je pripravená na ubytovanie. UC končí.  

- 5a. Používateľ zruší vykonanie check-in.  
- 5a1. Systém nevykoná žiadnu zmenu. UC končí.


## UC03 – Check-out hosťa

**Účel**  
Ukončiť pobyt hosťa a uvoľniť izbu na ďalšie použitie.

**Používateľ**  
Personál hotela

**Vstupné podmienky**  
V systéme existuje rezervácia v stave CHECKED_IN.

**Výstup**  
Rezervácia má stav CHECKED_OUT a izba má nový stav podľa rozhodnutia personálu.

**Postup**
1. Používateľ otvorí evidenciu aktívnych pobytov. Systém zobrazí zoznam rezervácií, pri ktorých hosť aktuálne býva v hoteli.  

2. Používateľ vyhľadá konkrétnu rezerváciu. Systém zobrazí detail rezervácie a zobrazí údaje o hosťovi, izbe a termíne pobytu.  

3. Používateľ skontroluje ukončenie pobytu. Systém zobrazí informáciu o platbe alebo o stave úhrady.  

4. Používateľ podľa potreby zaeviduje platbu. Systém overí správnosť údajov o platbe, uloží platbu k rezervácii a aktualizuje stav úhrady.  

5. Používateľ potvrdí check-out a zvolí nový stav izby. Systém zmení stav rezervácie na CHECKED_OUT a uloží nový stav izby.

**Alternatívy**
- 2a. Rezervácia neexistuje.  
- 2a1. Systém informuje používateľa, že rezervácia nebola nájdená. UC končí.  

- 4a. Údaje o platbe nie sú správne alebo nie sú úplné.  
- 4a1. Systém zobrazí chybové hlásenie a vyžiada opravu údajov.  

- 5a. Rezervácia nie je v stave CHECKED_IN.  
- 5a1. Systém informuje používateľa, že check-out nie je možné vykonať. UC končí.  

- 5b. Používateľ nezvolí nový stav izby.  
- 5b1. Systém vyžiada zvolenie stavu izby.  

- 5c. Používateľ zruší vykonanie check-out.  
- 5c1. Systém nevykoná žiadnu zmenu. UC končí.


# Ostatné prípady použitia

- UC04 Vyhľadávanie hotelov  
- UC05 Filtrovanie izieb  
- UC06 Zobrazenie detailu izby  
- UC07 Zobrazenie rezervácie  
- UC08 Správa rezervácií  
- UC09 Zmena stavu izby  
- UC10 Evidencia platby  
- UC11 Správa hotelov  
- UC12 Pridanie izby do hotela  
- UC13 Úprava údajov hotela  
- UC14 Sledovanie dostupnosti izieb  
- UC15 Sledovanie obsadenosti hotela  
- UC16 Dynamická úprava cien
