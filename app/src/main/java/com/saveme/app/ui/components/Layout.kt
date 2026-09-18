package com.saveme.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ukuran layar diambil dari jendela aplikasi, bukan dari layar perangkat, agar
 * tetap benar saat aplikasi berjalan di jendela terbagi atau di layar lipat.
 */
@Composable
@ReadOnlyComposable
fun windowWidth(): Dp {
    val width = LocalWindowInfo.current.containerSize.width
    return with(LocalDensity.current) { width.toDp() }
}

/**
 * Jarak isi ke tepi layar. Ponsel kecil diberi napas seperlunya, layar lebar
 * diberi tepi yang lebih longgar supaya barisnya tidak melebar sampai ujung.
 */
@Composable
@ReadOnlyComposable
fun screenGutter(): Dp {
    val width = windowWidth()
    return when {
        width < 360.dp -> 12.dp
        width < 600.dp -> 16.dp
        width < 840.dp -> 24.dp
        else -> 32.dp
    }
}

/**
 * Jumlah kolom petak kartu. Dihitung dari lebar jendela, bukan dari lebar
 * minimum tiap kartu, supaya ponsel sempit tetap mendapat dua kolom dan layar
 * lebar tidak berubah jadi barisan kartu kurus.
 */
@Composable
@ReadOnlyComposable
fun cardGridCells(): GridCells {
    val width = windowWidth()
    return GridCells.Fixed(
        when {
            width < 600.dp -> 2
            width < 900.dp -> 3
            width < 1240.dp -> 4
            else -> 5
        },
    )
}

/** Jarak antar kartu di petak, ikut melebar bersama layar. */
@Composable
@ReadOnlyComposable
fun cardGridSpacing(): Dp = if (windowWidth() < 360.dp) 12.dp else 16.dp

/** Padding isi petak kartu, sudah termasuk tepi layar yang menyesuaikan. */
@Composable
@ReadOnlyComposable
fun gridContentPadding(top: Dp = 8.dp, bottom: Dp = 32.dp): PaddingValues {
    val gutter = screenGutter()
    return PaddingValues(start = gutter, end = gutter, top = top, bottom = bottom)
}

/**
 * Isi satu kolom — pengaturan, detail link, mode fokus — dibatasi lebarnya lalu
 * ditaruh di tengah oleh pemanggilnya. Tanpa ini, satu baris teks di tablet
 * jadi terlalu panjang untuk diikuti mata.
 */
fun Modifier.readableColumn(max: Dp = 620.dp): Modifier = this.widthIn(max = max)

/** Tinggi jendela aplikasi, dipakai membatasi daftar panjang di dalam dialog. */
@Composable
@ReadOnlyComposable
fun windowHeight(): Dp {
    val height = LocalWindowInfo.current.containerSize.height
    return with(LocalDensity.current) { height.toDp() }
}

/**
 * Tinggi maksimum daftar yang bisa digulir di dalam dialog.
 *
 * Diikat ke tinggi jendela, bukan angka tetap: di ponsel pendek daftar tidak
 * lagi mendorong tombol keluar layar, dan di layar tinggi ruangnya tidak
 * tersisa kosong.
 */
@Composable
@ReadOnlyComposable
fun dialogListMaxHeight(): Dp = (windowHeight() * 0.44f).coerceIn(160.dp, 460.dp)
