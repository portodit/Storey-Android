package com.example.storey.ui.auth.loginregister.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storey.data.repository.AuthRepository

class Untuk submisi intermediet itu bikin aplikasi yang struktur projectnya ada folder data isi local (ada remotekeydao, storydao, storydatabase), lalu ada model (loginresponse, registerresponse,remotekeys,storiesresponse,uploadresponse), lalu remote (apiconfig, apiservice, storyremotemediator), repository (authrepository, mainrepository)

dan folder ui isinya adapter (loadingstateadapter, storyadapter), addstory(addstoryactivity, addstoryviewmodel), customview (customedittextemail,edittextpassword), detail, factory, main, map, widget

lalu ada folder utils isinya result dan settingprreference

ini sebenarnya dibuatkan oleh teman saya dan teman saya menjelaskan seperti ini:
Alur Pengerjaan Submission intermediet android (1):
1. Buat Model untuk menerima response dari API
2. Buat interface dan konfigurasi untuk koneksi ke API
3. Buat repository untuk berinteraksi dengan function yang berfungsi untuk terhubung dengan API yang sudah dibuat sebelumnya.
4. Buat SettingsPreferences untuk menyimpan token yang didapatkan dari API nanti.
5. Buat views & activity yang diperlukan beserta viewmodel & factorynya.
6. Buat localization sesuai dengan bahasa yang akan dibuat.
7. Buat Stack Widget dan inisialisasi isinya dengan cara memanggil fungsi getStories dari repository



nah cuma saya masih belum mengerti maksud dari file file dan folder yang ada di direktori project android studio saya itu meskipun sudah dijelaskan oleh teman saya tapi saya masih bingung. Bisakha anda buatkan penjelasan ulang yang lebih menjelaskan, kaya model itu buat apa, repository buat apa, terus apakah setiap activity harus dibuat view model


RegisterViewModel(private val repository: AuthRepository) :
    ViewModel() {

    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>()

    fun register(name: String, email: String, password: String) =
        repository.register(name, email, password)
}