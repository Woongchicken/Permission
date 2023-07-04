package com.example.permission

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
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


//비트맵 - https://devsmin.tistory.com/27
//외부 저장소 - https://ddangeun.tistory.com/58  https://heeeju4lov.tistory.com/50


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        saveImageToGallery()

//        binding.bitmapButton.setOnClickListener {
//            saveImageToGallery()
//        }

        /* 런타임 권한 체크 */
        binding.runtimePermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(isAllPermissionsGrated()) {
                    Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
                } else {
//                requestDangerousPermissions()
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                }
            } else {
                // 안드로이드 버전이 M 미만이므로 권한을 부여할 필요 없음
                Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }




//        API 레벨 29(Q) 이전 - READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE 를 구분하여 사용
//        API 레벨 30(R) 이상 - WRITE_EXTERNAL_STORAGE 대신 READ_EXTERNAL_STORAGE만 요청
//        API 레벨 33(TIRAMISU) 이상 - READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO 나눠서 요청

        binding.testPermission.setOnClickListener {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 레벨 33(TIRAMISU) 이상
                if(isReadMediaImagesPermissionsGrated()){
                    Snackbar.make(binding.root,"READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO", Snackbar.LENGTH_SHORT).show()
                } else {
                    requestPermissionLauncher.launch(REQUIRED_MEDIA_IMAGES_PERMISSIONS)
                }
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {    // API 레벨 30(R) 이상
                if(isReadExternalStoragePermissionsGrated()){
                    Snackbar.make(binding.root,"READ_EXTERNAL_STORAGE", Snackbar.LENGTH_SHORT).show()
                } else {
                    requestPermissionLauncher.launch(REQUIRED_READ_EXTERNAL_STORAGE_PERMISSIONS)
                }
            } else {    // API 레벨 29(Q) 이전
                if(isAllExternalStoragePermissionsGrated()){
                    Snackbar.make(binding.root,"READ_EXTERNAL_STORAGE & WRITE_EXTERNAL_STORAGE ", Snackbar.LENGTH_SHORT).show()
                } else {
                    requestPermissionLauncher.launch(REQUIRED_ALL_EXTERNAL_STORAGE_PERMISSIONS)
                }
            }

        }







//@ READ_EXTERNAL_STORAGE = WRITE_EXTERNAL_STORAGE 동일 권한?
/*        READ_EXTERNAL_STORAGE
        API 레벨 33(TIRAMISU)부터 -> READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO 권한이 대신 적용*/
        binding.readExternalStorage.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(isReadMediaImagesPermissionsGrated()){
                    Snackbar.make(binding.root,"READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    requestPermissionLauncher.launch(REQUIRED_MEDIA_IMAGES_PERMISSIONS)
                }
            } else {
                if(isReadExternalStoragePermissionsGrated()){
                    Snackbar.make(binding.root,"READ_EXTERNAL_STORAGE", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    requestPermissionLauncher.launch(REQUIRED_READ_EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
        }



/*         WRITE_EXTERNAL_STORAGE
        API 레벨 30(R)부터 -> 이 권한은 적용되지 않음 MANAGE_EXTERNAL_STORAGE 권한 사용 */

        binding.writeExternalStorage.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(isManageExternalStoragePermissionGranted()){
                    Snackbar.make(binding.root,"MANAGE_EXTERNAL_STORAGE", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    openManageExternalStorageSettings()
                }
            } else {
                if(isWriteExternalStoragePermissionsGrated()){
                    Snackbar.make(binding.root,"WRITE_EXTERNAL_STORAGE", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    requestPermissionLauncher.launch(REQUIRED_WRITE_EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
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


    /* 권한 명시 */
    companion object {
        private val REQUIRED_PERMISSIONS : Array<String> = arrayOf(

//            Manifest.permission.READ_CALENDAR,
//            Manifest.permission.READ_CALL_LOG,
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.READ_MEDIA_AUDIO,
//            Manifest.permission.READ_SMS,
//            Manifest.permission.READ_PHONE_STATE
//            Manifest.permission.WRITE_CALENDAR,
//            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.WRITE_CONTACTS


            /*Build.VERSION_CODES.TIRAMISU 이상*/
//            Manifest.permission.READ_PHONE_NUMBERS

            /*Build.VERSION_CODES.R 이상*/
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private val REQUIRED_READ_EXTERNAL_STORAGE_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private val REQUIRED_MEDIA_IMAGES_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )

        private val REQUIRED_WRITE_EXTERNAL_STORAGE_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private val REQUIRED_ALL_EXTERNAL_STORAGE_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )



        private const val REQUEST_CODE_PERMISSIONS = 1001
    }




    /* 권한 부여 여부 확인 */
    private fun isAllPermissionsGrated() : Boolean = REQUIRED_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }


    /* 권한 요청 */
//    private  fun requestDangerousPermissions() {
//        ActivityCompat.requestPermissions(
//            this,REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//        )
//
//    }

    /* 권한 요청에 대한 콜백 */
            // onRequestPermissionsResult - androidx.activity 버전 1.2.0 - deprecated
//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onRequestPermissionsResult(        // ActivityCompat.requestPermissions을 요청하면 결과를 콜백으로 받음
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {        // 모든 권한이 취득된 경우
//                Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
//            } else {
//                if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {        // 권한을 다시 요청
//                    Snackbar.make(binding.root,"Permission required to use app!", Snackbar.LENGTH_SHORT).show()
//                    requestDangerousPermissions()
//                } else {
//                    Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()      // 그럼에도 유저가 거절하면, 앱은 권한을 더 이상 요청을 못함. 권한 요청을 2번하면 유저가 직접 셋팅에서 권한 설정
//                    openSetings()       // 권한 설정 화면 띄워줌
//                }
//            }
//        }
//    }

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
                        openSettings()
                    }

                }
            }
        }


    /* 권한 설정 화면 */
    private fun openSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package",packageName,null)
        }.run (::startActivity)
    }


    /*READ_EXTERNAL_STORAGE & *WRITE_EXTERNAL_STORAG 권한 체크*/
    private fun isAllExternalStoragePermissionsGrated() : Boolean = REQUIRED_ALL_EXTERNAL_STORAGE_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }




    /*READ_EXTERNAL_STORAGE 권한 체크 (READ_EXTERNAL_STORAGE)*/
    private fun isReadExternalStoragePermissionsGrated() : Boolean = REQUIRED_READ_EXTERNAL_STORAGE_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }

    /*READ_EXTERNAL_STORAGE 권한 체크 (READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO)*/
    private fun isReadMediaImagesPermissionsGrated() : Boolean = REQUIRED_MEDIA_IMAGES_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }


    /*WRITE_EXTERNAL_STORAGE 권한 체크 (WRITE_EXTERNAL_STORAGE)*/
    private fun isWriteExternalStoragePermissionsGrated() : Boolean = REQUIRED_WRITE_EXTERNAL_STORAGE_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }

    /*MANAGE_EXTERNAL_STORAGE 권한 체크 */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun isManageExternalStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }


    /*MANAGE_EXTERNAL_STORAGE 권한 설정*/
    private fun openManageExternalStorageSettings() {
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
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


    private fun saveImageToGallery(){
        binding.bitmapButton.setOnClickListener {
            //권한 체크

//            if(!isReadExternalStoragePermissionsGrated() || !isWriteExternalStoragePermissionsGrated()){
//                Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            //그림 저장
            val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
            if(!imageExternalSave(this, bitmap, this.getString(R.string.app_name))){
                Toast.makeText(this, "그림 저장을 실패하였습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "그림이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
        }
    }


    fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {

            val rootPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val dirName = "/" + path
            val fileName = System.currentTimeMillis().toString() + ".png"
            val savePath = File(rootPath + dirName)
            savePath.mkdirs()

            val file = File(savePath, fileName)
            if (file.exists()) file.delete()

            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                //갤러리 갱신
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    )
                )
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }








}
