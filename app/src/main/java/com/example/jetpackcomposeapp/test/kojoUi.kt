package com.example.jetpackcomposeapp.test

import android.R.attr.subtitle
import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.sync.Mutex

@Composable
fun BreakfastScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF2E8),
                    Color(0xFFFFC4A8),
                    Color(0xFFFFB08D)
                )
            )),
        contentAlignment = Alignment.Center
    ){
        BreakfastCard()
    }
}
@Composable
fun ReviewScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF2E8),
                    Color(0xFFFFC4A8),
                    Color(0xFFFFB08D)
                )
            )),
        contentAlignment = Alignment.Center
    ){
//        ReviewCard()
    }
}

@Composable
fun BreakfastCard(){
    Column(
        modifier = Modifier
            .width(300.dp)
            .height(500.dp)
            .shadow(15.dp, RoundedCornerShape(25.dp))
            .padding(26.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "K O J O",
                color = Color(0xFF191B2E),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = Color(0xFF191B2E),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Breakfast",
            color = Color(0xFF191B2E),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Until 12pm",
            color = Color(0xFF191B2E),
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryBox("🥧","Trat",true)
            CategoryBox("🥭","Fruit",false)
            CategoryBox("🍞","Toast",true)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Grab & Go",
            color = Color(0xFF191B2E),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        FoodItem(
            title = "⭐️Fruit Bowl",
            subtitle="1700kg/430kcal",
            price="$10.90",
            color = Color(0xFF191B2E),
        )

        Spacer(modifier = Modifier.height(12.dp))

        FoodItem(
            title="Acai Na Tigled",
            subtitle="1700kg/430kcal",
            price="$10.90",
            emoji = "🍫"
        )







    }
}


































@Preview
@Composable
fun BreakfastScreenPreview(){
    BreakfastScreen()
}

@Preview
@Composable
fun ReviewScreenPreview(){
    ReviewScreen()
}