package com.michaelmuratov.healthapp.Fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.michaelmuratov.healthapp.R
import com.michaelmuratov.healthapp.ml.Model
import kotlinx.android.synthetic.main.fragment_photo.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class PhotoFragment : Fragment() {

    var pictureTaken = false
    var details = false

    var prediction = ""
    lateinit var  hashMap: HashMap<String, Int>

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


        }
        txt_diagnosis.setOnClickListener {
            val link = hashMap[prediction]?.let { it1 -> getString(it1) }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(
                true
            ) {
                override fun handleOnBackPressed() {
                    if (details) {
                        setViewLayout(R.layout.fragment_photo)
                        initialize_layout()
                    }
                }
            })

        hashMap = HashMap()
        hashMap["Chickenpox"] = R.string.Chickenpox
        hashMap["Hf&m"] = R.string.HfandM
        hashMap["Measles"] = R.string.Measles
        hashMap["Mumps"] = R.string.Mumps
        hashMap["Roseola"] = R.string.Roseola
        hashMap["Rubella"] = R.string.Rubella
        hashMap["Scarlet Fever"] = R.string.ScarletFever
        hashMap["Scd"] = R.string.Scd
        hashMap["Skin"] = R.string.Skin
        txt_diagnosis.movementMethod = LinkMovementMethod.getInstance();
        Linkify.addLinks(txt_diagnosis, Linkify.WEB_URLS);
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
                            "com.michaelmuratov.android.fileprovider",
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
            txt_diagnosis.visibility = View.VISIBLE
            //layout_linear.setBackgroundColor(Color.WHITE)
            if (pictureTaken){
                val hashMap = HashMap<String, String>()
                hashMap["Chickenpox"] = "Chickenpox ⓘ"
                hashMap["Hf&m"] = "Hand, Foot, and Mouth Disease (HFMD) ⓘ"
                hashMap["Measles"] = "Measles ⓘ"
                hashMap["Mumps"] = "Mumps ⓘ"
                hashMap["Roseola"] = "Roseola ⓘ"
                hashMap["Rubella"] = "Rubella ⓘ"
                hashMap["Scarlet Fever"] = "Scarlet Fever ⓘ"
                hashMap["Scd"] = "Slapped Cheek Disease (SCD) ⓘ"
                hashMap["Skin"] = "None"

                val txt_prediction = hashMap[prediction]
                txt_diagnosis.text = "$txt_prediction"

            }
            else{
                txt_diagnosis.text = "No picture to analyze"
            }
        }
    }

    fun unPackPixel(pixel: Int): IntArray {
        val rgb = IntArray(3)
        rgb[0] = pixel shr 16 and 0xFF
        rgb[1] = pixel shr 8 and 0xFF
        rgb[2] = pixel shr 0 and 0xFF
        return rgb
    }

    fun getBitmapPixels(bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): IntArray {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(
            pixels, 0, bitmap.width, x, y,
            width, height
        )
        val subsetPixels = IntArray(width * height)
        for (row in 0 until height) {
            System.arraycopy(
                pixels, row * bitmap.width,
                subsetPixels, row * width, width
            )
        }
        return subsetPixels
    }

    fun BitmapToArray(bitmap: Bitmap): IntArray? {
        val pixels: IntArray =
            getBitmapPixels(bitmap, 0, 0, bitmap.width, bitmap.height)
        val rgb_float_pixels = IntArray(3 * pixels.size)
        for (i in pixels.indices) {
            val rgb: IntArray = unPackPixel(pixels[i])
            rgb_float_pixels[3 * i] = rgb[0]
            rgb_float_pixels[3 * i + 1] = rgb[1]
            rgb_float_pixels[3 * i + 2] = rgb[2]
            //Log.d("PIXEL", String.format("r:%d, g:%d, b:%d", rgb[0],rgb[1],rgb[2]));
            //Log.d("PIXEL",""+rgb_float_pixels[i]);
        }
        return rgb_float_pixels
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
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { original_bitmap ->

            imageView.setImageBitmap(original_bitmap)
            val bitmap = Bitmap.createScaledBitmap(original_bitmap, 100, 100, false)


            val array = BitmapToArray(bitmap)
            if (array != null) {
                val float_array = FloatArray(array.size)
                /*
                val float_pixels = Array(3) { Array(100) { FloatArray(100) { 0F } } }
                for (i in 0..2) {
                    for (j in 0..99) {
                        for (k in 0..99) {
                            float_pixels[i][j][k] = array[i*(100*100)+j*100+k]/255F
                        }
                    }
                }*/


                for (i in array.indices){
                    float_array[i] = array[i]/255F
                }
                val model = context?.let { Model.newInstance(it) }

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(
                    intArrayOf(1, 100, 100, 3),
                    DataType.FLOAT32
                )
                val inputShape = intArrayOf(100, 100, 3) //the data shape before I flattened it
                val tensorBuffer = TensorBuffer.createFixedSize(inputShape, DataType.FLOAT32)
                Log.d("ARRAY LENGTH", array.size.toString())
                tensorBuffer.loadArray(float_array)
                //inputFeature0.loadArray(float_array)
                inputFeature0.loadBuffer(tensorBuffer.buffer)
                val outputs = model?.process(inputFeature0)
                val outputFeature0 = outputs?.outputFeature0AsTensorBuffer




                var string = ""
                if (outputFeature0 != null) {
                    for (value in  outputFeature0.floatArray){
                        string = string.plus(value.toString())
                            .plus(",")
                    }

                    val maxIdx = outputFeature0.floatArray.indices.maxBy { outputFeature0.floatArray[it] } ?: -1
                    val categories = arrayOf(
                        "Chickenpox",
                        "Hf&m",
                        "Measles",
                        "Mumps",
                        "Roseola",
                        "Rubella",
                        "Scarlet Fever",
                        "Scd",
                        "Skin"
                    )
                    if (maxIdx != -1){
                        prediction = categories[maxIdx]
                    }
                }

                // Releases model resources if no longer used.
                model?.close()
            }
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