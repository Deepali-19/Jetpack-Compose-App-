package com.example.jetpackcomposeapp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.jetpackcomposeapp.Navigation.Dest
import com.example.jetpackcomposeapp.Utils.DataStoreManager
import com.example.jetpackcomposeapp.ViewModels.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(outerNavController: NavHostController) {
    val viewModel: ProfileViewModel = viewModel()
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    var savedToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        dataStoreManager.accessToken.collect { token ->
            savedToken = token

            if (!token.isNullOrBlank() && viewModel.user == null && !viewModel.isLoading) {
                viewModel.getUserData("Bearer $token")
            }

            if (token.isNullOrBlank()) {
                viewModel.user = null
                viewModel.errorMessage = "Please login first"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF151515),
                        Color(0xFF2D1640),
                        Color(0xFF7A00FF)
                    )
                )
            )
    ) {
        if (viewModel.isLoading && viewModel.user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF353535)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = viewModel.user?.avatar,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A4A4A)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3D3D3D))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
                            ) {
                                ProfileDetailRow("Name", viewModel.user?.name ?: "No name")
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Email", viewModel.user?.email ?: "No email")
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Role", viewModel.user?.role ?: "No role")
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Avatar", viewModel.user?.avatar ?: "No avatar")
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Id", viewModel.user?.id?.toString() ?: "No id")
                            }
                        }

                        viewModel.errorMessage?.let {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = it,
                                color = Color(0xFFFF8A80),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        scope.launch {
                            dataStoreManager.clearTokens()
                            outerNavController.navigate(Dest.LOGIN) {
                                popUpTo(Dest.MAIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8E8E8),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(50.dp),
                    enabled = !savedToken.isNullOrBlank()
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label :",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = Color(0xFFE3E3E3),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
