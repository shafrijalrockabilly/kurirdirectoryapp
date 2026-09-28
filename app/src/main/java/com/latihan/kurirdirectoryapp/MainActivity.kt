package com.latihan.kurirdirectoryapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity utama yang mengorkestrasi pemuatan data kurir/pengguna dari API,
 * penanganan loading/error/empty state, serta pencarian real-time in-memory.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var editTextSearchName: EditText
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var textViewErrorMessage: TextView
    private lateinit var textViewEmptyState: TextView

    // Layout containers untuk tampilan state modern
    private lateinit var layoutLoading: View
    private lateinit var layoutEmptyState: View
    private lateinit var layoutErrorState: View
    private lateinit var buttonRetry: Button
    private lateinit var textViewCount: TextView

    private lateinit var userAdapter: UserAdapter

    // Variable source of truth berisi seluruh data pengguna dari API
    private var daftarPenggunaAsli: List<UserResponse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inisialisasiTampilan()
        persiapkanRecyclerView()
        pasangPencarianTextWatcher()
        pasangListenerTombolCobaLagi()

        ambilDataPengguna()
    }

    /**
     * Menghubungkan variabel tampilan dengan ID komponen di layout.
     */
    private fun inisialisasiTampilan() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        editTextSearchName = findViewById(R.id.editTextSearchName)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage)
        textViewEmptyState = findViewById(R.id.textViewEmptyState)

        layoutLoading = findViewById(R.id.layoutLoading)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutErrorState = findViewById(R.id.layoutErrorState)
        buttonRetry = findViewById(R.id.buttonRetry)
        textViewCount = findViewById(R.id.textViewCount)
    }

    /**
     * Mengatur RecyclerView dengan LinearLayoutManager dan UserAdapter.
     */
    private fun persiapkanRecyclerView() {
        userAdapter = UserAdapter()
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter
    }

    /**
     * Memasang TextWatcher pada EditText untuk mendeteksi setiap perubahan teks pencarian.
     */
    private fun pasangPencarianTextWatcher() {
        editTextSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNamaPengguna(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Memasang OnClickListener pada tombol "Coba Lagi" saat terjadi kesalahan koneksi.
     */
    private fun pasangListenerTombolCobaLagi() {
        buttonRetry.setOnClickListener {
            ambilDataPengguna()
        }
    }

    /**
     * Mengambil daftar pengguna dari API RESTful secara asinkron di I/O thread.
     */
    private fun ambilDataPengguna() {
        tampilkanLoadingState()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getAllUsers()
                withContext(Dispatchers.Main) {
                    sembunyikanLoadingState()
                    if (response.isSuccessful && response.body() != null) {
                        daftarPenggunaAsli = response.body()!!
                        if (daftarPenggunaAsli.isEmpty()) {
                            tampilkanEmptyState()
                        } else {
                            filterNamaPengguna(editTextSearchName.text.toString())
                        }
                    } else {
                        tampilkanPesanError("Gagal memuat data dari server. Kode HTTP: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sembunyikanLoadingState()
                    tampilkanPesanError("Gagal terhubung ke jaringan. Silakan periksa koneksi internet Anda.")
                }
            }
        }
    }

    /**
     * Memfilter daftar pengguna berdasarkan kata kunci pada nama ATAU perusahaan secara in-memory.
     */
    private fun filterNamaPengguna(kataKunci: String) {
        val kataKunciBersih = kataKunci.trim()
        val hasilFilter = if (kataKunciBersih.isEmpty()) {
            daftarPenggunaAsli
        } else {
            daftarPenggunaAsli.filter { pengguna ->
                pengguna.name.contains(kataKunciBersih, ignoreCase = true) ||
                        pengguna.company.companyName.contains(kataKunciBersih, ignoreCase = true)
            }
        }

        if (hasilFilter.isEmpty() && daftarPenggunaAsli.isNotEmpty()) {
            tampilkanEmptyState()
        } else if (daftarPenggunaAsli.isNotEmpty()) {
            tampilkanDaftar(hasilFilter)
        }
    }

    /**
     * Menampilkan indikator loading dan menyembunyikan tampilan lain saat mengambil data.
     */
    private fun tampilkanLoadingState() {
        layoutLoading.visibility = View.VISIBLE
        progressBarLoading.visibility = View.VISIBLE
        recyclerViewUsers.visibility = View.GONE
        layoutEmptyState.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        layoutErrorState.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
        textViewCount.visibility = View.GONE
    }

    /**
     * Menyembunyikan indikator loading setelah proses fetch selesai.
     */
    private fun sembunyikanLoadingState() {
        layoutLoading.visibility = View.GONE
        progressBarLoading.visibility = View.GONE
    }

    /**
     * Menampilkan daftar pengguna hasil fetch/filter ke dalam RecyclerView.
     */
    private fun tampilkanDaftar(daftar: List<UserResponse>) {
        userAdapter.perbaruiDaftar(daftar)
        recyclerViewUsers.visibility = View.VISIBLE

        textViewCount.text = getString(R.string.format_jumlah_kurir, daftar.size)
        textViewCount.visibility = View.VISIBLE

        layoutEmptyState.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        layoutErrorState.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
    }

    /**
     * Menampilkan empty state ketika hasil pencarian kosong.
     */
    private fun tampilkanEmptyState() {
        recyclerViewUsers.visibility = View.GONE
        textViewCount.visibility = View.GONE

        layoutEmptyState.visibility = View.VISIBLE
        textViewEmptyState.visibility = View.VISIBLE

        layoutErrorState.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
    }

    /**
     * Menampilkan pesan kesalahan jika terjadi keandalan jaringan atau HTTP error.
     */
    private fun tampilkanPesanError(pesan: String) {
        recyclerViewUsers.visibility = View.GONE
        textViewCount.visibility = View.GONE

        layoutEmptyState.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE

        layoutErrorState.visibility = View.VISIBLE
        textViewErrorMessage.text = pesan
        textViewErrorMessage.visibility = View.VISIBLE
    }
}
