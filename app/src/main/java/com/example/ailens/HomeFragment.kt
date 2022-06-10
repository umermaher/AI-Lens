package com.example.ailens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.ailens.databinding.FragmentHomeBinding
import com.example.ailens.databinding.FragmentSuccessBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!
    private var bottomSheetDialog:BottomSheetDialog?=null
    private lateinit var imageBitmap:Bitmap
    //cameraOrGallery variable is just for the first time when permission will be given by the user
    //I will initialize this var as 0 if user wanted to use the camera and 1 for the gallery
    private var cameraOrGallery:Int = 0
    private lateinit var title:String
    private lateinit var link:String
    private lateinit var displayedLink:String
    private lateinit var snippet:String
    private val searchList =SearchResultList()
    private lateinit var defaultDrawable:Drawable
    companion object{
        private const val MY_PERMISSIONS_REQUEST_CAMERA=1
        private const val MY_PERMISSIONS_REQUEST_GALLERY=2
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)  //create option menu in toolbar for the fragment
        binding.imageFab.setOnClickListener {
//           onImageFabClicked()
            if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraOrGallery=0
                activityResultLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            }
        }
        defaultDrawable=binding.imageToScan.drawable

        binding.searchBtn.setOnClickListener {
            onSearchClicked()
        }

        return binding.root
    }

    private fun onSearchClicked() {
//        if(binding.imageToScan.drawable.constantState!! == ActivityCompat.getDrawable(requireActivity(),R.drawable.ic_scanner)?.constantState){
    if(binding.imageToScan.drawable.constantState!! == defaultDrawable.constantState){
            Snackbar.make(binding.root,"Take a snap.",Snackbar.LENGTH_SHORT)
                .setAction("Ok"){}
                .setActionTextColor(ContextCompat.getColor(requireContext(),R.color.white))
                .show()
        }else{
        lifecycleScope.launch {
            getResults()
            delay(2000)
            applyAnimation()
            navigateToSuccess()
        }
        }
    }
    private fun getResults(){
        binding.progressBar.visibility=View.VISIBLE
        val firebaseVisionImage= FirebaseVisionImage.fromBitmap(imageBitmap)
        val labeler= FirebaseVision.getInstance().onDeviceImageLabeler

        labeler.processImage(firebaseVisionImage).addOnSuccessListener {
//            it -> (Mutable)List<FirebaseVisionImageLabel>
            val searchQuery= it[0].text
            getSearchResult(searchQuery)

        }.addOnFailureListener {
            Toast.makeText(requireContext(),"Failed to detect image...",Toast.LENGTH_LONG).show()
            binding.progressBar.visibility=View.GONE
        }
    }

    private fun getSearchResult(searchQuery: String) {
        val url="https://serpapi.com/search.json?engine=google&q=$searchQuery&api_key=7a4dbf1c3c59955d894f578dcb9f04fe40b1c7b2efddb711537c515b42b63966"
        val jsonObjectRequest=object: JsonObjectRequest(Request.Method.GET,url,null,
            {
                //it -> JSONObject
                try{
                    val organicResultArray=it.getJSONArray("organic_results")
                    for(i in 0 until organicResultArray.length()){
                        val organicObj:JSONObject= organicResultArray.getJSONObject(i)

                        if(organicObj.has("title"))
                            title=organicObj.getString("title")

                        if(organicObj.has("link"))
                            link=organicObj.getString("link")

                        if(organicObj.has("displayed_link"))
                            displayedLink=organicObj.getString("displayed_link")

                        if(organicObj.has("snippet"))
                            snippet=organicObj.getString("snippet")

                        searchList.add(SearchResult(title,link,displayedLink,snippet))
//                        Toast.makeText(requireContext(),title,Toast.LENGTH_LONG).show()
                    }
                }catch (e:JSONException){
                    e.printStackTrace()
                }
                binding.progressBar.visibility=View.GONE
            },{
                Toast.makeText(requireContext(),"No result found..",Toast.LENGTH_LONG).show()
                binding.progressBar.visibility=View.GONE
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers=HashMap<String,String>()
                headers["User-Agent"]="Mozilla/5.0"
                return headers
            }
        }
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest)
    }

    private fun navigateToSuccess() {
        val directions=HomeFragmentDirections.actionHomeFragmentToSuccessFragment(searchList)
        findNavController().navigate(directions)
//        findNavController().navigate(R.id.action_homeFragment_to_successFragment)
    }


    private suspend fun applyAnimation() {
        binding.searchBtn.isClickable=false
        binding.imageFab.isClickable=false
        binding.titleText.animate().alpha(0f).duration=500L
        binding.searchBtn.animate().alpha(0f).duration=500L
        binding.imageFab.animate().alpha(0f).duration=500L
        binding.imageToScan.animate().alpha(0f).translationXBy(1200f).duration=400L

        delay(300L)

        binding.successBackground.animate().alpha(1f).duration=600L
        binding.successBackground.animate().rotationBy(720f).duration=800L
        binding.successBackground.animate().scaleXBy(900f).duration=800L
        binding.successBackground.animate().scaleYBy(900f).duration=800L

        delay(500L)

        binding.successImage.animate().alpha(1f).duration=1000L

        delay(1000L)
    }

    private fun onImageFabClicked() {
        bottomSheetDialog=BottomSheetDialog(requireContext())
        bottomSheetDialog?.setContentView(R.layout.source_bottom_sheet)
        bottomSheetDialog?.setCanceledOnTouchOutside(true)
        val cameraFab=bottomSheetDialog?.findViewById<FloatingActionButton>(R.id.cameraFab)
        val galleryFab=bottomSheetDialog?.findViewById<FloatingActionButton>(R.id.galleryFab)
        val removeImageBtn=bottomSheetDialog?.findViewById<ImageView>(R.id.deleteImageBtn)

        cameraFab?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                cameraOrGallery=0
                activityResultLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
            }
        }

        galleryFab?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                cameraOrGallery=1
                activityResultLauncher.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }

        removeImageBtn?.setOnClickListener {
            binding.imageToScan.setImageResource(R.drawable.ic_scanner)
            binding.imageToScan.imageTintList= ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.show()
    }

    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    init{
        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }

            if(allAreGranted) {
                if(cameraOrGallery==0)
                    openCamera()
                else
                    openGallery()
            }
        }
    }

    private fun openCamera(){
        val intent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            binding.imageToScan.imageTintList=null
            when(requestCode){
                MY_PERMISSIONS_REQUEST_CAMERA -> {
                    //Use line of code 155, 156 only if you just capturing image and set in imageview
                    imageBitmap=data?.extras?.get("data") as Bitmap
                    binding.imageToScan.setImageBitmap(imageBitmap)
                }
                MY_PERMISSIONS_REQUEST_GALLERY -> {
                    imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(requireContext().contentResolver, data?.data!!)
                        )
                    } else {
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, data?.data)
                    }
//                    binding.imageToScan.setImageURI(data?.data)
                    binding.imageToScan.setImageBitmap(imageBitmap)
                }
            }
            bottomSheetDialog?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding=null
    }
}