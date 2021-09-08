package com.boostasoft.contactreader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.boostasoft.contactreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val PICK_CONTACT_REQUEST_CODE = 6500
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            debug("Intent: ${result.data}")
            when(result.resultCode){
                Activity.RESULT_OK -> {
                    val intent = result.data

                    if(intent != null){
                        val uri = intent.data
                        val number = getNumberFromUri(uri!!)
                        debug("Number: ${number}")
                        updateNumber(number)
                    }

                }
            }
        }

        binding!!.searchBox.searchBoxBtn.setOnClickListener{
            openContactPikerActivity()
        }

        when(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)){
            PackageManager.PERMISSION_DENIED -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),
                PICK_CONTACT_REQUEST_CODE)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PICK_CONTACT_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED)
                finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun getNumberFromUri(uri: Uri): String?{
        var number: String? =  null
        val contactId = getContactIdFromUri(uri)
        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +'='+ contactId,
        null, null)
        when{
            cursor!!.moveToFirst() -> {
                val numberColIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                number = cursor.getString(numberColIndex)
                debug("Number: ${number}")
            }

        }
        cursor?.close()
        return number

    }
    private fun getContactIdFromUri(uri: Uri): Long{
        var contactId: Long? = null
        val cursor = contentResolver.query(uri, arrayOf(ContactsContract.Contacts._ID), null, null, null)
        when{
            cursor!!.moveToFirst() -> {
                val idColIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                contactId = cursor.getLong(idColIndex)
                debug("Contact Id: ${contactId}")
            }

        }
        cursor?.close()
        return contactId!!
    }
    fun openContactPikerActivity(){
        resetNumber()
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        if(intent.resolveActivity(packageManager) != null)
            resultLauncher.launch(intent)
        else Toast.makeText(this, "Aucune activité ne prend en charge cette action i", Toast.LENGTH_LONG)
            .show()

    }

    private fun resetNumber(){
        binding!!.searchBox.searchBoxEntry.text = "".toEditable()
    }

    private fun updateNumber(number: String?){
        if(number != null)
            binding!!.searchBox.searchBoxEntry.text = number.toEditable()
        else Toast.makeText(this, "Aucun numéro trouvé !", Toast.LENGTH_LONG)
            .show()
    }
}

fun AppCompatActivity.debug(str: String){
    Log.d(this::class.java.toString(), str)
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)