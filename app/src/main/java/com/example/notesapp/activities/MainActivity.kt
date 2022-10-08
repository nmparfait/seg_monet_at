package com.example.notesapp.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.notesapp.R
import com.example.notesapp.adapter.NotesAdapter
import com.example.notesapp.billing.BillingClass
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.model.Notes
import com.example.notesapp.utils.openActivity
import com.example.notesapp.utils.showBanner
import com.example.notesapp.utils.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), NotesAdapter.ItemClickListener {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var firebaseAuth: FirebaseAuth? = null
    private var storageReference: StorageReference? = null
    private var builder: AlertDialog.Builder? = null
    private var premiumBuilder: AlertDialog.Builder? = null
    private var mDatabase: DatabaseReference? = null
    private var progressDialog: ProgressDialog? = null

    private var notesAdapter: NotesAdapter? = null
    private var notesList: ArrayList<Notes>? = null
    private lateinit var loja : BillingClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        initRecycle()
        toolbarClicks()
        drawerClicks()
        loadList()

    }

    private fun initRecycle() {
        notesList = arrayListOf()
        with(binding.rNotes) {
            hasFixedSize()
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

    }

    private fun loadList() {
        progressDialog?.show()
        val userId = firebaseAuth?.uid ?: return
        mDatabase?.child(userId)?.get()?.addOnSuccessListener { task ->
            progressDialog?.dismiss()
            for (item in task.child("NotesList").children) {
                val title = item.child("notesTitle").value.toString()
                val detail = item.child("notesDetail").value.toString()
                val date = item.child("notesDate").value.toString()
                val path = item.child("notesImagePath").value.toString()
                notesList?.add(Notes(title, detail, date, path))
            }
            progressDialog?.dismiss()
            notesList?.let { list ->
                notesAdapter = NotesAdapter(list, this)
                binding.rNotes.adapter = notesAdapter
            }
        }?.addOnFailureListener {
            progressDialog?.dismiss()
            showToast(it.message.toString())
        }
    }

    private fun init() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait")
        progressDialog?.setMessage("Loading...")

        builder = AlertDialog.Builder(this)
        premiumBuilder = AlertDialog.Builder(this)
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        mDatabase = Firebase.database.getReference("Notes")
        val userId = firebaseAuth?.uid ?: return
        getUserName(userId)
    }

    private fun drawerClicks() {
        binding.inHeader.tvHome.setOnClickListener {
            binding.drawer.closeDrawers()
        }

        binding.inHeader.tvExit.setOnClickListener {
            binding.drawer.closeDrawers()
            exitDialog()
        }

        binding.fab.setOnClickListener {
            openActivity(AddNotesActivity::class.java)
        }
    }

    private fun toolbarClicks() {
        binding.toolbar.menuBtn.setOnClickListener { binding.drawer.openDrawer(GravityCompat.START) }

        binding.toolbar.ivMore.setOnClickListener { showPopUpMenu() }

        storageReference?.child("profilePics/" + firebaseAuth?.currentUser?.uid)?.downloadUrl?.addOnCompleteListener { task: Task<Uri?> ->
            if (task.isSuccessful) {
                Log.d("task.result", "toolbarClicks:${task.result} ")
                Glide.with(this@MainActivity).load(task.result)
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .into(binding.inHeader.headerImg)
            }
        }

        binding.toolbar.ivSearch.setOnClickListener {
            premiumDialog {
                val userId = firebaseAuth?.uid ?: return@premiumDialog
                mDatabase?.child(userId)?.child("premiumAccess")?.setValue(true)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.toolbar.ivSearch.visibility = View.GONE
                            binding.bannerAd.visibility = View.GONE
                        }

                    }?.addOnFailureListener {
                        showToast(it.message.toString())
                    }
            }
        }
    }

    private fun showPopUpMenu() {
        val popupMenu = PopupMenu(this@MainActivity, binding.toolbar.ivMore)
        popupMenu.menuInflater.inflate(R.menu.more_items, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.itemId == R.id.logOut) {
                firebaseAuth!!.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
            true
        }
        popupMenu.show()
    }

    private fun exitDialog() {
        builder?.setMessage(R.string.dialog_message)
            ?.setCancelable(false)
            ?.setPositiveButton(
                "Exit"
            ) { dialog: DialogInterface, id: Int ->
                dialog.dismiss()
                finishAffinity()
            }
            ?.setNegativeButton(
                "Cancel"
            ) { dialog: DialogInterface, id: Int -> dialog.dismiss() }
        val alert = builder?.create()
        alert?.setTitle(R.string.dialog_title)
        alert?.show()
    }

    private fun premiumDialog(onAction: (() -> Unit)? = null) {
        premiumBuilder?.setMessage(R.string.premium_message)
            ?.setCancelable(false)
            ?.setPositiveButton(
                "Yes"
            ) { dialog: DialogInterface, id: Int ->
                onAction?.invoke()
                dialog.dismiss()
            }
            ?.setNegativeButton(
                "No"
            ) { dialog: DialogInterface, id: Int -> dialog.dismiss() }
        val alert = premiumBuilder?.create()
        alert?.setTitle(R.string.premium_title)
        alert?.show()
    }

    private fun getUserName(userId: String) {
        mDatabase?.child(userId)?.get()?.addOnSuccessListener { task ->
            progressDialog?.dismiss()
            Log.d("getUserName", "getUserName: ${task.child("name").value}")
            val name = task.child("name").value.toString()
            val email = task.child("email").value.toString()
            val location = task.child("currentLocation").value.toString()
            val checkPremium: Boolean? = task.child("premiumAccess").value as Boolean?
            checkPremium?.let {
                if (checkPremium) {
                    binding.toolbar.ivSearch.visibility = View.GONE
                    binding.bannerAd.visibility = View.GONE
                } else {
                    binding.toolbar.ivSearch.visibility = View.VISIBLE
                    binding.bannerAd.visibility = View.VISIBLE
                    showBanner(binding.bannerAd)
                }
            }
            binding.inHeader.txtAppName.text = name
            binding.inHeader.tvEmail.text = email
            binding.inHeader.tvLocation.text = location
            Toast.makeText(this, "Welcome $name", Toast.LENGTH_SHORT).show()

        }?.addOnFailureListener {
            showToast(it.message.toString())
        }

    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerVisible(GravityCompat.START)) {
            binding.drawer.closeDrawers()
        } else {
            //exitDialog()
            super.onBackPressed()
        }
    }

    override fun itemClick(notes: Notes) {
        val intent = Intent(this, NotesDetailActivity::class.java).apply {
            putExtra("TITLE", notes.notesTitle)
            putExtra("DETAIL", notes.notesDetail)
            putExtra("IMG", notes.notesImagePath)
        }
        startActivity(intent)
    }

    override fun saveItem(notes: Notes) {
        val text = "${notes.notesTitle} \n\n ${notes.notesDetail}"
        save(notes.notesTitle, text)
    }

    private fun save(fileName: String, text: String) {
        val name = "TXT$fileName${System.currentTimeMillis()}.txt"
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(name, MODE_PRIVATE)
            fos.write(text.toByteArray())
            Toast.makeText(this, "Saved to $filesDir/$name", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}