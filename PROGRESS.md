# saveME — Catatan Progres

Terakhir diperbarui: **18 September 2026**

Berkas ini merangkum keadaan proyek supaya sesi berikutnya bisa langsung menyambung
tanpa perlu menjelaskan ulang dari awal. Untuk penjelasan fitur dari sisi pemakai,
lihat `README.md`.

---

## 1. Apa yang sedang dibuat

Tiruan aplikasi penyimpan tautan **Tuckii** berdasarkan 9 tangkapan layar di folder
`screenshot/`, dengan tiga permintaan pokok dari pemilik proyek:

- tampilan dan fitur ditiru semirip mungkin,
- **semua pembatas berbayar dihilangkan** (tidak ada Pro, kuota, atau layar langganan),
- **penyimpanan sepenuhnya lokal** — tanpa akun, tanpa server. Aplikasi pribadi,
  tidak akan dipublikasikan.

Nama aplikasi: **saveME**, package `com.saveme.app`.

---

## 2. Status: sudah jalan dan sudah diuji

Aplikasi **selesai dan berfungsi**. Build bersih (0 error, 0 warning),
43 berkas Kotlin, ~7.500 baris, APK debug 15MB, terpasang dan diuji di emulator
Pixel 10 (API 37) tanpa satu pun crash.

Layar yang sudah ada: Home, isi koleksi, detail link, pencarian, Settings,
Focus Mode, dan layar tujuan berbagi.

Fitur yang sudah diuji langsung di emulator:

| Yang diuji | Hasil |
|---|---|
| Simpan lewat tempel + tombol `+` | Jalan |
| Berbagi dari aplikasi lain (`ACTION_SEND`) | Jalan, kembali ke aplikasi asal |
| Pintasan koleksi di lembar berbagi (Direct Share) | Jalan, simpan langsung tanpa dialog |
| Pratinjau OpenGraph (situs biasa, YouTube, TikTok) | Jalan |
| Pratinjau Instagram | Jalan lewat User-Agent perayap |
| Ganti pratinjau dari galeri + kembalikan ke bawaan | Jalan |
| Migrasi database v1 → v2 | Jalan, data lama utuh |
| Tema terang / gelap / ikut sistem | Jalan |
| Animasi geser antar halaman (maju dan mundur) | Jalan |
| Kembali lewat tombol/usapan sistem | Jalan, geser sama dengan tombol di aplikasi |
| Modal New collection di layar pendek dan mendatar | Jalan, isinya menggulir sampai tombol Create |
| Petak koleksi di layar lebar dan mendatar | Jalan, 2–5 kolom mengikuti lebar jendela |

---

## 3. Perintah yang sering dipakai

```bash
# Build
./gradlew :app:assembleDebug

# Pasang ke emulator/HP yang tersambung
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Jalankan
adb shell am start -n com.saveme.app/.MainActivity

# Uji alur berbagi tanpa membuka aplikasi lain
adb shell 'am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "https://contoh.com/artikel" com.saveme.app'

# Uji pintasan koleksi (simpan langsung ke koleksi id 1)
adb shell 'am start -a android.intent.action.SEND -t text/plain \
  --es android.intent.extra.TEXT "https://contoh.com" \
  --es "android.intent.extra.shortcut.ID" "collection-1" \
  -n com.saveme.app/.ShareTargetActivity'

# Lihat pintasan berbagi yang terdaftar
adb shell dumpsys shortcut | sed -n '/Package: com.saveme.app/,/^  Package: /p'

# Mulai dari data bersih
adb shell pm clear com.saveme.app
```

**Catatan Git Bash di Windows:** path Android seperti `/sdcard/...` akan diubah
jadi path Windows. Awali perintahnya dengan `MSYS_NO_PATHCONV=1` bila perlu.

---

## 4. Keputusan teknis dan jebakan yang sudah dipecahkan

Bagian ini yang paling menghemat waktu — semuanya sempat memakan waktu untuk
ditemukan.

### Konfigurasi build (jangan diutak-atik tanpa alasan)

`gradle.properties` **wajib** memuat:

```properties
android.builtInKotlin=false
android.newDsl=false
```

AGP 9 membawa dukungan Kotlin bawaan, tetapi **KSP (yang dibutuhkan Room) belum
kompatibel dengannya**. Kedua setelan itu mengembalikan proyek ke plugin Kotlin
eksplisit dan DSL AGP klasik (`compileSdk = 37`, bukan blok `compileSdk { }`).
Hapus keduanya hanya jika Room sudah tidak lagi memakai KSP.

### Versi yang cocok

| | Versi | Catatan |
|---|---|---|
| AGP | 9.3.3 | bawaan proyek |
| Kotlin | 2.3.21 | 2.2.x **tidak** kompatibel dengan AGP 9 |
| KSP | 2.3.12 | |
| Compose BOM | 2026.09.00 | |
| Room | 2.8.4 | migrasi memakai `SQLiteConnection`, bukan `SupportSQLiteDatabase` |
| activity-compose | 1.13.0 | 1.14.0 belum rilis stabil |
| Coil | 3.6.2 | perlu `SingletonImageLoader.Factory` di `SaveMeApp` |

`minSdk = 26` (Android 8.0) karena Archivo dipakai sebagai variable font — satu
berkas untuk bobot 400 sampai 900, diminta lewat sumbu `wght`.

Jangan tambahkan `androidx.sharetarget` — pustaka itu membawa service usang tanpa
`android:exported` dan menggagalkan merge manifest. Direct Share sudah didukung
sistem sejak Android 10 tanpa pustaka itu.

### Gaya tampilan: neo-brutalism

Tiga hal yang membentuk tampilannya, dan ketiganya sudah terkumpul di satu tempat
masing-masing — jangan menuliskan angkanya lagi di layar:

| Apa | Di mana | Isi |
|---|---|---|
| Sudut | `NeoRadius` di `components/Neo.kt` | `Chip` 3dp, `Control` 4dp, `Card` 6dp — nyaris siku |
| Garis dan bayangan | `NeoBorder` 3dp, `NeoShadow` 6dp | hitam pekat, bayangan pejal tanpa blur |
| Huruf | `ui/theme/Type.kt` | Archivo, bobot 700–900 untuk judul, jarak huruf dirapatkan |

Tombol utama (`NeoButton`) menulis labelnya kapital semua lewat parameter
`uppercase`, jadi label di pemanggil tetap ditulis biasa.

### Warna dan tema

Warna adalah **properti komposabel** (`val Ink: Color @Composable get() = ...`)
yang membaca `LocalPalette`. Berkat itu seluruh aplikasi ikut berganti tema tanpa
satu pun pemanggil perlu diubah. Konsekuensinya:

- warna **tidak bisa dibaca di dalam lambda `Canvas`/`DrawScope`** — angkat dulu
  ke variabel di badan komposabel (lihat `Illustrations.kt`),
- latar ber-alpha rendah akan ditembus bayangan pejal di belakangnya; pakai
  `softTint()` yang mencampur warna, bukan `.copy(alpha = ...)`,
- teks di atas bidang berwarna memakai `OnAccent` atau `readableOn(background)`
  supaya kontrasnya benar di kedua tema.

### Pratinjau Instagram

Instagram menyembunyikan tag OpenGraph dari User-Agent peramban, **tetapi tetap
menyajikannya ke User-Agent perayap tautan**. Karena itu `LinkMetadataFetcher`
mencoba dua kali: UA peramban dulu, lalu `facebookexternalhit` bila belum dapat
gambar. Cadangan terakhir: `instagram.com/p/<kode>/media/?size=l` yang
mengembalikan berkas gambar langsung.

Halaman `embed/captioned` **sudah tidak bisa dipakai** — kini berdinding login.

### Lain-lain

- Kartu link memakai `minLines = 2` pada judul supaya tinggi kartu bersebelahan sama.
- **Back sistem punya pasangan transisi sendiri.** `enterTransition` dan kawan-kawan
  hanya dipakai untuk navigasi di dalam aplikasi; tombol atau usapan "kembali" milik
  ponsel memakai `predictivePopEnterTransition` / `predictivePopExitTransition`, yang
  bawaannya mengecil sambil memudar. Keduanya wajib diisi di `SaveMeNavHost`, kalau
  tidak dua cara kembali akan terasa berbeda. Sejak targetSdk 36 bendera manifest
  `enableOnBackInvokedCallback` tidak lagi bisa mematikan perilaku ini.
- **Ukuran layar dibaca lewat `components/Layout.kt`**, bukan `LocalConfiguration`:
  `windowWidth()`, `screenGutter()`, `cardGridCells()`, `dialogListMaxHeight()`.
  Semuanya membaca `LocalWindowInfo.containerSize`, jadi tetap benar di jendela
  terbagi dan layar lipat.
- **`NeoDialog` membungkus isinya dengan `fillMaxSize()`,** bukan `fillMaxWidth()`.
  Dengan `fillMaxWidth` kotaknya ikut setinggi isi, sehingga `verticalScroll` di
  dalamnya tidak pernah punya sisa untuk digulir dan bagian bawah dialog — termasuk
  tombol Create — terpotong di layar pendek. Dialog yang daftarnya sudah menggulir
  sendiri memanggilnya dengan `scrollable = false`.
- Layar detail menahan data terakhir (`lastKnown`) selama animasi keluar, kalau
  tidak isinya berkedip kosong saat ViewModel dibersihkan.
- Semua tempo animasi terkumpul di `ui/theme/Motion.kt`.
- **Animasi tekan ditahan, bukan dipercepat saja.** Satu ketukan cuma sekitar 80 md,
  dan kalau ketukan itu memindahkan halaman, geseran halaman mulai di milidetik yang
  sama — tombolnya bergerak tapi tidak pernah sempat terlihat. `rememberHeldPress`
  di `components/Clickable.kt` menahan status tertekan selama `Motion.PressHoldMillis`
  setelah jari terangkat. Turunnya memakai `Motion.PressIn*` (45 md), naiknya
  `Motion.PressOut*` yang memantul sedikit. Pemakai `InteractionSource` mentah tidak
  akan mendapat efek ini, jadi lewati `neoClickable` atau `NeoSurface` hanya bila ada
  alasan kuat.

---

## 5. Peta kode

```
app/src/main/java/com/saveme/app/
├─ MainActivity.kt          Layar penuh; menentukan tema dan ikon bilah sistem
├─ ShareTargetActivity.kt   Tujuan lembar berbagi; tembus pandang, menutup sendiri
├─ SaveMeApp.kt             Wadah komponen berumur panjang + ImageLoader Coil
├─ data/
│  ├─ db/        Entity, DAO, AppDatabase (versi 2, ada migrasi 1→2)
│  ├─ repo/      LibraryRepository — satu-satunya pintu ke penyimpanan
│  ├─ meta/      LinkMetadataFetcher — OpenGraph, oEmbed, gambar galeri
│  ├─ prefs/     SettingsStore (DataStore) + ThemeMode
│  └─ backup/    Ekspor/impor JSON, termasuk pratinjau kustom (Base64)
├─ share/        CollectionShortcuts — pintasan Direct Share per koleksi
├─ ui/
│  ├─ theme/     Palet dua tema, tipografi Archivo, Motion, ikon gambar tangan
│  ├─ components/ Permukaan, tombol, kartu, dialog bergaya neo-brutalist,
│  │              Layout.kt (ukuran layar), Illustrations.kt (lambang mata rantai)
│  ├─ home/ collection/ link/ search/ settings/ focus/   satu folder per layar
│  └─ nav/       Rute dan NavHost
├─ work/         Pengingat harian (WorkManager)
└─ util/         URL, papan klip, berbagi, getaran
```

Data pengguna: `saveme.db` (Room) dan gambar di `files/thumbs/`, keduanya di
direktori privat aplikasi.

---

## 6. Penyimpangan yang disengaja dari screenshot asli

Semuanya sudah disepakati dengan pemilik proyek:

- **Maskot dan ilustrasi** digambar ulang sebagai vektor Canvas — mirip, bukan
  salinan piksel.
- **Logo Instagram** diganti lencana huruf berwarna. Logo asli adalah merek dagang.
- **Font** memakai Nunito (Google Fonts), padanan bebas terdekat dari font rounded
  di screenshot.
- **Tombol play** di pratinjau dihapus atas permintaan — ini penyimpan tautan,
  bukan pemutar video.
- **Paywall dan modal "GO PRO"** dihapus seluruhnya.

---

## 7. Batasan yang diketahui

- Preview Instagram bergantung pada perilaku Instagram terhadap UA perayap. Bila
  suatu saat ditutup juga, jalan keluarnya sudah tersedia: ganti pratinjau manual
  dari galeri.
- Vimeo tidak merespons dari jaringan pengujian; oEmbed-nya sudah terpasang dan
  mestinya jalan di jaringan lain.
- Belum ada pengurutan ulang koleksi dengan seret-lepas.
- Belum ada unit test maupun instrumented test.

---

## 8. Langkah berikutnya

**Yang sedang menunggu keputusan:**

> **Build release.** APK yang ada sekarang adalah build *debug* — jalan normal,
> tapi ditandatangani kunci debug dan tidak dioptimasi. Kalau nanti dibuat build
> release, kuncinya berbeda sehingga **Android menolak memperbaruinya di atas
> versi debug** dan data ikut terhapus saat dipasang ulang.
>
> Karena itu sebaiknya diputuskan sebelum aplikasi dipakai serius:
> - buat keystore dan build release sekarang, atau
> - pakai debug dulu, tapi ekspor cadangan lewat Settings ▸ Export Backup sebelum
>   nanti pindah ke release.

**Ide lain yang belum dikerjakan:**

- Seret-lepas untuk mengurutkan koleksi.
- Widget layar utama untuk simpan cepat.
- Pencarian di dalam satu koleksi.

---

## 9. Riwayat permintaan

Ringkasan apa saja yang sudah diminta dan dikerjakan, supaya tidak terulang.

| # | Permintaan | Hasil |
|---|---|---|
| 1 | Tiru aplikasi dari 9 screenshot, hilangkan berbayar, simpan lokal | Aplikasi lengkap, 7 layar |
| 2 | "Sempurnakan" | Proporsi disamakan dengan screenshot asli; alur berbagi dirombak jadi activity sendiri; urutan link; transisi layar |
| 3 | Muncul di share sheet, preview tampil, preview bisa diedit | Direct Share per koleksi; oEmbed + UA perayap; ganti pratinjau dari galeri |
| 4 | Font tebal, animasi slide, hapus play, preview Instagram, dark mode | Kelimanya selesai |
| 5 | Tombol back di detail, animasi keluar, animasi tekan, tempo lebih gesit | Keempatnya selesai |
| 6 | Tinggi kartu di satu baris disamakan | `minLines = 2` pada judul kartu |
| 7 | Logo jadi mata rantai, back sistem ikut menggeser, animasi tekan jangan kedahuluan, modal responsif, gaya Neo Brutalism | Kelimanya selesai |
