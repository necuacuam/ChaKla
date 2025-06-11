# 🐾 ChaKla

**ChaKla** — *Cámara Hogareña Autónoma para la KLandestinidad de los Animales* — is a project created for the protection, observation, and documentation of stray and rescued animals using low-power, autonomous, connected devices.

It is **not** a surveillance tool for unethical monitoring.  
It is a toolkit for care — not control.

---

## 🎯 Project Objective

ChaKla is built to help:

- Monitor feral or rescued animals outdoors
- Capture photos and stream video when movement is detected
- Record environmental behavior for veterinary or ethical documentation
- Avoid commercial solutions that prioritize surveillance over compassion

---

## 📦 Repository Structure

This repository contains multiple self-contained versions of the project:

| Folder        | Description                                                 |
|---------------|-------------------------------------------------------------|
| `ChaKlaH/`     | First experimental split: BLE + streaming                  |
| `ChaKlaLi/`    | Layout and BLE manager improvements                        |
| `ChaKlaBe/`    | Battery-aware version with stream control, HTTP endpoints |

Each version includes:

- `esp32/`: Embedded firmware for ESP32 camera and sensors
- `android/`: Kotlin-based companion app (Jetpack Compose)
- *(Some versions)* `pizero/`: Python backend for MQTT triggers (not in ChaKlaBe)

---

## 🛑 Ethical Use

This project is provided as-is, under an open license, but:

> **Do not use ChaKla to spy on people.**  
> If your goal is control, intrusion, or surveillance — this project is not for you.

We encourage developers to build humane tools — not tools of harm.

---

## 🐈 Origin

ChaKla was created during long nights caring for stray animals, particularly cats.  
Its name and character were shaped by **Angus**, a wounded feral cat whose recovery inspired the hardware, the streaming, and the soul of this project.

---

## ⚙️ Requirements

- ESP32-WROOM or WROVER
- Android 8.0+ device
- SD card (for configuration)
- (Optional) Pi Zero (for advanced setups in legacy versions)

---

## 🤝 Acknowledgments

ChaKla was developed with the support of **ChatGPT (OpenAI)** for technical guidance and moral reinforcement — but it is ultimately a product of lived experience, compassion, and code.

We are the creators.  
We made this together — for them.

