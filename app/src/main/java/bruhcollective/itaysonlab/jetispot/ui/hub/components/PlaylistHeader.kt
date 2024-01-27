package bruhcollective.itaysonlab.jetispot.ui.hub.components

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import bruhcollective.itaysonlab.jetispot.R
import bruhcollective.itaysonlab.jetispot.core.objs.hub.HubItem
import bruhcollective.itaysonlab.jetispot.ui.hub.LocalHubScreenDelegate
import bruhcollective.itaysonlab.jetispot.ui.hub.components.essentials.EntityActionStrip
import bruhcollective.itaysonlab.jetispot.ui.shared.MediumText
import bruhcollective.itaysonlab.jetispot.ui.shared.PreviewableAsyncImage
import bruhcollective.itaysonlab.jetispot.ui.shared.navClickable
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlaylistHeader(
    item: HubItem
) {
    val darkTheme = isSystemInDarkTheme()
    val dominantColor = remember { mutableStateOf(Color.Transparent) }
    val dominantColorAsBg = animateColorAsState(dominantColor.value)
    val delegate = LocalHubScreenDelegate.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            if (dominantColor.value != Color.Transparent) return@withContext
            dominantColor.value = delegate.calculateDominantColor(item.images?.main?.uri.toString(), darkTheme)
        }
    }

    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(dominantColorAsBg.value, Color.Transparent)
                )
            )
            .padding(top = 16.dp)
            .statusBarsPadding()
    ) {
        PreviewableAsyncImage(
            item.images?.main?.uri, "playlist", modifier = Modifier
                .size((LocalConfiguration.current.screenWidthDp * 0.7).dp)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        MediumText(
            text = item.text?.title!!, fontSize = 21.sp, modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )

        if (!item.text.subtitle.isNullOrEmpty()) {
            Text(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                text = item.text.subtitle, modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )
        }

        PlaylistHeaderAdditionalInfo(item.custom)
        EntityActionStrip(delegate, item)
    }
}

@Composable
fun LargePlaylistHeader(
    item: HubItem
) {
    val delegate = LocalHubScreenDelegate.current

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            AsyncImage(
                model = item.images?.main?.uri,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )

            Box(
                Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .fillMaxSize()
            )

            MediumText(
                text = item.text?.title!!,
                fontSize = 48.sp,
                lineHeight = 52.sp,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }

        if (!item.text?.subtitle.isNullOrEmpty()) {
            AndroidView(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                factory = { ctx ->
                    TextView(ctx).apply {
                        text = HtmlCompat.fromHtml(item.text?.subtitle!!, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                }
            )
        }

        PlaylistHeaderAdditionalInfo(item.custom)
        EntityActionStrip(delegate, item)
    }
}

@Composable
fun PlaylistHeaderAdditionalInfo(
    custom: Map<String, Any>?
) {
    custom ?: return

    val ownerPic = remember(custom) { custom["owner_pic"] as String }
    val ownerName = remember(custom) { custom["owner_name"] as String }
    val likesCount = remember(custom) { custom["likes_count"] as Long }
    val totalDuration = remember(custom) { custom["total_duration"] as String }

    Spacer(modifier = Modifier.height(12.dp))

    Row(Modifier
        .navClickable(
            enableRipple = false
        ) { navController -> navController.navigate(custom["owner_username"] as String) }
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        PreviewableAsyncImage(
            imageUrl = ownerPic, placeholderType = "user", modifier = Modifier
                .clip(CircleShape)
                .size(32.dp)
        )

        MediumText(
            text = ownerName, fontSize = 13.sp, modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 12.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(26.dp))

        Text(
            text = stringResource(id = R.string.likes_dot, likesCount, totalDuration),
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
}