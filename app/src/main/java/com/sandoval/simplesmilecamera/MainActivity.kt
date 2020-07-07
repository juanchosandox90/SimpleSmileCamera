package com.sandoval.simplesmilecamera

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.sandoval.simplesmilecamera.auth.AuthActivity
import com.sandoval.simplesmilecamera.face.LiveFaceAnalyseActivity
import com.sandoval.simplesmilecamera.push.GetTokenAction
import com.sandoval.simplesmilecamera.utils.Constant
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val displayName = intent.getStringExtra("displayName")
        if (displayName!!.isNotEmpty()) {
            displayNameText.text = displayName
        } else {
            Log.d("DisplayName: ", "Username")
        }
        if (!allPermissionsGranted()) {
            runtimePermissions
        }
        logoutBtn.setOnClickListener {
            logoutHuaweiId()
        }
    }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PERMISSION_REQUESTS) {
            return
        }
        var isNeedShowDiag = false
        for (i in permissions.indices) {
            if (permissions[i] == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[i] != PackageManager.PERMISSION_GRANTED
            ) {
                // If permissions aren't satisfied, show dialog.
                isNeedShowDiag = true
            }
        }
        if (isNeedShowDiag && !ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CALL_PHONE
            )
        ) {
            val dialog: AlertDialog = AlertDialog.Builder(this)
                .setMessage(getString(R.string.camera_permission_rationale))
                .setPositiveButton(
                    getString(R.string.settings)
                ) { _, _ ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, 200)
                    startActivity(intent)
                }
                .setNegativeButton(
                    getString(R.string.cancel)
                ) { _, _ -> finish() }.create()
            dialog.show()
        }
    }

    fun mostPeopleSmile(view: View?) {
        val intent = Intent(this@MainActivity, LiveFaceAnalyseActivity::class.java)
        intent.putExtra(Constant.DETECT_MODE, Constant.MOST_PEOPLE)
        startActivity(intent)
    }

    fun nearestPeopleSmile(view: View?) {
        val intent = Intent(this@MainActivity, LiveFaceAnalyseActivity::class.java)
        intent.putExtra(Constant.DETECT_MODE, Constant.NEAREST_PEOPLE)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUESTS = 1
        private fun isPermissionGranted(
            context: Context,
            permission: String?
        ): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }

    private fun logoutHuaweiId() {
        val mAuthParam = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .createParams()
        val mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParam)
        val logoutTask = mAuthManager.signOut()
        logoutTask.addOnSuccessListener {
            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
            finish()
        }
        logoutTask.addOnFailureListener {
            Toast.makeText(this@MainActivity, "Logout Failed!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        //no haga nada
    }
}