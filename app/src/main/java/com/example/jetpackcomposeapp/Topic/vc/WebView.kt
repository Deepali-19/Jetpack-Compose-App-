package com.example.jetpackcomposeapp.Topic.vc

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

@Composable
fun VideoCallScreen(
    roomIdOverride: String? = null,
    userNameOverride: String? = null,
    autoStartCall: Boolean = false
) {
    val context = LocalContext.current
    val host = context as? VideoCallHost
    val activity = context as? FragmentActivity
    var callId by rememberSaveable { mutableStateOf(roomIdOverride.orEmpty()) }
    var activeCallId by rememberSaveable { mutableStateOf("") }
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }

    val userId = remember(firebaseUser) {
        firebaseUser?.uid ?: "user_${System.currentTimeMillis()}"
    }
    val userName = remember(userNameOverride, firebaseUser) {
        userNameOverride?.takeIf { it.isNotBlank() }
            ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
            ?: firebaseUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "User"
    }

    LaunchedEffect(autoStartCall, roomIdOverride) {
        if (autoStartCall && !roomIdOverride.isNullOrBlank()) {
            activeCallId = roomIdOverride
            callId = roomIdOverride
        }
    }

    DisposableEffect(host) {
        host?.prepareVideoCallPermissions()
        host?.enableVideoCallScreenMode()

        onDispose {
            host?.disableVideoCallScreenMode()
        }
    }

    if (activeCallId.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF150927), Color(0xFF5C00D3))
                    )
                )
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = callId,
                    onValueChange = { callId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter Call ID") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )

                Button(
                    onClick = {
                        if (callId.trim().isEmpty()) {
                            Toast.makeText(context, "Enter Call ID", Toast.LENGTH_SHORT).show()
                        } else {
                            activeCallId = callId.trim()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Start Call")
                }
            }
        }
    } else if (activity != null) {
        ZegoCallContainer(
            activity = activity,
            callId = activeCallId,
            userId = userId,
            userName = userName
        )
    }
}

@Composable
private fun ZegoCallContainer(
    activity: FragmentActivity,
    callId: String,
    userId: String,
    userName: String
) {
    val containerId = remember { View.generateViewId() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            FragmentContainerView(context).apply {
                id = containerId
            }
        }
    )

    DisposableEffect(activity, callId, userId, userName, containerId) {
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            1683641312L,
            "559f6f9a3837515eb008fbb287e1b7e7be16076879e9305b80abdb684d55fbe2",
            userId,
            userName,
            callId,
            config
        )

        activity.supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()

        onDispose {
            activity.supportFragmentManager.findFragmentById(containerId)?.let { currentFragment ->
                activity.supportFragmentManager.beginTransaction()
                    .remove(currentFragment)
                    .commit()
            }
        }
    }
}
