package bruhcollective.itaysonlab.jetispot.ui.dac.components_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bruhcollective.itaysonlab.jetispot.ui.dac.LocalDacDelegate
import bruhcollective.itaysonlab.jetispot.ui.ext.dynamicUnpack
import bruhcollective.itaysonlab.jetispot.ui.shared.MediumText
import bruhcollective.itaysonlab.jetispot.ui.shared.PreviewableAsyncImage
import bruhcollective.itaysonlab.jetispot.ui.shared.Subtext
import bruhcollective.itaysonlab.jetispot.ui.shared.navClickable
import com.spotify.home.dac.component.v1.proto.AlbumCardMediumComponent
import com.spotify.home.dac.component.v1.proto.ArtistCardMediumComponent
import com.spotify.home.dac.component.v1.proto.EpisodeCardMediumComponent
import com.spotify.home.dac.component.v1.proto.PlaylistCardMediumComponent
import com.spotify.home.dac.component.v1.proto.SectionComponent
import com.spotify.home.dac.component.v1.proto.ShowCardMediumComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SectionComponentBinder(
  item: SectionComponent
) {
  val localDacDelegate = LocalDacDelegate.current

  val list = item.componentsList.map { it.dynamicUnpack() }
  LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
    items(list) { listItem ->
      when (listItem) {
        is AlbumCardMediumComponent -> MediumCard(
          title = listItem.title,
          subtitle = listItem.subtitle,
          navigateUri = listItem.navigateUri,
          imageUri = listItem.imageUri,
          imagePlaceholder = "album"
        )

        is PlaylistCardMediumComponent -> {
          LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
              localDacDelegate.updateRootlistImage(listItem.navigateUri, listItem.imageUri, overwrite = true)
            }
          }
          MediumCard(
            title = listItem.title,
            subtitle = listItem.subtitle,
            navigateUri = listItem.navigateUri,
            imageUri = listItem.imageUri,
            imagePlaceholder = "playlist"
          )
        }

        is ArtistCardMediumComponent -> MediumCard(
          title = listItem.title,
          subtitle = listItem.subtitle,
          navigateUri = listItem.navigateUri,
          imageUri = listItem.imageUri,
          imagePlaceholder = "artist"
        )

        is EpisodeCardMediumComponent -> MediumCard(
          title = listItem.title,
          subtitle = listItem.subtitle,
          navigateUri = listItem.navigateUri,
          imageUri = listItem.imageUri,
          imagePlaceholder = "podcasts"
        )

        is ShowCardMediumComponent -> MediumCard(
          title = listItem.title,
          subtitle = listItem.subtitle,
          navigateUri = listItem.navigateUri,
          imageUri = listItem.imageUri,
          imagePlaceholder = "podcasts"
        )
      }
    }
  }
}

@Composable
fun MediumCard(
  title: String,
  subtitle: String,
  navigateUri: String,
  imageUri: String,
  imagePlaceholder: String
) {
  val size = 160.dp

  Column(
    Modifier
      .width(size)
      .navClickable { navController ->
        navController.navigate(navigateUri)
      }) {
    var drawnTitle = false

    PreviewableAsyncImage(
      imageUrl = imageUri, placeholderType = imagePlaceholder, modifier = Modifier
        .size(size)
        .clip(
          if (imagePlaceholder == "artist") CircleShape else RoundedCornerShape(if (imagePlaceholder == "podcasts") 12.dp else 0.dp)
        )
    )

    if (title.isNotEmpty()) {
      drawnTitle = true
      MediumText(title, modifier = Modifier.padding(top = 8.dp).align(if (imagePlaceholder == "artist") Alignment.CenterHorizontally else Alignment.Start))
    }

    if (subtitle.isNotEmpty()) {
      Subtext(subtitle, modifier = Modifier.padding(top = if (drawnTitle) 4.dp else 8.dp).align(if (imagePlaceholder == "artist") Alignment.CenterHorizontally else Alignment.Start))
    }
  }
}