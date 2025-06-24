package com.custom.customswipetodismissinjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.custom.customswipetodismissinjetpackcompose.ui.theme.CustomSwipeToDismissInJetpackComposeTheme
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomSwipeToDismissInJetpackComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwipeToDismissList(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToDismissList(modifier: Modifier = Modifier) {
    val randomList = remember { mutableStateListOf(*List(150) { "Item ${it + 1}" }.toTypedArray()) }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.dp.toPx() }

    LazyColumn(modifier = modifier) {
        items(randomList) { item ->
            var offsetX by remember { mutableStateOf(0f) }
            var dismissed by remember { mutableStateOf(false) }
            var isSwipeReleased by remember { mutableStateOf(false) }

            val animatedOffsetX by animateFloatAsState(
                targetValue = if (dismissed) screenWidthPx else offsetX,
                animationSpec = if (isSwipeReleased) tween(durationMillis = 300) else tween(
                    durationMillis = 0
                ),
                finishedListener = {
                    if (dismissed && it >= screenWidthPx) {
                        dismissed = false
                        isSwipeReleased = false
                        randomList.remove(item)
                        offsetX = 0f
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down =
                                        awaitPointerEvent().changes.firstOrNull { it.pressed }
                                            ?: continue

                                    var dragAmount = 0f
                                    while (down.pressed) {
                                        val event = awaitPointerEvent()
                                        val drag = event.changes.firstOrNull() ?: break
                                        val deltaX = drag.positionChange().x
                                        val deltaY = drag.positionChange().y
                                        isSwipeReleased = false

                                        if (deltaX > 0 && abs(deltaX) > abs(deltaY) * 2 || offsetX > 0) {
                                            offsetX += deltaX
                                            dragAmount += deltaX

                                            drag.consume()
                                        }

                                        // Pointer released — swipe ends
                                        if (!drag.pressed) {
                                            println("Swipe released")
                                            isSwipeReleased = true

                                            // Now decide if it's a dismissal or reset
                                            if (abs(offsetX) > screenWidthPx * 0.5) {
                                                dismissed = true
                                            } else {
                                                offsetX = 0f
                                            }

                                            break
                                        }
                                    }
                                }
                            }
                        }
                        .background(Color.White)
                ) {
                    Text(
                        text = item,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
