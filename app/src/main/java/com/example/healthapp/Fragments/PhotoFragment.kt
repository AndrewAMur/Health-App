package com.example.healthapp.Fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.healthapp.R
import kotlinx.android.synthetic.main.fragment_photo.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PhotoFragment : Fragment() {

    var pictureTaken = false
    var details = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }


    // This event fires 2nd, before views are created for the fragment
    // The onCreate method is called when the Fragment instance is being created, or re-created.
    // Use onCreate for any standard setup that does not require the activity to be fully created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    fun initialize_layout(){
        btn_photo.setOnClickListener {
            dispatchTakePictureIntent()
        }
        btn_analyze.setOnClickListener {
            txt_diagnosis.visibility = View.VISIBLE
            layout_linear.setBackgroundColor(Color.WHITE)
            if (pictureTaken){
                val handler = Handler()
                handler.postDelayed(
                    Runnable { txt_diagnosis.setText("Condition: .") },
                    0
                )
                handler.postDelayed(
                    Runnable { txt_diagnosis.setText("Condition: ..") },
                    1000
                )
                handler.postDelayed(
                    Runnable { txt_diagnosis.setText("Condition: ...") },
                    3000
                )
                handler.postDelayed(
                    Runnable { txt_diagnosis.setText("Condition: Scarlet Fever ⓘ") },
                    6000
                )
                layout_linear.setOnClickListener {
                    setViewLayout(R.layout.fragment_scarlet);
                    details = true;
                }
            }
            else{
                txt_diagnosis.setText("No picture to analyze")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (details){
                    setViewLayout(R.layout.fragment_photo)
                    initialize_layout()
                }
            }
        })

        initialize_layout()

    }

    private fun setViewLayout(id: Int) {
        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var mainView = inflater.inflate(id, null)
        val rootView = view as ViewGroup?
        rootView!!.removeAllViews()
        rootView.addView(mainView)
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            context?.let {
                takePictureIntent.resolveActivity(it.packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Toast.makeText(context, "Failed saving the picture", Toast.LENGTH_SHORT).show()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.android.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic()
            pictureTaken = true
            btn_analyze.visibility = View.VISIBLE
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }


    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

}