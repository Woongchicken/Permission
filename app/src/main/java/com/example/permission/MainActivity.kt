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


class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//
//        }

            binding.locationIcon.setOnClickListener {
                if(isAllPermissionsGrated()) {
                    Snackbar.make(binding.root,"Permission granted", Snackbar.LENGTH_SHORT).show()
                } else {
//                requestDangerousPermissions()
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                }
            }


    }


    companion object {
        private val REQUIRED_PERMISSIONS : Array<String> = arrayOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//            Manifest.permission.READ_CALENDAR,
//            Manifest.permission.READ_BASIC_PHONE_STATE

                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS

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
    @RequiresApi(Build.VERSION_CODES.M)             // onRequestPermissionsResult - androidx.activity 버전 1.2.0 - deprecated
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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->   // ActivityResultContracts - 미리 정의된 계약서를 전달을 통해, Activity에서 결과를 쉽게 가져올수 있음. 계약서를 취득해서 권한을 설정
            permissions.entries.forEach{permission->                                                       // RequestMultiplePermissions : 여러개,  RequestPermission : 한개
                when {
                    permission.value -> {       // 권한 승인
                        Snackbar.make(binding.root, "Permission granted", Snackbar.LENGTH_SHORT).show()
                    }
                    shouldShowRequestPermissionRationale(permission.key) -> {       // 권한 거부
                        Snackbar.make(binding.root, "Permission required to use app!", Snackbar.LENGTH_SHORT).show()
                    }       // 2번 거부
                    else -> {
                        Snackbar.make(binding.root, "Permission denied", Snackbar.LENGTH_SHORT).show()
                        openSetings()
                    }

                }
            }
        }


    /* 권한 설정 화면 */
    private fun openSetings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package",packageName,null)
        }.run (::startActivity)
    }

}
