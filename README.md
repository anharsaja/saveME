# saveME

Aplikasi penyimpan tautan untuk Android. Dibuat untuk dipakai sendiri: seluruh isinya
tersimpan di perangkat, tanpa akun, tanpa server, dan tanpa fitur berbayar.

## Yang bisa dilakukan

- **Simpan cepat** — tempel tautan di layar utama lalu pilih koleksi tujuan.
- **Bagikan dari aplikasi lain** — tekan *Share* di Instagram, TikTok, peramban, atau
  aplikasi apa pun, lalu pilih saveME. Pemilih koleksi muncul melayang di atas
  aplikasi tersebut, dan setelah menyimpan Anda langsung kembali ke sana.
  Sejak Android 10, tiap koleksi juga tampil sebagai tujuan langsung di lembar
  berbagi ("saveME · coding"), jadi satu ketukan sudah cukup.
- **Pratinjau otomatis** — judul, deskripsi, nama situs, dan gambar diambil dari tag
  OpenGraph halaman aslinya. Gambarnya diunduh ke penyimpanan internal supaya kartu
  tetap tampil saat offline. Untuk unggahan media sosial, baris seperti
  `penulis · tanggal · 219 likes · 5 comments` diurai jadi baris meta tersendiri.
  Bila situsnya menutup tag itu di balik dinding login — seperti Instagram — saveME
  mencoba lagi sebagai perayap tautan, lalu lewat oEmbed publik YouTube, TikTok, dan
  Vimeo, dan terakhir lewat alamat media langsung Instagram.
- **Pratinjau bisa diganti sendiri** — di layar detail, ketuk keping *Preview* lalu
  pilih gambar dari galeri. Gambarnya dikecilkan ke sisi terpanjang 1080 px dan
  arahnya diluruskan otomatis. Pratinjau bawaan tidak hilang: *Use the original
  preview* mengembalikannya kapan saja. Selama tidak diganti, yang tampil selalu
  pratinjau bawaan dari kontennya.
- **Koleksi tanpa batas**, lengkap dengan subkoleksi, 10 pilihan warna, dan 16 ikon.
- **Tag, pin, catatan** pada tiap tautan, plus sunting judul dan deskripsi secara manual.
- **Pencarian** menyeluruh ke judul, URL, deskripsi, catatan, dan nama koleksi; bisa juga
  disaring lewat tag.
- **Pilih banyak** di dalam koleksi untuk memindahkan atau menghapus sekaligus.
- **Urutkan** isi koleksi: terbaru, terlama, atau judul A-Z. Tautan yang dipin selalu di atas.
- **Focus Mode** — satu tautan sekali tampil untuk membereskan tumpukan bacaan.
- **Pengingat harian** untuk tautan yang belum dibaca, jamnya bisa diatur.
- **Tampilan neo-brutalism** — garis hitam tebal, bayangan pejal tanpa blur, sudut
  nyaris siku, warna blok yang pekat, dan huruf Archivo yang berat. Lambangnya dua
  mata rantai saling mengait, dipakai mulai dari ikon peluncur sampai layar kosong.
- **Tema terang atau gelap**, atau ikut pengaturan ponsel. Diatur di Settings ▸ Appearance.
- **Menyesuaikan layar** — petak koleksi berisi dua sampai lima kolom mengikuti lebar
  jendela, dan setiap dialog tetap utuh di layar pendek maupun saat papan ketik muncul.
- **Cadangkan & pulihkan** ke satu berkas JSON lewat pemilih berkas bawaan Android.
  Pratinjau bawaan diunduh ulang dari alamatnya saat dipulihkan, sedangkan gambar
  pilihan Anda sendiri ikut dibawa di dalam berkas cadangan supaya tidak hilang.

Tidak ada versi Pro, kuota harian, maupun layar berlangganan. Semua fitur terbuka.

## Menjalankan

Buka folder ini di Android Studio lalu tekan *Run*, atau lewat terminal:

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Berkas APK yang dihasilkan ada di `app/build/outputs/apk/debug/app-debug.apk`.

## Kebutuhan

- Android 8.0 (API 26) ke atas — dibutuhkan oleh variable font Archivo yang dipakai.
- JDK 17 ke atas, Android SDK 37.
- Izin `INTERNET` hanya dipakai untuk mengambil pratinjau tautan, dan
  `POST_NOTIFICATIONS` hanya untuk pengingat harian. Keduanya bisa diabaikan:
  aplikasi tetap berjalan tanpa itu, hanya tanpa pratinjau dan pengingat.

## Susunan kode

```
app/src/main/java/com/saveme/app/
├─ MainActivity.kt          Layar penuh aplikasi
├─ ShareTargetActivity.kt   Tujuan lembar berbagi; tembus pandang, menutup sendiri
├─ data/
│  ├─ db/        Entity, DAO, dan database Room
│  ├─ repo/      LibraryRepository — satu-satunya pintu ke penyimpanan
│  ├─ meta/      Pengambil tag OpenGraph dan pengunduh thumbnail
│  ├─ prefs/     Preferensi lewat DataStore
│  └─ backup/    Ekspor dan impor JSON
├─ ui/
│  ├─ theme/     Warna, tipografi, dan ikon yang digambar tangan
│  ├─ components/ Permukaan, tombol, kartu, dialog bergaya neo-brutalist
│  ├─ home/ collection/ link/ search/ settings/ focus/   satu folder per layar
│  └─ nav/       Daftar rute dan NavHost
├─ work/         Pekerja pengingat harian (WorkManager)
└─ util/         URL, papan klip, berbagi, getaran
```

Data tersimpan di `saveme.db` (Room) dan thumbnail di `files/thumbs/`, keduanya di
direktori privat aplikasi. Mencopot aplikasi akan menghapus semuanya, jadi pakai
*Export Backup* di Settings sebelum memasang ulang.

## Catatan konfigurasi build

`gradle.properties` memuat dua setelan yang perlu tetap ada:

```properties
android.builtInKotlin=false
android.newDsl=false
```

AGP 9 membawa dukungan Kotlin bawaan, tetapi KSP (yang dibutuhkan Room) belum
kompatibel dengannya. Kedua setelan itu mengembalikan proyek ke plugin Kotlin eksplisit
dan DSL AGP klasik. Hapus keduanya hanya jika Room sudah tidak lagi memakai KSP.
