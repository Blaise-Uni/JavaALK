# Java alkalmazások – beadandó (Spring Boot, Java 25)

Böngészős alkalmazás: MNB árfolyamok (SOAP + Chart.js) és Forex kereskedés (Oanda v20 API).

## 1. Futtatás IntelliJ IDEA-ban
1. **File → Open** → válaszd ki a projekt `pom.xml` fájlját → *Open as Project*.
2. **File → Project Structure → Project → SDK**: válassz **JDK 25**-öt (ha nincs: *Add SDK → Download JDK…* → 25).
   A *Language level* legyen **25**. A Maven az IntelliJ-be beépítve érkezik, külön telepíteni nem kell.
3. Írd át az `src/main/resources/application.properties` fájlban:
   - `app.authors` – nevünk és kódunk (ez kerül minden oldal aljára),
   - `oanda.token` és `oanda.account-id` – az Oanda **demó (practice)** fiók adatai.
4. Futtasd a `BeadandoApplication` osztályt (zöld nyíl), majd nyisd meg: <http://localhost:8081>

> **XAMPP:** ehhez a feladathoz nem kell (nincs adatbázis a kötelező részekben), nem is zavarja.
> Az alkalmazás a **8081**-es porton fut, hogy ne ütközzön a XAMPP Apache (80/443) és Tomcat (8080) szolgáltatásaival.
> A XAMPP MySQL-re (3306) csak a +5 pontos React/CRUD résznél lenne szükség, de ott a feladat H2-t ír elő.

## 2. Oanda demó token
Oanda → *fxTrade Practice* fiók → **Manage API Access** → *Generate* token.
A számlaazonosító (pl. `101-004-1234567-001`) a belépés után a számla adatainál látszik.
Csak demó fiókot használj, éles tokent soha ne tegyél a GitHubra.

## 3. beadando.jar készítése
IntelliJ jobb oldali **Maven** panel → *beadando → Lifecycle → package* (vagy `clean` majd `package`).
Eredmény: `target/beadando.jar`. Futtatás parancssorból (Java 25 alatt):

```
java -jar beadando.jar
```
A kész jar-t másold a GitHub projekt főmappájába.

## 4. Pontozási lista → hol van megvalósítva
| Feladat | Hely |
|---|---|
| Reszponzív téma (Bootswatch *Lux*, Bootstrap 5) | `templates/fragments/layout.html` |
| Főoldal, lábléc (név + Neptun) | `templates/index.html`, `app.authors` |
| SOAP menü | `MnbService`, `soap.html` (Chart.js grafikon) |
| Forex-account / AktÁr / HistÁr / Nyit / Poz / Zár | `OandaService`, `MainController`, `forex-*.html` |

## 5. GitHub és dokumentáció
- Mindketten commitoljuk, legalább 5 részlépésben fejenként; a repó legyen nyilvános.
- A dokumentáció (min. 10 oldal, képernyőképekkel, PDF, `Név-NeptunKód.pdf`): a 2. oldalon a GitHub URL és az Oanda belépési adatok;
  írjuk le a témát (Bootswatch Lux), a feladatok megvalósítását és azt, hogy ki melyik részt készítette.

## 6. Bónusz: Teszt menü (JUnit + MockMvc)
`src/test/java/hu/beadando/` – futtatás: jobb klikk a `src/test/java` mappán → *Run 'All Tests'* (vagy `mvn test`).
Az alkalmazás a `/teszt` oldalon érhető el (négyműveletes számológép).

## 7. Bónusz: React + Axios + H2 (fájlba mentett adatbázis)
- Menü: **React** (`/react/index.html`). REST API: `/api/vevok` (`hu.beadando.crud` csomag).
- Adatbázis: H2 fájl a **`C:\data\beadando`** helyen (az `application.properties` tartalmazza, a mappát az H2 létrehozza).
  H2 konzol: <http://localhost:8081/h2-console>, JDBC URL: `jdbc:h2:file:C:/data/beadando`, felhasználó: `sa`, jelszó üres.
- A React forrás a `frontend/` mappában van (Vite, **build-eléses** módszer, nem Babel/standalone).
  A Maven build (`compile` / `package`) automatikusan letölti a Node-ot, lefuttatja az `npm install` és `npm run build` parancsot,
  az eredmény a `target/classes/static/react` mappába kerül, és a jarba is bekerül.
  **Első indítás előtt** futtasd a Maven panelen a *Lifecycle → compile* (vagy *package*) lépést, utána indíthatod a `BeadandoApplication`-t.
  Gyors React fejlesztéshez: `cd frontend`, `npm install`, `npm run dev` (http://localhost:5173, az /api hívásokat a 8081-es Spring Boot kapja).
- **Másik tábla használata:** a feladat a kiadott Google Drive adatbázisok egyikét kéri. A minta a `Vevo` entitás;
  másik tábla esetén írd át a `Vevo.java` mezőit, a `VevoApiController` `update` metódusát és a `frontend/src/App.jsx` `FIELDS` tömbjét.
