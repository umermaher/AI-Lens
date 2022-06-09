package com.example.ailens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ailens.databinding.FragmentHomeBinding
import com.example.ailens.databinding.FragmentSuccessBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!
    private lateinit var bottomSheetDialog:BottomSheetDialog

    //cameraOrGallery variable is just for the first time when permission will be given by the user
    //I will initialize this var as 0 if user wanted to use the camera and 1 for the gallery
    private var cameraOrGallery:Int = 0

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

        binding.imageFab.setOnClickListener {
           onImageFabClicked()
        }

        binding.searchBtn.setOnClickListener {
            onSearchClicked()
        }

        return binding.root
    }

    private fun onSearchClicked() {
//        if(binding.imageToScan.drawable==ActivityCompat.getDrawable(requireContext(),R.drawable.ic_scanner)){
//            Snackbar.make(binding.root,"Field empty.",Snackbar.LENGTH_SHORT)
//                .setAction("Ok"){}
//                .setActionTextColor(ContextCompat.getColor(requireContext(),R.color.white))
//                .show()
//        }else{
//
//        }
        lifecycleScope.launch {
            applyAnimation()
            navigateToSuccess()
        }
    }

    @SuppressLint("ResourceType")
    private fun navigateToSuccess() {
        findNavController().navigate(R.layout.fragment_success)
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
        bottomSheetDialog.setContentView(R.layout.source_bottom_sheet)
        bottomSheetDialog.setCanceledOnTouchOutside(true)
        val cameraFab=bottomSheetDialog.findViewById<FloatingActionButton>(R.id.cameraFab)
        val galleryFab=bottomSheetDialog.findViewById<FloatingActionButton>(R.id.galleryFab)
        val removeImageBtn=bottomSheetDialog.findViewById<ImageView>(R.id.deleteImageBtn)

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
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
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
                    val bitmap=data?.extras?.get("data") as Bitmap
                    binding.imageToScan.setImageBitmap(bitmap)
//                    Toast.makeText(requireContext(),"$bitmap",Toast.LENGTH_LONG).show()
                }
                MY_PERMISSIONS_REQUEST_GALLERY -> {
                    binding.imageToScan.setImageURI(data?.data)
//                    Toast.makeText(requireContext(), data?.data.toString(),Toast.LENGTH_LONG).show()
                }
            }
            bottomSheetDialog.dismiss()
        }
    }
}