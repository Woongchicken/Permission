package com.example.permission

import android.os.Bundle
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.permission.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

// Android 13 (API 33) - READ_EXTERNAL_STORAGE의 권한 세분화 -> (이미지/사진 - READ_MEDIA_IMAGES  동영상 - READ_MEDIA_VIDEO  오디오 - READ_MEDIA_AUDIO)  https://developer.android.com/about/versions/13/behavior-changes-13?hl=ko
// Android 11 (API 30) - Target SDK 버전과 관계없이 모든 앱에 Scoped mode를 적용, 기존 권한(WRITE_EXTERNAL_STORAGE)이 무시 됌. MediaStore로 파일 저장 가능
// Android 10 (API 29) 이전 - READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE를 구분하여 사용


//사진 저장 & 갖고오기 (외부 저장소) - https://lab.cliel.com/283

class    MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var deniedCount:Int = 0



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonCamera.setOnClickListener{
            callCamera()
        }

        binding.buttonGallery.setOnClickListener{
            getAlbum()
        }



        /* WRITE_SETTINGS */
        binding.writeSetingsPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(isWriteSetingsGrated()){
                    Snackbar.make(binding.root,"WriteSetings Permission granted", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root,"WriteSetings Permission denied", Snackbar.LENGTH_SHORT).show()
                    openWriteSettings()
                }
            } else {
                Snackbar.make(binding.root, "WriteSetings Permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }


    }



    val CAMERA = arrayOf(Manifest.permission.CAMERA)
    val READ = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    val WRITE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val IMAGES = arrayOf(Manifest.permission.READ_MEDIA_IMAGES,)


    /* 권한 부여 여부 확인 */
    private fun isAllPermissionsGrated(permissions : Array<String>) : Boolean = permissions.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }

    /* 계약서 전달 방식 */
    @RequiresApi(Build.VERSION_CODES.M)
    private val requestPermissionLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->   // ActivityResultContracts - 미리 정의된 계약서를 전달 하여 Activity에서 결과를 쉽게 가져올수 있음. 계약서를 취득해서 권한을 설정
            permissions.entries.forEach{permission->                                                       // RequestMultiplePermissions : 여러개,  RequestPermission : 한개
                when {
                    permission.value -> {       // 권한 승인
                        Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
                    }
                    shouldShowRequestPermissionRationale(permission.key) -> {       // 권한 거부
                        Snackbar.make(binding.root, "Permission required to use app!", Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {  // 2번 거부
                        Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
                        if (deniedCount < 1){
                            openSettings()
                            deniedCount++
                        }
                    }

                }
            }
        }


    fun checkPermission(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(isAllPermissionsGrated(permissions)){
                return true
            } else {
                deniedCount = 0
                requestPermissionLauncher.launch(permissions)
                return false
            }
        }
        return true;
    }


    /* 카메라 기능 */
    fun callCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkPermission(CAMERA)) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureLauncher.launch(takePictureIntent)
//                startActivityForResult(takePictureIntent, CAMERA_CODE)
            }
        } else  {
            if (checkPermission(CAMERA) && checkPermission(WRITE)) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }



    /* 파일 이름 정하기 */
    fun randomFileName() : String {
        val fineName = this.getString(R.string.app_name) + SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        return fineName
    }



    /* 사진을 찍으면 사진 파일을 저장하고 저장된 사진을 불러오도록 */
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                CAMERA_CODE -> {
//                    if (data?.extras?.get("data") != null) {
//                        val img = data?.extras?.get("data") as Bitmap
//                        val uri = saveFile(RandomFileName(), "image/jpeg", img)
//                        binding.imageView.setImageURI(uri)
//                    }
//                }
//
//                STORAGE_CODE -> {
//                    val uri = data?.data
//                    binding.imageView.setImageURI(uri)
//                }
//
//            }
//        }
//    }


    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data?.extras?.get("data") != null) {
                    val img = data.extras?.get("data") as Bitmap
                    val uri = saveFile(randomFileName(), "image/jpeg", img)
                    binding.imageView.setImageURI(uri)
                }

            }
        }


    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val uri = data?.data
                binding.imageView.setImageURI(uri)
            }
        }


    /* 파일 저장 */
    fun saveFile(fileName: String, mimeType: String, bitmap: Bitmap): Uri? {
        var CV = ContentValues()
        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CV.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CV)

        if (uri != null) {
            var scriptor = contentResolver.openFileDescriptor(uri, "w")

            if (scriptor != null) {
                val fos = FileOutputStream(scriptor.fileDescriptor)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    CV.clear()
                    CV.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, CV, null, null)
                }
            }
        }

        return uri;
    }


    /*갤러리*/
    fun getAlbum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermission(IMAGES)) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                pickImageLauncher.launch(intent)

            }
        } else  {
            if (checkPermission(READ)) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                pickImageLauncher.launch(intent)
            }
        }
    }





        /* 권한 설정 화면 */
    private fun openSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package",packageName,null)
        }.run (::startActivity)
    }





    @RequiresApi(Build.VERSION_CODES.M)
    private fun isWriteSetingsGrated() : Boolean {
            return Settings.System.canWrite(this)
    }

    private fun openWriteSettings() {
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }.run (::startActivity)
    }




}




