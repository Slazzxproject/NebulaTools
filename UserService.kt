package id.nebula.tools

import android.os.Process
import kotlin.system.exitProcess

class UserService : IUserService.Stub() {
    override fun hancurkanProses() {
        // Fungsi untuk mematikan service ini sendiri jika sudah tidak dipakai
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun jalankanPerintahSistem(cmd: String): String {
        // Di sinilah keajaiban Shizuku bekerja, mengeksekusi perintah ADB internal
        return try {
            val process = Runtime.getRuntime().exec(cmd)
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            process.waitFor()
            output.ifEmpty { "Perintah berhasil dijalankan tanpa output." }
        } catch (e: Exception) {
            "Gagal mengeksekusi perintah: ${e.localizedMessage}"
        }
    }
}
