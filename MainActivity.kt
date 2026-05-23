package id.nebula.tools

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private var userService: IUserService? = null
    private val SHIZUKU_CODE = 1001

    // Listener saat Shizuku terhubung
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAndBindService()
    }

    // Listener saat Shizuku terputus
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        userService = null
        Toast.makeText(this, "Shizuku Terputus!", Toast.LENGTH_SHORT).show()
    }

    // Koneksi service antara aplikasi dan Shizuku
    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            userService = IUserService.Stub.asInterface(service)
            Toast.makeText(this@MainActivity, "Nebula Tools Terhubung ke Shizuku 100%!", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kita akan set UI Gaming-nya di langkah berikutnya
        setContentView(R.layout.activity_main)

        // Daftarkan listener Shizuku
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)

        if (Shizuku.pingBinder()) {
            checkAndBindService()
        } else {
            Toast.makeText(this, "Silakan aktifkan Shizuku terlebih dahulu!", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndBindService() {
        if (Shizuku.checkPermission() == PackageManager.PERMISSION_GRANTED) {
            bindNebulaService()
        } else {
            Shizuku.requestPermission(SHIZUKU_CODE)
        }
    }

    private fun bindNebulaService() {
        if (userService != null) return
        val args = Shizuku.UserServiceArgs(ComponentName(packageName, UserService::class.java.name))
            .version(1)
            .processNameSuffix("service")
        
        try {
            Shizuku.bindUserService(args, userServiceConnection)
        } catch (e: Exception) {
            Log.e("NebulaTools", "Gagal bind service: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
    }
}
