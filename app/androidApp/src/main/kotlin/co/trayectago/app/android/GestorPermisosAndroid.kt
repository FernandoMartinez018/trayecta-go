package co.trayectago.app.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import co.trayectago.app.permisos.GestorPermisos
import co.trayectago.app.permisos.Permiso
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Permisos de Android con Activity Result. Se crea en onCreate: registra el lanzador antes de que la actividad arranque. */
class GestorPermisosAndroid(private val actividad: ComponentActivity) : GestorPermisos {
    private var respuesta: CompletableDeferred<Unit>? = null

    // Un solo diálogo del sistema a la vez: dos toques seguidos en «Permitir» no lanzan dos solicitudes.
    private val enCurso = Mutex()

    private val lanzador = actividad.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        respuesta?.complete(Unit)
    }

    override fun concedido(p: Permiso): Boolean = when (p) {
        // La ubicación aproximada también basta: el usuario puede negar la precisa.
        Permiso.UBICACION -> tiene(Manifest.permission.ACCESS_FINE_LOCATION) || tiene(Manifest.permission.ACCESS_COARSE_LOCATION)
        // Antes de Android 13 las notificaciones no piden permiso.
        Permiso.NOTIFICACIONES -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || tiene(Manifest.permission.POST_NOTIFICATIONS)
        Permiso.MICROFONO -> tiene(Manifest.permission.RECORD_AUDIO)
    }

    override suspend fun solicitar(p: Permiso): Boolean {
        if (concedido(p)) return true
        enCurso.withLock {
            val espera = CompletableDeferred<Unit>().also { respuesta = it }
            lanzador.launch(permisosDe(p))
            espera.await()
        }
        return concedido(p)
    }

    // POST_NOTIFICATIONS es una constante que se copia al compilar; solo se pide desde Android 13 (concedido() devuelve true antes).
    @SuppressLint("InlinedApi")
    private fun permisosDe(p: Permiso): Array<String> = when (p) {
        Permiso.UBICACION -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        Permiso.NOTIFICACIONES -> arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        Permiso.MICROFONO -> arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    private fun tiene(permiso: String) = actividad.checkSelfPermission(permiso) == PackageManager.PERMISSION_GRANTED
}
