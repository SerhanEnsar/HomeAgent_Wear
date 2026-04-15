# AgentJee — Proje Tanım ve Devir Belgesi

Bu belge, projeyi devralan bir AI agent'ın sisteme tam hakimiyet sağlayabilmesi için
tüm bileşenleri, dosyaları, komutları ve çalışma akışlarını eksiksiz biçimde tanımlar.

---

## 1. Proje Genel Tanımı

**AgentJee**, bir Raspberry Pi üzerinde çalışan, aşağıdaki istemcilerden yönetilebilen
bir ev/yerel ağ kontrol sistemidir:

| İstemci | Teknoloji | Durum |
|---|---|---|
| Web Panel | FastAPI + Jinja2 + vanilla JS | ✅ Aktif |
| Telegram Bot | Python polling | ✅ Aktif |
| ESP32 Dokunmatik Ekran | Arduino C++ + TFT_eSPI | ✅ Aktif |
| Wear OS Saati (Galaxy Watch 4 Classic) | Kotlin + Jetpack Compose | 🔧 Geliştiriliyor |
| Mobil Uygulama (eski) | React Native / Expo Go | ⚠️ Eski, aktif değil |

---

## 2. Donanım

| Donanım | Detay |
|---|---|
| Sunucu | Raspberry Pi (Linux, ARM) |
| Ekran İstemci | ESP32 + TFT 240x320 dokunmatik ekran |
| Saat İstemci | Samsung Galaxy Watch 4 Classic (One UI Watch 6 / Wear OS 4 tabanlı) |
| Geliştirici Bilgisayar | MacBook (Apple Silicon M4) |

---

## 3. Dizin Yapısı

```
/home/serhanensar/HomeAgent/          ← Ana proje dizini (Raspberry Pi üzerinde)
├── app/
│   ├── main.py                      ← FastAPI ana uygulama
│   ├── docker_api.py                ← Docker container kontrolü
│   ├── telegram_control.py          ← Telegram bot polling loop
│   ├── notify_boot.py               ← Pi açılışında Telegram'a bildirim
│   └── telegram_state.txt           ← Telegram update_id kalıcı durumu
├── templates/
│   ├── index.html                   ← Dashboard sayfası
│   ├── files.html                   ← Dosya gezgini
│   ├── login.html                   ← Giriş sayfası
│   ├── profile.html                 ← Şifre değiştirme
│   ├── settings.html                ← Sistem ayarları
│   └── _nav.html                    ← Ortak navigasyon (include)
├── static/
│   └── logo.png                     ← Panel logosu
├── config.json                      ← Panel ayarları (refresh_interval_ms vb.)
├── .session_secret                  ← Session secret key (otomatik üretilir)
├── .auth_salt                       ← Şifre salt (otomatik üretilir)
└── .auth_hash                       ← Şifre hash (otomatik üretilir)

/Users/serhanensar/AndroidStudioProjects/HomeAgent/   ← Wear OS projesi (Mac'te)
└── app/src/main/
    ├── java/com/serhanensar/agentjee/presentation/
    │   └── MainActivity.kt          ← Tüm Wear OS UI ve API mantığı
    └── AndroidManifest.xml
```

---

## 4. Raspberry Pi — Servis Başlatma

### 4.1 SSH ile Pi'ye Bağlanma (Mac terminalinden)

```bash
ssh serhanensar@<PI_IP_ADRESI>
# veya hostname ile:
ssh serhanensar@AgentJee.local
```

SSH key kuruluysa şifresiz girer. Key kurulu değilse:
```bash
ssh-copy-id serhanensar@<PI_IP_ADRESI>
```

### 4.2 FastAPI Web Panel Başlatma

Pi üzerinde:
```bash
cd /home/serhanensar/HomeAgent
source venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Arka planda çalıştırmak için:
```bash
cd /home/serhanensar/HomeAgent
source venv/bin/activate
nohup uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload &
```

Panel adresi: `http://<PI_IP>:8000`
Varsayılan kullanıcı: `serhanensar`
Varsayılan şifre: `admin1234` (ilk kurulumda, sonradan değiştirilmeli)

### 4.3 Telegram Bot Başlatma

```bash
cd /home/serhanensar/HomeAgent
source venv/bin/activate
TG_TOKEN=<token> TG_CHAT_ID=<chat_id> python app/telegram_control.py
```

Arka planda:
```bash
nohup TG_TOKEN=<token> TG_CHAT_ID=<chat_id> python app/telegram_control.py &
```

### 4.4 Boot Bildirimi (Manuel Tetikleme)

```bash
cd /home/serhanensar/HomeAgent
source venv/bin/activate
TG_TOKEN=<token> TG_CHAT_ID=<chat_id> python app/notify_boot.py
```

### 4.5 Venv Kurulumu (ilk kurulum veya sıfırdan)

```bash
cd /home/serhanensar/HomeAgent
python3 -m venv venv
source venv/bin/activate
pip install fastapi uvicorn psutil docker python-multipart itsdangerous starlette
```

---

## 5. FastAPI — API Endpoint Listesi

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/` | Dashboard HTML |
| GET | `/login` | Giriş sayfası |
| POST | `/login` | Giriş işlemi |
| GET | `/logout` | Çıkış |
| GET | `/profile` | Profil sayfası |
| POST | `/profile/password` | Şifre değiştirme |
| GET | `/files` | Dosya gezgini HTML |
| GET | `/settings` | Ayarlar HTML |
| GET | `/api/status` | CPU, RAM, Disk, Sıcaklık (JSON) |
| GET | `/api/docker/containers` | Container listesi |
| POST | `/api/docker/{name}/start` | Container başlat |
| POST | `/api/docker/{name}/stop` | Container durdur |
| GET | `/api/files/devices` | Mount listesi |
| GET | `/api/files/list?mount=&path=` | Dizin içeriği |
| GET | `/api/files/download?mount=&path=` | Dosya indir |
| POST | `/api/files/rename` | Dosya/klasör yeniden adlandır |
| POST | `/api/files/delete` | Dosya/klasör sil |
| GET | `/api/settings/info` | IP, güç, fan bilgisi |
| GET | `/api/config` | Panel ayarları |
| POST | `/api/config` | Panel ayarları güncelle |
| POST | `/api/system/shutdown` | Sistemi kapat |
| POST | `/api/system/reboot` | Sistemi yeniden başlat |

**Not:** Tüm `/api/*` endpoint'leri login gerektirir. Session cookie ile kimlik doğrulama yapılır.

---

## 6. Wear OS Uygulaması — AgentJee

### 6.1 Proje Bilgileri

| Alan | Değer |
|---|---|
| Proje adı | HomeAgent (Android Studio'daki) |
| Uygulama adı | AgentJee |
| Package | `com.serhanensar.agentjee` |
| Konum (Mac) | `/Users/serhanensar/AndroidStudioProjects/HomeAgent/` |
| Dil | Kotlin |
| UI Framework | Jetpack Compose for Wear OS |
| Min SDK | API 30 (Wear OS 3) |
| Target SDK | API 36 (Android 16 / Wear OS 6) |
| Build config | Kotlin DSL (`build.gradle.kts`) |

### 6.2 Bağımlılıklar (`build.gradle.kts`)

```kotlin
implementation(libs.play.services.wearable)
implementation(platform(libs.compose.bom))
implementation(libs.ui)
implementation(libs.ui.graphics)
implementation(libs.ui.tooling.preview)
implementation(libs.compose.material3)
implementation(libs.compose.foundation)
implementation(libs.compose.ui.tooling)
implementation(libs.wear.tooling.preview)
implementation(libs.activity.compose)
implementation(libs.core.splashscreen)
// Eklenenler:
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### 6.3 AndroidManifest.xml — Gerekli İzinler

```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-feature android:name="android.hardware.type.watch" />
```

### 6.4 Uygulama Sayfaları (Page enum)

```
MENU           → Ana menü (bezel ile gezinme, dokunuşla seçim)
DASHBOARD      → CPU / RAM / Disk / Sıcaklık (3sn otomatik yenileme)
FILES          → Dosya gezgini (mount → dizin → dosya)
SETTINGS       → IP, güç, fan bilgisi
CONFIRM_REBOOT → Reboot onay ekranı
CONFIRM_SHUTDOWN → Shutdown onay ekranı
```

### 6.5 Navigasyon Mantığı

- **Bezel döndürme**: Menüde gezinme, sayfa içinde scroll
- **Saatin geri tuşu (BackHandler)**: Her zaman bir üst seviyeye veya ana menüye döner
- **FILES sayfasında geri**: currentPath → currentMount → MENU sırasıyla geriler

### 6.6 API Bağlantısı

```kotlin
const val PI_BASE_URL = "http://<PI_IP_ADRESI>:8000"
```

Pi IP'si değişirse bu sabit güncellenmeli ve uygulama yeniden derlenmelidir.
İlerleyen aşamada mDNS (`AgentJee.local`) veya dinamik IP çözümü eklenebilir.

### 6.7 Android Studio — Emülatör ve Deploy

**Emülatör başlatma:**
Android Studio → Device Manager → Wear OS XL Round (API 36, arm64) → ▶ Start

**Uygulamayı emülatöre yükleme:**
Üst çubukta hedef olarak `Wear OS XL Round` seçili iken → ▶ Run

**Gerçek saate deploy (Wi-Fi ADB):**
```bash
# Saatin IP'sini bul: Saat → Ayarlar → Wi-Fi → Bağlı ağ → Ayrıntılar
adb connect <SAAT_IP>:5555
adb devices   # bağlantıyı doğrula
```
Ardından Android Studio'da hedef olarak `samsung SM-R890` seç → ▶ Run

---

## 7. ESP32 Dokunmatik Ekran

### 7.1 Donanım

- ESP32 + ILI9341 tabanlı 240x320 TFT dokunmatik ekran
- TFT_eSPI kütüphanesi

### 7.2 Bağlantı Ayarları (kodda sabit)

```cpp
const char* ssid     = "Serhan Ensar S24 Ultra 2518";
const char* password = "3568230194Sb";
const char* apiBase  = "http://AgentJee.local:8000";
const char* apiKey   = "ASkfls5d6g4sd5g4s98gskfASLF958";
```

Pi IP'si mDNS (`AgentJee.local`) ile çözülür. Başarısız olursa fallback IP: `10.200.59.92`

### 7.3 ESP32 Sayfaları

```
PAGE_MENU      → Ana menü (4 buton: Dashboard, Files, Trash, Settings)
PAGE_DASHBOARD → CPU / RAM / Disk / Sıcaklık (bar grafikli, 3sn yenileme)
PAGE_FILES     → Dosya gezgini (mount → dizin → dosya, keyboard ile rename/mkdir)
PAGE_TRASH     → Çöp kutusu (restore / kalıcı sil)
PAGE_SETTINGS  → IP, Wi-Fi, hostname, kullanıcı, versiyon
```

### 7.4 Arduino IDE Yükleme

```
Arduino IDE → Tools → Board: ESP32 Dev Module
Tools → Port: /dev/cu.usbserial-XXXX (Mac'te)
Upload butonu (→)
```

---

## 8. Telegram Bot

### 8.1 Komutlar

| Komut / Buton | İşlev |
|---|---|
| `/ping` | Pi ayakta mı? |
| `/ip` | IP ve Wi-Fi SSID |
| `/status` | CPU, RAM, Disk, Sıcaklık |
| `/uptime` | Çalışma süresi |
| `/panel` | Panel URL |
| `/whoami` | Kullanıcı, host, UID |
| `/disk` | Disk kullanımı |
| `/reboot` | Yeniden başlat (onay popup) |
| `/shutdown` | Kapat (onay popup) |
| `/menu` | Klavye menüsü göster |
| `📊 Status` | Klavye butonu |
| `💾 Disk` | Klavye butonu |
| `👤 WhoAmI` | Klavye butonu |
| `🔄 Reboot` | Klavye butonu (onaylı) |
| `⛔ Shutdown` | Klavye butonu (onaylı) |
| `❎ Hide` | Klavyeyi gizle |

### 8.2 Durum Dosyası

```
/home/serhanensar/HomeAgent/app/telegram_state.txt
```
Son işlenen `update_id` burada tutulur. Bot yeniden başlatıldığında kaldığı yerden devam eder.

---

## 9. VS Code SFTP Ayarları

Mac'teki VS Code'dan Pi'ye dosya transferi için `sftp.json`:

```json
{
  "name": "Raspberry Pi",
  "host": "<PI_IP_ADRESI>",
  "protocol": "sftp",
  "port": 22,
  "username": "serhanensar",
  "remotePath": "/home/serhanensar/HomeAgent",
  "uploadOnSave": true,
  "useTempFile": false,
  "openSsh": false
}
```

Extension: **SFTP** by Natizyskunk (VS Code Marketplace)

---

## 10. Geliştirme Ortamı — Hazırlık Kontrol Listesi

Her geliştirme oturumunda aşağıdaki adımları sırayla uygula:

### Mac Tarafı

```bash
# 1. Pi'ye SSH bağlan
ssh serhanensar@<PI_IP_ADRESI>

# 2. (Opsiyonel) VNC ile masaüstü görüntüle
# VNC Viewer → <PI_IP_ADRESI>

# 3. VS Code'da SFTP bağlantısını aç
# Cmd+Shift+P → SFTP: Connect
```

### Pi Tarafı (SSH'ta)

```bash
# 1. Proje dizinine gir
cd /home/serhanensar/HomeAgent

# 2. Venv'i aktif et
source venv/bin/activate

# 3. Web panel'i başlat
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

# 4. (Ayrı terminal) Telegram bot'u başlat
TG_TOKEN=<token> TG_CHAT_ID=<chat_id> python app/telegram_control.py
```

### Android Studio (Wear OS geliştirme)

```
1. Android Studio aç
2. HomeAgent projesini aç (/Users/serhanensar/AndroidStudioProjects/HomeAgent)
3. Gradle sync bekle
4. Device Manager'dan Wear OS XL Round emülatörünü başlat
   VEYA: adb connect <SAAT_IP>:5555
5. Run ▶ ile deploy et
```

---

## 11. Kimlik Doğrulama Sistemi (Web Panel)

- Tek kullanıcı: `serhanensar`
- Şifre: PBKDF2-HMAC-SHA256 ile hash'lenir, 200.000 iterasyon
- Salt ve hash ayrı dosyalarda: `.auth_salt`, `.auth_hash`
- Session: `itsdangerous` / Starlette `SessionMiddleware`
- Session secret: `.session_secret` dosyasında kalıcı tutulur
- İlk kurulumda default şifre: `admin1234` → Profile sayfasından değiştirilmeli

---

## 12. Planlanan / Eksik Özellikler

| Özellik | Durum | Not |
|---|---|---|
| Wear OS - Docker kontrol | ❌ Yok | İleride eklenecek |
| Wear OS - mDNS IP çözümü | ❌ Yok | Şu an sabit IP |
| Pi Panel - CORS middleware | ⚠️ Eksik | Wear OS için eklenecek |
| FastAPI - `/api/files/mkdir` | ❌ Yok | ESP32 kodu kullanıyor ama endpoint yok |
| FastAPI - `/api/files/trash` | ❌ Yok | ESP32 kodu kullanıyor ama endpoint yok |
| FastAPI - `/api/files/move` | ❌ Yok | ESP32 kodu kullanıyor ama endpoint yok |
| FastAPI - `/api/files/copy` | ❌ Yok | ESP32 kodu kullanıyor ama endpoint yok |
| FastAPI - `/api/info` | ✅ Aktif | ESP32 settings için kullanılıyor |

---

## 13. Önemli Notlar

- Pi hostname: `AgentJee.local` (mDNS)
- Web panel port: `8000`
- Tüm API'ler session cookie ile korumalı — dış istemciler (Wear OS, ESP32) için
  ya cookie yönetimi eklenmeli ya da API key auth sistemi kurulmalı
- ESP32'de `apiKey` var ama FastAPI'de bu key henüz doğrulanmıyor
- Wear OS uygulaması şu an Pi'ye session olmadan istek atıyor —
  `/api/status` gibi endpoint'ler login gerektirdiği için **401 dönecek**;
  bu sorun çözülmeden dashboard verisi gelmez
