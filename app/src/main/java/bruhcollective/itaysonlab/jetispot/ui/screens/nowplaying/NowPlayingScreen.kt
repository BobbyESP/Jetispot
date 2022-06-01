package bruhcollective.itaysonlab.jetispot.ui.screens.nowplaying

import androidx.annotation.StringRes
import androidx.collection.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bruhcollective.itaysonlab.jetispot.R
import bruhcollective.itaysonlab.jetispot.core.SpPlayerServiceManager
import bruhcollective.itaysonlab.jetispot.core.api.SpPartnersApi
import bruhcollective.itaysonlab.jetispot.core.util.SpUtils
import bruhcollective.itaysonlab.jetispot.ui.shared.MediumText
import bruhcollective.itaysonlab.jetispot.ui.shared.PlayPauseButton
import bruhcollective.itaysonlab.jetispot.ui.shared.PreviewableAsyncImage
import bruhcollective.itaysonlab.jetispot.ui.shared.PreviewableSyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.spotify.metadata.Metadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
fun NowPlayingScreen(
  navController: NavController,
  bottomSheetState: BottomSheetState,
  bsOffset: Float,
  viewModel: NowPlayingViewModel = hiltViewModel()
) {
  val mainPagerState = rememberPagerState()
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    // one-time VM-UI connection
    viewModel.uiOnTrackIndexChanged = { new ->
      scope.launch { mainPagerState.animateScrollToPage(new) }
    }
  }

  Box(Modifier.fillMaxSize()) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxSize()) {
      NowPlayingBackground(
        state = mainPagerState,
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize(),
      )

      // main content
      NowPlayingHeader(
        stateTitle = viewModel.getHeaderTitle(),
        state = viewModel.getHeaderText(), modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .statusBarsPadding()
      )

      NowPlayingControls(
        viewModel = viewModel, modifier = Modifier
          .padding(horizontal = 16.dp)
          .navigationBarsPadding()
      )
    }

    NowPlayingMiniplayer(
      viewModel,
      Modifier
        .fillMaxWidth()
        .height(72.dp)
        .align(Alignment.TopStart)
        .alpha(1f - bsOffset)
    )
  }
}

@Composable
fun NowPlayingMiniplayer(
  viewModel: NowPlayingViewModel,
  modifier: Modifier
) {
  Surface(tonalElevation = 8.dp, modifier = modifier) {
    Box(Modifier.fillMaxSize()) {
      LinearProgressIndicator(progress = viewModel.currentPosition.value.progressRange, color = MaterialTheme.colorScheme.primary, modifier = Modifier
        .height(2.dp)
        .fillMaxWidth())

      /*Surface(
        Modifier
          .height(2.dp)
          .fillMaxWidth(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
      ) {}
      Surface(
        Modifier
          .height(2.dp)
          .fillMaxWidth(viewModel.currentPosition.value.progressRange),
        color = MaterialTheme.colorScheme.primary
      ) {}*/

      Row(
        Modifier
          .fillMaxHeight()
          .padding(horizontal = 16.dp)
      ) {
        PreviewableSyncImage(
          viewModel.currentTrack.value.artworkCompose,
          placeholderType = "track",
          modifier = Modifier
            .size(48.dp)
            .align(Alignment.CenterVertically)
            .clip(RoundedCornerShape(8.dp))
        )

        Column(
          Modifier
            .weight(2f)
            .padding(horizontal = 14.dp)
            .align(Alignment.CenterVertically)
        ) {
          Text(
            viewModel.currentTrack.value.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp
          )
          Text(
            viewModel.currentTrack.value.artist,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        PlayPauseButton(
          viewModel.currentState.value == SpPlayerServiceManager.PlaybackState.Playing,
          { viewModel.togglePlayPause() },
          MaterialTheme.colorScheme.onSurface,
          Modifier
            .fillMaxHeight()
            .width(56.dp)
            .align(Alignment.CenterVertically)
        )
      }
    }
  }
}

@Composable
fun NowPlayingHeader(
  @StringRes stateTitle: Int,
  state: String,
  modifier: Modifier
) {
  Row(modifier) {
    IconButton(onClick = { /*TODO*/ }, Modifier.size(32.dp)) {
      Icon(imageVector = Icons.Default.ArrowDownward, tint = Color.White, contentDescription = null)
    }

    Column(Modifier.weight(1f)) {
      Text(
        text = stringResource(id = stateTitle).uppercase(),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        textAlign = TextAlign.Center,
        color = Color.White.copy(alpha = 0.7f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        letterSpacing = 2.sp,
        fontSize = 12.sp
      )

      Text(
        text = state,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
      )
    }

    IconButton(onClick = { /*TODO*/ }, Modifier.size(32.dp)) {
      Icon(imageVector = Icons.Default.MoreVert, tint = Color.White, contentDescription = null)
    }
  }
}

@Composable
fun NowPlayingControls(
  viewModel: NowPlayingViewModel,
  modifier: Modifier
) {
  Column(modifier, verticalArrangement = Arrangement.Bottom) {
    // Header
    MediumText(text = viewModel.currentTrack.value.title, fontSize = 24.sp, color = Color.White)
    Spacer(Modifier.height(2.dp))
    Text(text = viewModel.currentTrack.value.artist, fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
    Spacer(Modifier.height(4.dp))

    // Progressbar
    Slider(value = viewModel.currentPosition.value.progressRange, colors = SliderDefaults.colors(
      thumbColor = Color.White,
      activeTrackColor = Color.White,
      inactiveTrackColor = Color.White.copy(alpha = 0.5f)
    ), onValueChange = {})

    Row {
      Text(text = viewModel.currentPosition.value.progressMilliseconds.toString())
      Spacer(modifier = Modifier.weight(1f))
      Text(text = viewModel.currentTrack.value.duration.toString())
    }

    // Control Buttons

    // Additional Buttons
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NowPlayingBackground(
  state: PagerState,
  viewModel: NowPlayingViewModel,
  modifier: Modifier
) {
  val dominantColorAsBg = animateColorAsState(viewModel.currentBgColor.value)
  Box(modifier = modifier.background(dominantColorAsBg.value)) {
    HorizontalPager(
      count = viewModel.currentQueue.value.size,
      state = state,
      modifier = modifier
    ) { page ->
      NowPlayingBackgroundItem(
        track = viewModel.currentQueue.value[page],
        modifier = Modifier
          .align(Alignment.Center)
          .padding(bottom = (LocalConfiguration.current.screenHeightDp * 0.25).dp)
          .size((LocalConfiguration.current.screenWidthDp * 0.9).dp)
      )
    }
  }
}

@Composable
fun NowPlayingBackgroundItem(
  track: Metadata.Track,
  modifier: Modifier,
) {
  PreviewableAsyncImage(
    SpUtils.getImageUrl(track.album.coverGroup.imageList.find { it.size == Metadata.Image.Size.LARGE }?.fileId),
    "track",
    modifier = modifier
  )
}

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
  private val spPlayerServiceManager: SpPlayerServiceManager,
  private val spPartnersApi: SpPartnersApi
) : ViewModel(), SpPlayerServiceManager.ServiceExtraListener, CoroutineScope by MainScope() {
  // states
  val currentTrack get() = spPlayerServiceManager.currentTrack
  val currentPosition get() = spPlayerServiceManager.playbackProgress
  val currentState get() = spPlayerServiceManager.playbackState
  val currentContext get() = spPlayerServiceManager.currentContext
  val currentContextUri get() = spPlayerServiceManager.currentContextUri
  val currentQueue get() = spPlayerServiceManager.currentQueue
  val currentQueuePosition get() = spPlayerServiceManager.currentQueuePosition
  val currentBgColor = mutableStateOf(Color.Transparent)

  // ui bridges
  var uiOnTrackIndexChanged: (Int) -> Unit = {}

  // caches
  private val imageCache = LruCache<String, Color>(10)
  private var imageColorTask: Job? = null

  fun togglePlayPause() {
    spPlayerServiceManager.playPause()
  }

  init {
    spPlayerServiceManager.registerExtra(this)
  }

  override fun onCleared() {
    spPlayerServiceManager.unregisterExtra(this)
  }

  override fun onTrackIndexChanged(new: Int) {
    if (currentQueue.value.isEmpty()) return
    uiOnTrackIndexChanged.invoke(new)

    imageColorTask?.cancel()
    imageColorTask = launch(Dispatchers.IO) {
      currentBgColor.value = calculateDominantColor(
        spPartnersApi,
        SpUtils.getImageUrl(currentQueue.value[new].album.coverGroup.imageList.find { it.size == Metadata.Image.Size.LARGE }?.fileId) ?: return@launch,
        false
      )
    }
  }

  fun getHeaderTitle(): Int {
    if (currentContextUri.value == "") return R.string.playing_src_unknown
    var uriSeparated = currentContextUri.value.split(":").drop(1)
    if (uriSeparated[0] == "user") uriSeparated = uriSeparated.drop(2)
    return when (uriSeparated[0]) {
      "collection" -> R.string.playing_src_library
      "playlist" -> R.string.playing_src_playlist
      "album" -> R.string.playing_src_album
      else -> R.string.playing_src_unknown
    }
  }

  fun getHeaderText(): String {
    return when {
      currentContextUri.value.contains("collection") -> "Liked Songs" // TODO: to R.string
      else -> currentContext.value
    }
  }

  suspend fun calculateDominantColor(
    partnersApi: SpPartnersApi,
    url: String,
    dark: Boolean
  ): Color {
    return try {
      if (imageCache[url] != null) {
        return imageCache[url]!!
      }

      val apiResult =
        partnersApi.fetchExtractedColors(variables = "{\"uris\":[\"$url\"]}").data.extractedColors[0].let {
          if (dark) it.colorRaw else it.colorDark
        }.hex

      Color(android.graphics.Color.parseColor(apiResult)).also { imageCache.put(url, it) }
    } catch (e: Exception) {
      // e.printStackTrace()
      Color.Transparent
    }
  }
}