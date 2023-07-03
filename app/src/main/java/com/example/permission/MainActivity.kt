package com.example.permission

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.permission.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


//
//"API 수준에 따라 앱에서 요구하는 퍼미션(permission)의 동작 방식"
//
//각 API별로 구현해야하며,
//변경되는 시뮬레이터 별로 어떤식으로 퍼미션권한을 얻을 수 있는지
//
//read, write를 기준으로 하여 각 API 별로 크게 변경되는 사항을 정리하고 구현
//
//============================================
//* API 레벨 22 이하:
//퍼미션은 설치 시에 모두 허용
//사용자는 동의를 요청하거나 거부할 수 있는 방법이 없습니다.
//
//
//*API 레벨 23 이상:
//퍼미션은 런타임 시에 사용자에게 요청
//앱이 퍼미션을 사용해야 하는 시점에서 사용자에게 요청 대화상자가 표시됩니다.
//사용자는 허용 또는 거부할 수 있습니다.
//
//*API 레벨 30 이상
//앱은 미리 정의된 일부 퍼미션에 대한 권한을 자동으로 얻을 수 있습니다. 이러한 퍼미션은 시스템 권한으로 분류됩니다.
//일부 민감한 퍼미션에 대해서는 여전히 사용자의 승인이 필요합니다.
//
//
//*영구 거부
//Android 11(API 수준 30)부터 사용자가  특정 권한에 관해 거부를 두 번 이상 탭하면
//앱에서 그 권한을 다시 요청하는 경우 사용자에게 시스템 권한 대화상자가 표시되지 않습니다.
//사용자가 권한 요청을 두 번 이상 거부하면 영구 거부로 간주됩니다.
//이전 버전에서는 앱에서 권한을 요청할 때마다 사용자에게 시스템 권한 대화상자가 표시되었습니다
//
//*일회성 권한
//'이번만 허용'이라는 옵션
//앱에서 일회성 권한을 요청할 때 표시되는 시스템 대화상자
//Android 11(API 수준 30)부터 앱이 위치, 마이크 또는 카메라와 관련된 권한을 요청할 때마다 사용자에게 표시되는 권한 대화상자에 이번만 허용이라는 옵션이 포함됩니다.
//
//
//
//*권한의 종류
//1.설치 시간 권한 -  앱이 설치될 때 자동으로 부여됩니다.
//1) 일반권한 - normal 보호 수준을 일반 권한에 할당
//2) 서명권한 - 정의하는 앱이나 OS와 동일한 인증서로 앱이 서명된 경우에만 앱에 서명 권한을 부여
//
//2.런타임 권한(위험한 권한) - 앱에서 추가 단계를 거쳐 런타임에 권한을 요청해야 합니다.
//앱에서는 제한된 데이터에 추가로 액세스하거나 시스템과 다른 앱에 더 큰 영향을 미치는 제한된 작업을 실행
//ex) 위치와 연락처 정보, 마이크와 카메라
//
//3. 특별 권한
//특정 앱 작업에 관한 것입니다.
//플랫폼과 OEM만이 특별 권한을 정의할 수 있습니다.
//
//
//
//
//*READ
//
//@
//READ_ASSISTANT_APP_SEARCH_DATA
//READ_HOME_APP_SEARCH_DATA
//
//
//(0) 시스템 앱에서만 사용 가능한 권한
//READ_INPUT_STATE
//READ_LOGS
//READ_NEARBY_STREAMING_POLICY
//READ_PRECISE_PHONE_STATE
//
//
//(1) 일반권한 (보호수준 - 정상(normal)),
//READ_BASIC_PHONE_STATE
//SYNC_SETTINGS
//READ_SYNC_STATS
//
//
//(2) 서명권한  (보호수준 - 서명(signature))
//READ_VOICEMAIL
//
//
//(3) 런타임 권한 (보호 수준 - 위험)
//READ_CALENDAR
//READ_CALL_LOG
//READ_CONTACTS
//READ_MEDIA_AUDIO
//READ_SMS
//READ_PHONE_STATE
//@READ_PHONE_NUMBERS - API 레벨 33부터 이 권한이 적용(?)
//
//READ_EXTERNAL_STORAGE -  API 레벨 33부터는 이 권한이 적용되지 않습니다. 앱이 다른 앱의 미디어 파일에 액세스하는 경우 대신 다음 권한 중 하나 이상을 요청하세요. READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO.
//READ_MEDIA_IMAGES -  API 레벨 33부터 이 권한이 적용
//READ_MEDIA_VIDEO  -  API 레벨 33부터 이 권한이 적용
//
//REQUIRED_EXTERNAL_STORAGE_PERMISSIONS
//REQUIRED_MEDIA_IMAGES_PERMISSIONS
//
//
//
//*WRITE
//
//
//(0) 시스템 앱에서만 사용 가능한 권한
//WRITE_APN_SETTINGS
//WRITE_GSERVICES
//WRITE_SECURE_SETTINGS
//
//
//(1) 보호 수준 - 정상(normal),
//WRITE_SYNC_SETTINGS
//
//(2) 서명권한  (보호수준 - 서명(signature))
//WRITE_VOICEMAIL
//
//(3) 런타임 권한 (보호 수준 - 위험)
//WRITE_CALENDAR
//WRITE_CALL_LOG
//WRITE_CONTACTS
//WRITE_EXTERNAL_STORAGE - Build.VERSION_CODES.R(API 레벨 30)이상을 대상으로 하는 경우 이 권한은 적용되지 않습니다.
//
//
//(4) 특별권한
//WRITE_SETTINGS - 앱이 API 레벨 23 이상을 대상으로 하는 경우 앱 사용자는 권한 관리 화면을 통해 앱에 이 권한을 명시적으로 부여
//앱에 대한 시스템 설정 변경 권한입니다. 이 권한은 앱이 기기의 시스템 설정을 수정할 수 있는 권한을 부여
//
//


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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


        /* WRITE_EXTERNAL_STORAGE 권한 체크 (Build.VERSION_CODES.R) / 30이상 추가 설정 -> 저장소 공간 개념 */
        binding.writeExternalStorage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    Snackbar.make(binding.root,"Build.VERSION_CODES.R(API 레벨 30)이상을 대상으로 하는 경우 이 권한은 적용되지 않습니다.", Snackbar.LENGTH_SHORT).show()
                } else {
                    if(isAllPermissionsGrated()) {
                        Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
                    } else {
                        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                    }
                }
            } else {
                Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }



        /* READ_EXTERNAL_STORAGE 권한 체크 (Build.VERSION_CODES.TIRAMISU) */
//        binding.readExternalStorage.setOnClickListener {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//                    Snackbar.make(binding.root,"Build.VERSION_CODES.TIRAMISU(API 레벨 33)이상을 대상으로 하는 경우 이 권한은 적용되지 않습니다.", Snackbar.LENGTH_SHORT).show()
//                } else {
//                    if(isAllPermissionsGrated()) {
//                        Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
//                    } else {
//                        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
//                    }
//                }
//            } else {
//                Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
//            }
//        }


        binding.readExternalStorage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if(isReadExternalStoragePermisionGrated()){
                        Snackbar.make(binding.root," ReadMediaImagesPermission Granted", Snackbar.LENGTH_SHORT)
                    }
                    else {
                        requestPermissionLauncher.launch(REQUIRED_EXTERNAL_STORAGE_PERMISSIONS)
                    }
                } else {
                    if(isReadMediaImagesPermisionGrated()){
                        Snackbar.make(binding.root," ReadMediaImagesPermission Granted", Snackbar.LENGTH_SHORT)
                    }
                    else {
                        requestPermissionLauncher.launch(REQUIRED_MEDIA_IMAGES_PERMISSIONS)
                    }
                }
            } else {
                Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }



        /* READ_MEDIA_IMAGES 권한 체크 (Build.VERSION_CODES.TIRAMISU) */
        binding.readMediaImages.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(isAllPermissionsGrated()) {
                    Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
                } else {
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                }
            } else {
                Snackbar.make(binding.root,"Build.VERSION_CODES.TIRAMISU(API 레벨 33)이하를 대상으로 하는 경우 이 권한은 적용되지 않습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }




            /* WRITE_SETTINGS */
        binding.writeSetingsPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(isWriteSetingsGrated()){
                    Snackbar.make(binding.root,"WriteSetings Permission granted", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root,"WriteSetings Permission denied", Snackbar.LENGTH_SHORT).show()
                    openWriteSetings()
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
//            Manifest.permission.WRITE_CONTACTS


            /*Build.VERSION_CODES.TIRAMISU 이상*/
//            Manifest.permission.READ_PHONE_NUMBERS

            /*Build.VERSION_CODES.R 이상*/
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private val REQUIRED_EXTERNAL_STORAGE_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )

        private val REQUIRED_MEDIA_IMAGES_PERMISSIONS : Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        )


        private const val REQUEST_CODE_PERMISSIONS = 1001
    }




    /* 권한 부여 여부 확인 */
    private fun isAllPermissionsGrated() : Boolean = REQUIRED_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }


    /* 권한 요청 */
    private  fun requestDangerousPermissions() {
        ActivityCompat.requestPermissions(
            this,REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )

    }

    /* 권한 요청에 대한 콜백 */
            // onRequestPermissionsResult - androidx.activity 버전 1.2.0 - deprecated
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(        // ActivityCompat.requestPermissions을 요청하면 결과를 콜백으로 받음
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {        // 모든 권한이 취득된 경우
                Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
            } else {
                if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {        // 권한을 다시 요청
                    Snackbar.make(binding.root,"Permission required to use app!", Snackbar.LENGTH_SHORT).show()
                    requestDangerousPermissions()
                } else {
                    Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()      // 그럼에도 유저가 거절하면, 앱은 권한을 더 이상 요청을 못함. 권한 요청을 2번하면 유저가 직접 셋팅에서 권한 설정
                    openSetings()       // 권한 설정 화면 띄워줌
                }
            }
        }
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
                        openSetings()
                    }

                }
            }
        }


    /* 권한 설정 화면 */
    private fun openSetings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package",packageName,null)
        }.run (::startActivity)
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun isWriteSetingsGrated() : Boolean {
            return Settings.System.canWrite(this)
    }

    private fun openWriteSetings() {
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }.run (::startActivity)
    }




//    /*READ_EXTERNAL_STORAGE*/
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun isReadExternalStoragePermissionGranted(): Boolean {
//        return REQUIRED_PERMISSIONS.any { permission ->
//            when (permission) {
//                Manifest.permission.READ_EXTERNAL_STORAGE  -> {
//                    return true
//                }
//                // 다른 특별 권한이 있을 경우 해당 권한 처리 추가
//                else -> false
//            }
//        }
//    }
//
//    /*READ_EXTERNAL_STORAGE*/
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun isReadMediaImagesPermissionGranted(): Boolean {
//        return REQUIRED_PERMISSIONS.any { permission ->
//            when (permission) {
//                Manifest.permission.READ_MEDIA_IMAGES   -> {
//                    return true
//                }
//                // 다른 특별 권한이 있을 경우 해당 권한 처리 추가
//                else -> false
//            }
//        }
//    }


    private fun isReadExternalStoragePermisionGrated() : Boolean = REQUIRED_EXTERNAL_STORAGE_PERMISSIONS.all { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }

    private fun isReadMediaImagesPermisionGrated() : Boolean = REQUIRED_MEDIA_IMAGES_PERMISSIONS.any { permission ->      // .all을 써서 REQUIRED_PERMISSIONS의 권한 중 하나라도 권한이 부여가 안 되어있으면 false 반환
        ContextCompat.checkSelfPermission(this,permission) ==       // 해당 권한이 부여되었는지 확인
                PackageManager.PERMISSION_GRANTED
    }





}
