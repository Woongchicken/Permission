package com.example.permission

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.permission.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.permissionButton.setOnClickListener{
            permissionCheck()
        }
    }

    /* 권한 승인 여부 */
    private fun permissionCheck() {
        if (versionCehck()){
            if (checkSelfPermission()) {

                Log.d(Constants.TAG, "${this::class.simpleName} " + "권한 : 승인 상태")

                ...

            } else {

                Log.d(Constants.TAG, "${this::class.simpleName} " + "권한 : 거절 상태")

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        PERMISSIONS.toString())) {
                    // 1. 사용자가 승인 거절을 누른 상태

                    Log.d(Constants.TAG, "${this::class.simpleName} " + "사용자가 승인 거절을 누른 경우")

                    requestPermissions()

                } else {
                    // 2. 사용자가 승인 거절 + 다시 표시하지 않기를 누른 경우
                    // 3. 아직 승인 요청을 한 적이 없는 경우

                    Log.d(Constants.TAG, "${this::class.simpleName} " + "사용자가 승인 거절 + 다시 표시하지 않기를 누른 경우")

                    requestPermissions()
                }
            }
        }
    }


    /* SDK 버전 확인 */
    private fun versionCehck() : Boolean {
        if (android.os.Build.VERSION.SDK_INT >= SDK_VERSION) {
            return true
        }else{
            return false
        }
    }

    /* 권한 승인 여부 확인 */
    private fun checkSelfPermission(): Boolean {
        Log.d(Constants.TAG, "${this::class.simpleName} 권한 체크를 시작합니다.")

        PERMISSIONS.forEach {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, it) != PackageManager.PERMISSION_GRANTED) {

                Log.d(Constants.TAG, "${this::class.simpleName} $it 권한 : 거절")

                return false
            }
        }
        return true
    }

    /* 사용자에게 권한 요청 */
    private fun requestPermissions() {
        Log.d(Constants.TAG, "${this::class.simpleName} 권한 요청을 시작합니다.")
        ActivityCompat.requestPermissions(
            this,
            PERMISSIONS,
            Constants.REQUEST_CODE)
    }



}
