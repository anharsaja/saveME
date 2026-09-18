package com.saveme.app.ui.nav

import androidx.activity.BackEventCompat
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saveme.app.ui.collection.CollectionScreen
import com.saveme.app.ui.focus.FocusScreen
import com.saveme.app.ui.home.HomeScreen
import com.saveme.app.ui.link.LinkDetailScreen
import com.saveme.app.ui.search.SearchScreen
import com.saveme.app.ui.settings.SettingsScreen
import com.saveme.app.ui.theme.Motion
import com.saveme.app.ui.theme.Paper
import com.saveme.app.ui.theme.SunnySoft



object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val FOCUS = "focus"
    const val COLLECTION = "collection/{id}"
    const val LINK = "link/{id}"

    fun collection(id: Long) = "collection/$id"
    fun link(id: Long) = "link/$id"
}

@Composable
fun SaveMeNavHost(
    navController: NavHostController = rememberNavController(),
) {
    // Mode fokus memakai latar krem sampai ke tepi layar, jadi warnanya
    // ditentukan di sini, di luar padding bilah sistem.
    val entry by navController.currentBackStackEntryAsState()
    val background = if (entry?.destination?.route == Routes.FOCUS) SunnySoft else Paper

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
            .safeDrawingPadding(),
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // Halaman bergeser penuh selebar layar, seperti membalik halaman
            // komik: yang baru masuk dari kanan, yang lama keluar ke kiri.
            //
            // Keempat transisi berikut selalu dipicu tombol di dalam aplikasi,
            // jadi semuanya memakai Motion.ScreenSlide yang menahan geseran
            // selama Motion.ScreenDelayMillis. Perpindahannya sendiri sudah
            // terjadi saat diketuk; yang ditunggu hanya gerakannya, supaya
            // tombol pemanggilnya sempat terlihat memantul naik lebih dulu.
            //
            // Settings satu-satunya perkecualian arah, lihat isSettings() di
            // bawah berkas ini.
            enterTransition = {
                val fromLeft = targetState.isSettings()
                slideInHorizontally(Motion.ScreenSlide) { full -> if (fromLeft) -full else full }
            },
            exitTransition = {
                val toRight = targetState.isSettings()
                slideOutHorizontally(Motion.ScreenSlide) { full -> if (toRight) full else -full }
            },
            popEnterTransition = {
                val fromRight = initialState.isSettings()
                slideInHorizontally(Motion.ScreenSlide) { full -> if (fromRight) full else -full }
            },
            popExitTransition = {
                val toLeft = initialState.isSettings()
                slideOutHorizontally(Motion.ScreenSlide) { full -> if (toLeft) -full else full }
            },
            // Tombol atau usapan "kembali" milik sistem tidak memakai transisi
            // pop di atas: Navigation punya pasangan sendiri untuk itu, dan
            // bawaannya adalah mengecil sambil memudar. Tanpa dua baris berikut,
            // kembali lewat tombol ponsel terasa berbeda dari tombol di aplikasi.
            //
            // Gerakannya mengikuti tepi yang diusap, jadi halaman selalu
            // menyingkir searah dengan jari — dan tanpa jeda, karena di sini
            // tidak ada tombol yang sedang memantul untuk mengisi jeda itu.
            // Usapan sistem pun menuntun geserannya sendiri; menahannya di
            // awal hanya akan membuat halaman tertinggal dari jari.
            predictivePopEnterTransition = { edge ->
                slideInHorizontally(Motion.ScreenSlideGesture) { full ->
                    if (edge == BackEventCompat.EDGE_RIGHT) full else -full
                }
            },
            predictivePopExitTransition = { edge ->
                slideOutHorizontally(Motion.ScreenSlideGesture) { full ->
                    if (edge == BackEventCompat.EDGE_RIGHT) -full else full
                }
            },
        ) {

            composable(Routes.HOME) {
                HomeScreen(
                    onOpenCollection = { navController.navigate(Routes.collection(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }

            composable(
                route = Routes.COLLECTION,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                CollectionScreen(
                    collectionId = id,
                    onBack = { navController.popBackStack() },
                    onOpenLink = { navController.navigate(Routes.link(it)) },
                    onOpenCollection = { navController.navigate(Routes.collection(it)) },
                )
            }

            composable(
                route = Routes.LINK,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                LinkDetailScreen(
                    linkId = id,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCollection = { navController.navigate(Routes.collection(it)) },
                    onOpenLink = { navController.navigate(Routes.link(it)) },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFocus = { navController.navigate(Routes.FOCUS) },
                )
            }

            composable(Routes.FOCUS) {
                FocusScreen(
                    onBack = { navController.popBackStack() },
                    onOpenLink = { navController.navigate(Routes.link(it)) },
                )
            }
        }
    }
}

/**
 * Halaman baru umumnya datang dari kanan, seperti membalik ke halaman
 * berikutnya. Settings satu-satunya yang datang dari kiri: tombolnya duduk di
 * ujung kiri bilah atas Home, jadi halamannya terasa keluar dari tombol yang
 * baru diketuk, bukan menyeberang dari sisi yang berlawanan. Sewaktu ditutup
 * ia kembali ke kiri, ke tempat tombolnya semula.
 *
 * Kembali lewat usapan sistem tetap mengikuti tepi yang diusap, bukan aturan
 * ini — gerakan itu milik jari, bukan milik halamannya.
 */
private fun NavBackStackEntry.isSettings() = destination.route == Routes.SETTINGS
