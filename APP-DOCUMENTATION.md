# SER Inventarios - Mobile App Documentation

## Overview

A unified Android app that consolidates two existing client apps (**SERV122** for inventory counting and **ActivoFijoV200RFID** for fixed asset management) into a single modern application.

- **Platform:** Android (Min SDK 24, Target SDK 35)
- **Language:** Kotlin + Jetpack Compose + Material 3
- **Architecture:** MVVM, single-activity, Compose Navigation
- **Stack:** Hilt (DI), Room (DB), Retrofit+Moshi (API), CameraX+ML Kit (barcode), WorkManager (sync), DataStore (prefs), Coil (images)

---

## Page Structure

| # | Screen | Route | Bottom Nav | Description |
|---|--------|-------|:----------:|-------------|
| 1 | Login | `login` | - | User authentication + server URL config |
| 2 | Empresa/Sucursal Selection | `empresa_selection` | - | Post-login onboarding wizard (2 steps) |
| 3 | Dashboard | `dashboard` | Inicio | Home with stats, sync, network status |
| 4 | Inventario List | `inventario_list` | Inventario | Inventory session management |
| 5 | Inventario Capture | `inventario_capture/{sessionId}` | - | Product scanning & counting |
| 6 | Activo Fijo List | `activofijo_list` | Activo Fijo | Asset session management |
| 7 | Activo Fijo Capture | `activofijo_capture/{sessionId}` | - | Asset scanning, photos, status |
| 8 | Asset Catalog | `asset_catalog/{sessionId}` | - | Browse assets by category |
| 9 | Asset Search | `asset_search/{sessionId}` | - | Search assets by barcode/code |
| 10 | Cross-Count Compare | `crosscount/{s1}/{s2}` | - | Side-by-side session comparison |
| 11 | Barcode Scanner | `scanner/{returnRoute}` | - | CameraX + ML Kit fullscreen scanner |
| 12 | RFID Capture | `rfid_capture` | - | RFID tag reader interface |
| 13 | Profile (Mi Pagina) | `profile` | Mi Pagina | User info, sync, logout |
| 14 | Settings | `settings` | Ajustes | Preferences, printer, capture options |

---

## Navigation Flowchart

```
                        ┌─────────────┐
                        │  APP START  │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │ Check Auth  │
                        │(token+user) │
                        └──────┬──────┘
                               │
                 ┌─────────────┴─────────────┐
                 │                           │
           No token/user              Token + User
                 │                           │
                 ▼                           ▼
           ┌──────────┐          ┌───────────────────┐
           │  LOGIN   │          │ EMPRESA SELECTION  │
           │          │─────────>│ (auto-skip if set) │
           └──────────┘          └─────────┬─────────┘
                                           │
                          ┌────────────────┴────────────────┐
                          │                                 │
                    Already configured               Not configured
                    (auto-skip)                            │
                          │                     ┌──────────▼──────────┐
                          │                     │ Step 1: Select      │
                          │                     │        Empresa      │
                          │                     │ Step 2: Select      │
                          │                     │        Sucursal     │
                          │                     │ Step 3: Confirm     │
                          │                     └──────────┬──────────┘
                          │                                │
                          └────────────┬───────────────────┘
                                       ▼
    ┌──────────────────────────────────────────────────────────────┐
    │                        DASHBOARD                             │
    │  User Avatar | Stats Cards | Sync Status | Network Monitor  │
    │  [Inventarios: N] [Activos: N] [Encontrados/No/Agregados]   │
    │  [Sincronizar ahora]                                         │
    └──────────────────────────────────────────────────────────────┘
                               │
     ══════════════════════════╪═══════════════════════════════════
     BOTTOM NAVIGATION BAR     │
     ┌────────┬────────┬───────┴──┬────────────┬─────────┐
     │ Inicio │Inventar│Mi Pagina │ Activo Fijo│ Ajustes │
     │  (Home)│  (Inv) │ (Person) │  (Scanner) │(Settings│
     └───┬────┴───┬────┴────┬─────┴─────┬──────┴────┬────┘
         │        │         │           │           │
         ▼        ▼         ▼           ▼           ▼
     Dashboard  Inv.List  Profile   A.F.List    Settings
     ══════════════════════════════════════════════════════


     INVENTARIO FLOW
     ═══════════════

     ┌────────────────────┐
     │  INVENTARIO LIST   │
     │                    │
     │  Session cards     │
     │  (name, date,      │
     │   empresa, count)  │
     │                    │
     │  [+] Create new    │
     └─────────┬──────────┘
               │ tap session
               ▼
     ┌────────────────────────────────┐
     │     INVENTARIO CAPTURE         │
     │                                │
     │  [Scan] Barcode ───────────────┼──> BARCODE SCANNER
     │  Description (auto-fill)       │        (CameraX)
     │  Quantity                      │         │
     │  Location                      │    returns barcode
     │  Lote (autocomplete dropdown)  │<────────┘
     │  Caducidad (auto-fill)         │
     │  Factor (optional)             │
     │  Serial Number (optional)      │
     │  ───────────────────────────── │
     │  Saved registros list          │
     │  ───────────────────────────── │
     │  Stats: CONTEO | REGISTROS     │
     │  [Export CSV/Excel]            │
     └────────────────────────────────┘


     ACTIVO FIJO FLOW
     ════════════════

     ┌────────────────────┐
     │  ACTIVO FIJO LIST  │
     │                    │
     │  Session cards     │
     │  Compare mode      │──────────> CROSS-COUNT
     │  [+] Create new    │            (2 sessions
     └─────────┬──────────┘             side-by-side)
               │ tap session
               ▼
     ┌────────────────────────────────┐
     │    ACTIVO FIJO CAPTURE         │
     │                                │
     │  [Scan] [Catalog] [Search]     │
     │       │      │        │        │
     │       │      │        └────────┼──> ASSET SEARCH
     │       │      └─────────────────┼──> ASSET CATALOG
     │       └────────────────────────┼──> BARCODE SCANNER
     │                                │
     │  ┌─ Status Chips ────────────┐ │
     │  │ Found│NotFound│Added│Transf│ │
     │  └──────────────────────────┘ │
     │                                │
     │  Barcode                       │
     │  Description (auto-fill)       │
     │  Category (dropdown)           │
     │  Brand (autocomplete)          │
     │  Model / Color / Serie         │
     │  Ubicacion                     │
     │  Area (autocomplete)           │
     │  Tag Nuevo                     │
     │  Serie Revisado                │
     │  Comentarios                   │
     │                                │
     │  ┌─ Photos ─────────────────┐ │
     │  │ [Photo 1] [Photo 2] [3]  │ │
     │  └──────────────────────────┘ │
     │                                │
     │  [Save] [Print] [Transfer]     │
     │  ───────────────────────────── │
     │  Session Stats Dashboard       │
     │  (Pie chart + stat badges)     │
     │  ───────────────────────────── │
     │  Saved registros list          │
     │  [Export CSV/Excel]            │
     └────────────────────────────────┘


     SUPPORT SCREENS
     ═══════════════

     ┌──────────────────┐    ┌──────────────────────────┐
     │    PROFILE        │    │       SETTINGS            │
     │                  │    │                          │
     │  User avatar     │    │  Empresa dropdown        │
     │  Name / Role     │    │  Sucursal dropdown       │
     │  Company         │    │  ─── Impresora ────────  │
     │  Sucursal        │    │  Bluetooth printer       │
     │  Last sync       │    │  ─── Opciones Captura ── │
     │  [Sync]          │    │  9 toggle switches       │
     │  [Logout]        │    │  ─── Catalogos ──────── │
     │                  │    │  Import from file        │
     └──────────────────┘    │  [Sync] [Logout]        │
                             └──────────────────────────┘

     ┌──────────────────┐    ┌──────────────────────────┐
     │ BARCODE SCANNER  │    │     RFID CAPTURE          │
     │                  │    │                          │
     │  CameraX preview │    │  Connect/Disconnect      │
     │  ML Kit detect   │    │  Power slider (0-30)     │
     │  Frame overlay   │    │  Start/Stop inventory    │
     │  Flash toggle    │    │  Tag list (EPC, RSSI)    │
     │  Auto-return     │    │  Match status            │
     │  on detection    │    │  Filter: All/Matched     │
     └──────────────────┘    └──────────────────────────┘

     ┌──────────────────┐    ┌──────────────────────────┐
     │  ASSET CATALOG   │    │    ASSET SEARCH           │
     │                  │    │                          │
     │  Stats header    │    │  Search by barcode/code  │
     │  Search bar      │    │  Catalog info card       │
     │  Category list   │    │  Captured registro       │
     │  Product list    │    │  details + photos        │
     │  Captured status │    │                          │
     └──────────────────┘    └──────────────────────────┘

     ┌──────────────────────────────────────┐
     │          CROSS-COUNT COMPARE         │
     │                                      │
     │  Session 1 Name  │  Session 2 Name   │
     │  ────────────────┼────────────────── │
     │  Registro list   │  Registro list    │
     │  Status counts   │  Status counts    │
     │                  │                   │
     └──────────────────────────────────────┘
```

---

## Features

### 1. Authentication & Onboarding

| Feature | Description |
|---------|-------------|
| Login | Username/password authentication via API |
| Offline login | Falls back to cached user if no network |
| Server URL config | Configurable API endpoint ("Conexion" dialog) |
| Empresa selection | Post-login wizard to choose company |
| Sucursal selection | Second step to choose branch within company |
| Auto-skip | Skips selection if empresa+sucursal already saved |
| Token validation | Checks both token AND user exist on startup |
| 401 handling | Auto-logout on expired/invalid token |
| Demo login | `demo/demo` credentials for testing (to be removed) |

### 2. Inventory Counting (Inventario)

| Feature | Description |
|---------|-------------|
| Session management | Create/view inventory counting sessions |
| Barcode scanning | CameraX camera scanner + hardware scanner support |
| Product lookup | Auto-fill description from local product catalog |
| Lote autocomplete | Dropdown suggestions from LoteEntity by barcode |
| Caducidad auto-fill | Expiry date populated from selected lote |
| Factor field | Box counting mode (quantity x factor) |
| Serial number | Optional serial number capture per item |
| Forced codes | Allow manual entry when product not in catalog |
| Conteo toggle | Switch between unit counting and box counting |
| Location field | Per-registro location/aisle tracking |
| Stats bar | Real-time totals: quantity, registro count, factor |
| Export | CSV and Excel export of session data |
| Offline capture | Save locally with background sync |

### 3. Fixed Asset Management (Activo Fijo)

| Feature | Description |
|---------|-------------|
| Session management | Create/view asset counting sessions |
| Barcode scanning | CameraX camera + hardware scanner |
| Product lookup | Auto-fill from global product catalog |
| Rich form | 12+ fields: description, category, brand, model, color, serie, ubicacion, area, tag nuevo, serie revisado, comentarios |
| 4 status types | Found, Not Found, Added, Transferred |
| Status filter chips | Quick status selection with colored chips |
| 3 photo slots | Capture up to 3 photos per asset via CameraX |
| Brand autocomplete | Suggestions from existing session data |
| Area autocomplete | Suggestions from existing session data |
| Transfer dialog | Move asset to different sucursal with confirmation |
| Session dashboard | Pie chart + stat badges (found/not found/added/transferred) |
| Asset catalog | Browse products by category with captured indicators |
| Asset search | Search by barcode, view full details + photos |
| Cross-count | Side-by-side comparison of 2 sessions |
| Thermal printing | Print asset labels via Bluetooth (ESC/POS + CPCL) |
| Edit/Delete | Modify or remove captured registros with confirmation |
| Export | CSV and Excel export |

### 4. RFID Integration

| Feature | Description |
|---------|-------------|
| Reader connection | Connect via serial port (/dev/ttyS4, 115200 baud) |
| Inventory scan | Start/stop RFID tag reading |
| Power control | Adjustable power level (0-30) |
| Tag list | Display EPC, RSSI, read count per tag |
| Tag matching | Match RFID tags to assets in catalog |
| Filter | All / Matched / Unmatched views |
| Sync | Upload matched tags to server |

### 5. Profile & Settings

| Feature | Description |
|---------|-------------|
| User profile | Avatar with initials, name, role, company, sucursal |
| Empresa/sucursal switch | Change active company and branch |
| Bluetooth printer | Select and configure thermal printer |
| **9 capture options** | Toggle switches for capture behavior |
| - Validate catalog | Reject barcodes not in product catalog |
| - Allow forced codes | Permit manual entry for unknown products |
| - Capture factor | Show factor field in inventario |
| - Capture lotes | Show lote autocomplete field |
| - Capture serial | Show serial number field |
| - Capture negatives | Allow negative quantities |
| - Capture zeros | Allow zero quantities |
| - Capture GPS | Record lat/lng with registros |
| - Conteo unidad | Default unit counting mode |
| Catalog import | Import products from Excel/CSV file |
| Manual sync | Trigger full sync on demand |
| Logout | Clear session data and return to login |

### 6. Data Sync & Offline

| Feature | Description |
|---------|-------------|
| Background sync | WorkManager runs every 15 minutes |
| Manual sync | Available from Dashboard, Profile, Settings |
| Upload queue | Pending registros, no-encontrados, traspasos, RFID tags |
| Image upload | Photos converted to base64 for server upload |
| Download sync | Empresas, sucursales, products (paginated), lotes, sessions |
| Offline mode | Full operation without network (negative IDs for new sessions) |
| Network monitor | Real-time online/offline status indicator |
| Retry logic | Up to 3 retry attempts on failed uploads |
| Last sync timestamp | Displayed on Dashboard and Profile |

### 7. Hardware Support

| Hardware | Integration |
|----------|-------------|
| Camera | CameraX for barcode scanning + photo capture |
| ML Kit | Google ML Kit barcode detection (all formats) |
| Hardware scanner | KeyEvent interception in MainActivity (Zebra, Honeywell, etc.) |
| Bluetooth printer | ESC/POS and CPCL thermal label printing |
| RFID reader | Native SDK via serial port connection |
| GPS | Location capture for asset registros |

---

## Data Model

### Database Entities (14 tables)

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   UserEntity    │     │  EmpresaEntity   │     │ SucursalEntity   │
│─────────────────│     │──────────────────│     │──────────────────│
│ id              │     │ id               │     │ id               │
│ usuario         │     │ nombre           │     │ empresaId        │
│ nombres         │     │ codigo           │     │ nombre           │
│ email           │     │ eliminado        │     │ codigo           │
│ rolId           │     └──────────────────┘     │ direccion        │
│ rolNombre       │                               └──────────────────┘
│ empresaIds      │
│ accesoApp       │
└─────────────────┘

┌─────────────────────┐     ┌──────────────────────────┐
│  InventarioEntity   │     │ InventarioRegistroEntity  │
│─────────────────────│     │──────────────────────────│
│ id                  │     │ id                       │
│ empresaId           │     │ serverId                 │
│ sucursalId          │     │ sessionId (FK)           │
│ nombre              │     │ codigoBarras             │
│ tipo                │     │ descripcion              │
│ estado              │     │ cantidad                 │
│ fechaCreacion       │     │ ubicacion                │
│ empresaNombre       │     │ lote                     │
│ sucursalNombre      │     │ caducidad                │
└─────────────────────┘     │ factor                   │
                            │ numeroSerie              │
                            │ sincronizado             │
                            │ fechaCaptura             │
                            │ usuarioId                │
                            └──────────────────────────┘

┌──────────────────────────┐     ┌────────────────────────────────┐
│ ActivoFijoSessionEntity  │     │  ActivoFijoRegistroEntity      │
│──────────────────────────│     │────────────────────────────────│
│ id                       │     │ id                             │
│ empresaId                │     │ serverId                       │
│ sucursalId               │     │ sessionId (FK)                 │
│ nombre                   │     │ codigoBarras                   │
│ estado                   │     │ descripcion                    │
│ fechaCreacion            │     │ categoria, marca, modelo       │
│ empresaNombre            │     │ color, serie, ubicacion        │
│ sucursalNombre           │     │ comentarios, tagNuevo          │
└──────────────────────────┘     │ serieRevisado                  │
                                 │ statusId (1=Found, 2=NotFound, │
                                 │   3=Added, 4=Transferred)      │
                                 │ imagenUri1, imagenUri2,        │
                                 │   imagenUri3                   │
                                 │ latitud, longitud              │
                                 │ sincronizado, fechaCaptura     │
                                 │ usuarioId                      │
                                 └────────────────────────────────┘

┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ ProductoEntity   │     │   LoteEntity     │     │  StatusEntity    │
│──────────────────│     │──────────────────│     │──────────────────│
│ id               │     │ id               │     │ id               │
│ empresaId        │     │ empresaId        │     │ status           │
│ codigoBarras     │     │ productoId       │     │ nombre           │
│ descripcion      │     │ codigoBarras     │     │ (1=Encontrado,   │
│ categoria        │     │ lote             │     │  2=No Encontrado,│
│ marca            │     │ caducidad        │     │  3=Agregado,     │
│ modelo           │     │ existencia       │     │  4=Traspasado)   │
│ color, serie     │     └──────────────────┘     └──────────────────┘
│ sucursalId       │
└──────────────────┘

┌─────────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ NoEncontradoEntity  │     │ TraspasoEntity   │     │  RfidTagEntity   │
│─────────────────────│     │──────────────────│     │──────────────────│
│ id                  │     │ id               │     │ id               │
│ serverId            │     │ serverId         │     │ epc              │
│ sessionId           │     │ registroId       │     │ rssi             │
│ activoId            │     │ sucursalOrigenId │     │ readCount        │
│ usuarioId           │     │ sucursalDestinoId│     │ sessionId        │
│ lat, lng            │     │ sincronizado     │     │ timestamp        │
│ sincronizado        │     │ fechaCaptura     │     │ matched          │
│ fechaCaptura        │     └──────────────────┘     │ matchedRegistroId│
└─────────────────────┘                               │ sincronizado     │
                                                      └──────────────────┘

┌──────────────────┐
│ SyncQueueEntity  │
│──────────────────│
│ id               │
│ type             │
│ entityId         │
│ payload          │
│ status (pending/ │
│  uploading/done/ │
│  error)          │
│ errorMessage     │
│ retryCount       │
│ createdAt        │
└──────────────────┘
```

---

## API Endpoints (18 total)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/login` | Authenticate user, get token |
| POST | `/api/logout` | Invalidate token |
| GET | `/api/me` | Get current user info |
| GET | `/api/empresas` | List all empresas |
| GET | `/api/sucursales/{empresaId}` | List sucursales for empresa |
| GET | `/api/productos/{empresaId}` | Paginated products (500/page) |
| GET | `/api/lotes/{empresaId}` | List lotes for empresa |
| GET | `/api/statuses` | List asset statuses |
| GET | `/api/inventarios` | List inventario sessions |
| POST | `/api/inventarios` | Create new inventario session |
| POST | `/api/inventarios/upload` | Upload inventario registros |
| GET | `/api/activo-fijo` | List activo fijo sessions |
| POST | `/api/activo-fijo` | Create new activo fijo session |
| GET | `/api/activo-fijo/productos` | Paginated asset products |
| POST | `/api/activo-fijo/upload` | Upload activo fijo registros |
| POST | `/api/activo-fijo/no-encontrados` | Upload not-found records |
| POST | `/api/activo-fijo/traspasos` | Upload transfer records |
| POST | `/api/activo-fijo/rfid-tags` | Upload RFID tag data |
| POST | `/api/activo-fijo/imagen` | Upload asset photo (multipart) |

---

## Roles & Permissions

| Role | ID | Access |
|------|----|--------|
| **Super Admin** | 1 | Full access to all empresas, all features |
| **Supervisor** | 2 | Full access within assigned empresas |
| **Capturista** | 3 | Capture only within assigned empresa+sucursal |
| **Supervisor Invitado** | 4 | Limited access for transfer operations |

---

## Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation (single-activity) |
| DI | Hilt |
| Local DB | Room (14 entities, 10 DAOs) |
| API | Retrofit2 + Moshi + OkHttp3 |
| Auth | Laravel Sanctum (Bearer token) |
| Background | WorkManager (15-min sync interval) |
| Preferences | DataStore (19 preference keys) |
| Camera | CameraX + ML Kit Barcode |
| Images | Coil |
| Printing | Bluetooth ESC/POS + CPCL |
| RFID | Native SDK (BaseReader, serial port) |
| Export | CSV + Excel (OpenXML) |
