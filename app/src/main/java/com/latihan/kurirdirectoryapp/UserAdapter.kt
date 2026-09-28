package com.latihan.kurirdirectoryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView untuk menampilkan daftar data kurir/pengguna pada item_kurir.xml.
 */
class UserAdapter(
    private var daftarPengguna: MutableList<UserResponse> = mutableListOf()
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    /**
     * ViewHolder untuk menyimpan referensi komponen tampilan item kurir.
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAvatar: TextView = itemView.findViewById(R.id.textViewAvatar)
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewPerusahaan: TextView = itemView.findViewById(R.id.textViewPerusahaan)
        val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        val textViewTelepon: TextView = itemView.findViewById(R.id.textViewTelepon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kurir, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val pengguna = daftarPengguna[position]

        // Set huruf pertama nama pengguna sebagai inisial badge avatar
        val inisial = pengguna.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"
        holder.textViewAvatar.text = inisial

        holder.textViewNama.text = pengguna.name
        holder.textViewPerusahaan.text = pengguna.company.companyName
        holder.textViewEmail.text = pengguna.email
        holder.textViewTelepon.text = pengguna.phone
    }

    override fun getItemCount(): Int = daftarPengguna.size

    /**
     * Memperbarui daftar data pengguna pada adapter dan memicu pembaruan UI.
     */
    fun perbaruiDaftar(daftarBaru: List<UserResponse>) {
        daftarPengguna.clear()
        daftarPengguna.addAll(daftarBaru)
        notifyDataSetChanged()
    }
}
