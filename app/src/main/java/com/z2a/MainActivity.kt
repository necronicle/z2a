package com.z2a

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.z2a.ui.navigation.Z2aNavHost
import com.z2a.ui.theme.Z2aTheme

class MainActivity : ComponentActivity() {

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val app = application as Z2aApplication
            app.vpnManager.startVpn()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Z2aTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Z2aNavHost(
                        onToggleVpn = {
                            val app = application as Z2aApplication
                            val prepareIntent = app.vpnManager.toggleVpn(this)
                            if (prepareIntent != null) {
                                vpnPermissionLauncher.launch(prepareIntent)
                            }
                        }
                    )
                }
            }
        }
    }
}
