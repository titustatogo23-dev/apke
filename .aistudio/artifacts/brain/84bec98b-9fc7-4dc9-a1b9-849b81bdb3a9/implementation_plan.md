# E-Health SATUSEHAT & Appointment Calendar System

An Indonesian public healthcare mobile application (*Sistem Pelayanan Kesehatan Masyarakat & Rekam Medis Elektronik*) integrating standardized SATUSEHAT patient registration (RME), medical history records, and a comprehensive appointment scheduling calendar with real-time slot selection and verified digital queue confirmation passes.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture Decisions for Review:**
> - **Calendar & Slot Booking Flow**: Users can navigate months and dates in an interactive Material 3 calendar, filter clinics (Poli Umum, Gigi & Mulut, KIA/Kandungan, Penyakit Dalam, Anak, Mata, Lansia), view dynamically updated quotas and time slots (Morning, Afternoon, Evening), and receive immediate booking confirmation with queue number (e.g., `UM-014`) and QR check-in pass.
> - **SATUSEHAT RME Persistence**: Powered by a robust on-device Room SQLite database supporting complete patient profiles (16-digit NIK verification, BPJS Kesehatan/insurance validation, allergies, and chronic conditions) which auto-fills subsequent appointment bookings.
> - **Bilingual / Indonesian National Standard (Kemenkes/SATUSEHAT)**: Uses official Indonesian health terminology (*Puskesmas*, *RSUD*, *BPJS Kesehatan*, *Nomor Antrean*, *Jadwal Dokter*, *Rekam Medis Elektronik*).

---

## 1. Overview & Core Concept

- **What It Does**: Digitizes public health center (*Puskesmas* and *RSUD*) registration and scheduling. Patients register their demographic and medical profile once according to SATUSEHAT RME standards, then browse healthcare clinics, check doctor availability on an intuitive interactive calendar, reserve morning or afternoon consultation slots, and track confirmed visits with digital queue tickets.
- **Target Audience / Persona**: Indonesian citizens and patients seeking outpatient care at community health centers and regional general hospitals, requiring hassle-free queue bookings without waiting in long early-morning physical queues.
- **Key Value**: Eliminates physical queuing bottlenecks, ensures accurate digital health records with chronic disease and allergy tracking, and provides clear appointment visibility through an interactive calendar.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Fast Actions**:
   - Quick overview of patient's digital health identity (SATUSEHAT ID / NIK chip).
   - Card showing upcoming confirmed appointment with live status indicator (Terjadwal / Selesai / Dibatalkan).
   - Fast action buttons: *Daftar Janji Temu Baru*, *Jadwal & Kalender*, *Rekam Medis (RME)*, *Kartu Pasien Digital*.

2. **Interactive Calendar & Appointment Booking**:
   - **Interactive Month & Week Calendar View**: Visual day cells showing dots for confirmed bookings and green badges for available clinic days.
   - **Date & Slot Selection**: Selecting a date reveals available clinics and doctors with remaining quotas (e.g. "Sisa Kuota: 8/20").
   - **Time Slot Chooser**: Segmented chips for *Sesi Pagi (08:00 - 10:00)*, *Sesi Siang (10:30 - 12:30)*, *Sesi Sore (13:30 - 15:30)*.
   - **Payment & Guarantor Selection**: Toggle between *Umum / Mandiri*, *BPJS Kesehatan* (with automated 13-digit card validation), and *Asuransi Swasta*.

3. **Booking Confirmation & Digital Queue Pass**:
   - Immediate confirmation modal and card with generated Ticket Code, Poli, Doctor, Estimated Service Hour, and QR Code for on-site kiosk scanning.
   - Ability to add reminder, reschedule, or cancel with confirmation.

4. **Rekam Medis Elektronik (RME) & Profil Kesehatan (Modul 1 & 2)**:
   - Full 3-section stepper matching user specifications:
     - *Modul 1: Identitas & Data Kependudukan* (16-digit NIK, Nama Lengkap, Tempat & Tanggal Lahir, Jenis Kelamin, Golongan Darah, WhatsApp, Alamat).
     - *Modul 2: Riwayat Kesehatan Mandiri* (Penyakit Kronis: Diabetes, Hipertensi, Asma, Jantung; Alergi Obat toggle with conditional specification input).
     - *Modul 3: Data Penjamin & Faskes Favorit* (Puskesmas Sehat, RSUD Madani, dll).

### Visual Identity & Theme

- **Aesthetic Direction**: Professional, reassuring, clean Indonesian public healthcare design (inspired by SatuSehat, BPJS Mobile JKN, and modern health apps).
- **Color Palette**:
  - Primary Teal/Green: `#00897B` / `#198754` (Official health & hygiene tone)
  - Primary Container / Soft Sage: `#E0F2F1` / `#E8F5E9`
  - Accent / Urgency: `#FF9800` (Amber for pending/upcoming queues) and `#E53935` (Crimson for drug allergy warnings)
  - Surface Background: `#F8FAF9` (Crisp, clean neutral)
  - Card & Container Surface: `#FFFFFF` with soft 1.5dp elevation & 16dp rounded corners
- **Typography**: Clean humanist typography with bold numerals for queue codes, clear form labels, and accessible contrast ratios.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Interactive Compose Calendar with Custom Slot Engine**
  - *Chosen Approach*: Implement a lightweight, responsive Compose calendar grid allowing day-by-day navigation, slot filtering, and appointment dots, without relying on bloated external calendar libraries.
  - *Why*: Instant render speed, full touch target accessibility (>48dp), seamless Material 3 integration, and offline reliability.
- **Decision 2: Room SQLite with Kotlin Flow for Persistent Outpatient Data**
  - *Chosen Approach*: Store patient profiles, clinic schedules, doctor masters, and booked appointments in a local Room database with reactive Kotlin `Flow`.
  - *Why*: Fully offline capable; appointments and registered medical profile survive app restarts immediately with zero latency and high reliability.
- **Decision 3: Realistic Seed Clinics & Doctor Schedules**
  - *Chosen Approach*: Pre-seed realistic Puskesmas & RSUD polyclinics (Poli Umum, Poli Gigi, Poli KIA, Poli Anak, Poli Penyakit Dalam) with doctor names, schedules, and quota counters that decrease when the user books a slot.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────────┐ │
│  │ DashboardView │ │  CalendarView  │ │ RME Profile View  │ │
│  └───────┬───────┘ └───────┬────────┘ └─────────┬─────────┘ │
│          │                 │                    │           │
│  ┌───────┴─────────────────┴────────────────────┴────────┐  │
│  │              EHealthMainViewModel                     │  │
│  │   StateFlow<CalendarUiState>, StateFlow<PatientState> │  │
│  └─────────────────────────┬─────────────────────────────┘  │
└────────────────────────────┼────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                     Repository Layer                        │
│            EHealthRepository (Coroutines / Flow)            │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                     Room Database                           │
│  ┌───────────────────────┐   ┌───────────────────────────┐  │
│  │ PatientProfileEntity  │   │     AppointmentEntity     │  │
│  │ (NIK, Name, RME, etc.)│   │ (Date, Slot, Poli, Queue) │  │
│  └───────────────────────┘   └───────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                  ClinicSlotEntity                     │  │
│  │ (Poli, Doctor, Date, TimeRange, TotalQuota, Booked)   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Core Entities & State

- **`PatientProfileEntity`**:
  - `nik`: String (16 digits)
  - `fullName`, `birthPlace`, `birthDate`, `gender`, `bloodType`
  - `phone`, `address`
  - `chronicDiseases`: List<String> (Diabetes, Hipertensi, Asma, Jantung)
  - `hasDrugAllergy`: Boolean, `drugAllergyDetail`: String
  - `paymentMethod`: String ("Umum", "BPJS", "Asuransi"), `cardNumber`: String
- **`AppointmentEntity`**:
  - `id`: Long (Primary Key)
  - `queueNumber`: String (e.g. "UM-018", "GG-005")
  - `faskesName`: String ("Puskesmas Kecamatan Sehat", "RSUD Kota Madani")
  - `poliType`: String ("Poli Umum", "Poli Gigi & Mulut", "Poli KIA", etc.)
  - `doctorName`: String
  - `appointmentDate`: Long (epoch day / millis)
  - `timeSlot`: String ("08:00 - 10:00", etc.)
  - `paymentMethod`: String, `cardNumber`: String
  - `status`: String ("CONFIRMED", "COMPLETED", "CANCELLED")
  - `bookingCode`: String (SATU-RME-XXXXX)
- **`CalendarUiState`**:
  - `selectedDate`: LocalDate
  - `displayedMonth`: YearMonth
  - `availableClinics`: List<ClinicSlot>
  - `selectedSlot`: ClinicSlot?
  - `userAppointments`: List<AppointmentEntity>
  - `bookingResult`: AppointmentEntity?
