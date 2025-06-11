# 📷 ChaKlaBe

**ChaKlaBe** is an ESP32-based Wi-Fi camera system with an Android companion app. It supports real-time JPEG streaming, snapshot capture, and time synchronization via HTTP. This version uses no Bluetooth and no Pi Zero.

---

## 🧱 System Overview

- **ESP32 (WROOM/WROVER)** running camera firmware
- **Android app** to control streaming and capture
- **SD card** for Wi-Fi config (`chaklabe.cfg`)
- **Wi-Fi Station Mode**: ESP32 connects to an existing network
- **HTTP Server**: for `/stream`, `/picture`, `/settime`, and `/status`

---

## 📁 Firmware Structure (`esp32/`)

| Component         | Description                                                 |
|------------------|-------------------------------------------------------------|
| `main.c`          | Initializes SD, reads config, starts Wi-Fi and HTTP server |
| `wifi_utils.c`    | Connects ESP32 to Wi-Fi using credentials from SD          |
| `sd_utils.c`      | Parses `/sdcard/chaklabe.cfg`                              |
| `http_server.c`   | Implements all HTTP endpoints                              |
| `camera_utils.c`  | Sets up OV2640 camera and captures frames                  |
| `stream_control.c`| Tracks stream state (`is_streaming`)                       |

---

## 🔌 Configuration

1. Create a file on SD card named `chaklabe.cfg`:
```
ssid=YourNetworkName
password=YourPassword
```
2. Insert SD card and power on the ESP32.

---

## 🌐 HTTP Endpoints

| Endpoint      | Method | Description                             |
|---------------|--------|-----------------------------------------|
| `/stream`     | GET    | Live JPEG stream                        |
| `/picture`    | GET    | Captures and returns single JPEG frame |
| `/status`     | GET    | Returns "ok" if server is active       |
| `/settime`    | POST   | Sends UNIX time as plain text          |

---

## 🛠 Build & Flash ESP32

```bash
idf.py set-target esp32
idf.py menuconfig        # Optional
idf.py build
idf.py -p COMx flash monitor
```

ESP-IDF 4.4+ is recommended.

---

## 📱 Android App (`android/`)

This companion app connects to the ESP32 over Wi-Fi to:

- View live video stream
- Capture images
- Sync time with ESP32
- Manually configure ESP32 IP

### 🔧 Structure

- `MainActivity.kt`: Entry point
- `ChaKlaBeNavGraph.kt`: Navigation flow
- `ChaKlaBeMainScreen.kt`: UI for main functions
- `ConfigScreen.kt`: Manual IP entry and settings
- `ChaKlaBeViewModel.kt`: Business logic
- `SettingsDataStore.kt`: Saves preferences (IP, stream state)

### 🔐 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

No location or Bluetooth permissions are needed.

### 📦 Build Instructions

- Open `android/` in Android Studio
- Sync Gradle and build
- Target: Android 8.0 (API 26) and above

---

## 🛑 Known Limitations

- Single stream session only
- Manual IP entry required
- No fallback Wi-Fi or BLE config
- Stream FPS depends on network and SD speed

---

## 🐾 Credits

ChaKlaBe is dedicated to **Angus**, the recovering feral cat whose asymmetric glare inspired the project logo and spirit.

