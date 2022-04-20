package bruhcollective.itaysonlab.jetispot.ui.screens.config

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.navigation.NavController
import bruhcollectie.itaysonlab.jetispot.proto.AppConfig
import bruhcollective.itaysonlab.jetispot.core.SpConfigurationManager
import bruhcollective.itaysonlab.jetispot.ui.ext.compositeSurfaceElevation
import kotlinx.coroutines.launch

interface ConfigViewModel {
  suspend fun modifyDatastore (runOnBuilder: AppConfig.Builder.() -> Unit)
  fun provideDataStore(): DataStore<AppConfig>
  fun provideConfigList(): List<ConfigItem>
  @StringRes fun provideTitle(): Int
  fun isRoot(): Boolean = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseConfigScreen(
  navController: NavController,
  viewModel: ConfigViewModel
) {
  val scrollBehavior = remember { TopAppBarDefaults.enterAlwaysScrollBehavior() }

  val scope = rememberCoroutineScope()
  val dsConfigState = viewModel.provideDataStore().data.collectAsState(initial = SpConfigurationManager.DEFAULT)
  val dsConfig = dsConfigState.value

  Scaffold(topBar = {
    LargeTopAppBar(title = {
      Text(stringResource(viewModel.provideTitle()))
    }, navigationIcon = {
      if (!viewModel.isRoot()) {
        Icon(Icons.Default.ArrowBack, null,
          Modifier
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = rememberRipple(bounded = false),
              onClick = {
                navController.popBackStack()
              }
            )
            .padding(horizontal = 16.dp))
      }
    }, modifier = Modifier.statusBarsPadding(), scrollBehavior = scrollBehavior)
  }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
    LazyColumn(Modifier.fillMaxHeight()) {
      items(viewModel.provideConfigList()) { item ->
        when (item) {
          is ConfigItem.Category -> {
            ConfigCategory(stringResource(item.title))
          }

          is ConfigItem.Info -> {
            ConfigInfo(stringResource(item.text))
          }

          is ConfigItem.Preference -> {
            ConfigPreference(
              stringResource(item.title),
              item.subtitle(LocalContext.current, dsConfig)
            ) {
              item.onClick(navController)
            }
          }

          is ConfigItem.Switch -> {
            ConfigSwitch(stringResource(item.title), stringResource(item.subtitle), item.switchState(dsConfig)) { newValue ->
              scope.launch { viewModel.modifyDatastore { item.modify(this, newValue) }}
            }
          }

          is ConfigItem.LargeSwitch -> {
            ConfigLargeSwitch(stringResource(item.title), item.switchState(dsConfig)) { newValue ->
              scope.launch { viewModel.modifyDatastore { item.modify(this, newValue) }}
            }
          }

          is ConfigItem.Radio -> {
            ConfigRadio(stringResource(item.title), stringResource(item.subtitle), item.radioState(dsConfig), item.enabledState(dsConfig)) {
              scope.launch { viewModel.modifyDatastore { item.modify(this) }}
            }
          }
        }
      }
    }
  }
}

@Composable
@Stable
fun ConfigCategory(
  text: String
) {
  Text(
    text = text,
    color = MaterialTheme.colorScheme.primary,
    fontSize = 14.sp,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
  )
}

@Composable
@Stable
fun ConfigInfo(
  text: String
) {
  Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Icon(Icons.Default.Info, contentDescription = null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)

    Text(
      text = text,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 12.dp)
    )
  }
}

@Composable
fun ConfigSwitch(
  title: String,
  subtitle: String,
  value: Boolean,
  onClick: (Boolean) -> Unit
) {
  Row(modifier = Modifier
    .fillMaxWidth()
    .clickable {
      onClick(!value)
    }
    .padding(16.dp)) {

    Column(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .align(Alignment.CenterVertically)
    ) {
      Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
      if (subtitle.isNotEmpty()) Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    Switch(
      checked = value, onCheckedChange = {}, modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterVertically)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigLargeSwitch(
  title: String,
  value: Boolean,
  onClick: (Boolean) -> Unit
) {
  val color = animateColorAsState(targetValue = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.5f).compositeOver(MaterialTheme.colorScheme.inverseSurface))
  Card(containerColor = color.value, shape = RoundedCornerShape(28.dp), onClick = {
     onClick(!value)
  }, modifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
      Text(text = title, color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 18.sp, modifier = Modifier
        .fillMaxWidth(0.85f)
        .align(Alignment.CenterVertically))

      Switch(
        checked = value, onCheckedChange = {}, modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.CenterVertically)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigRadio(
  title: String,
  subtitle: String,
  value: Boolean,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  Row(modifier = Modifier
    .fillMaxWidth()
    .clickable(enabled) { onClick() }
    .padding(vertical = 16.dp, horizontal = 6.dp)) {

    RadioButton(selected = value, onClick = { onClick() }, enabled = enabled, modifier = Modifier.align(Alignment.CenterVertically))

    Column(
      modifier = Modifier
        .padding(start = 16.dp)
        .fillMaxWidth()
        .align(Alignment.CenterVertically)
        .alpha(if (enabled) 1f else 0.7f)
    ) {
      Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
      if (subtitle.isNotEmpty()) Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 4.dp)
      )
    }
  }
}

@Composable
fun ConfigPreference(
  title: String,
  subtitle: String = "",
  onClick: () -> Unit
) {
  Column(modifier = Modifier
    .fillMaxWidth()
    .clickable { onClick() }
    .padding(16.dp)) {
    Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
    if (subtitle.isNotEmpty()) Text(
      text = subtitle,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 14.sp,
      modifier = Modifier.padding(top = 4.dp)
    )
  }
}

//

sealed class ConfigItem {
  class Category(@StringRes val title: Int) : ConfigItem()
  class Info(@StringRes val text: Int) : ConfigItem()

  class Preference(
    @StringRes val title: Int,
    val subtitle: (Context, AppConfig) -> String,
    val onClick: (NavController) -> Unit
  ) : ConfigItem()

  class Switch(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val switchState: (AppConfig) -> Boolean,
    val modify: AppConfig.Builder.(value: Boolean) -> Unit
  ) : ConfigItem()

  class LargeSwitch(
    @StringRes val title: Int,
    val switchState: (AppConfig) -> Boolean,
    val modify: AppConfig.Builder.(value: Boolean) -> Unit
  ) : ConfigItem()

  class Radio(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val radioState: (AppConfig) -> Boolean,
    val enabledState: (AppConfig) -> Boolean,
    val modify: AppConfig.Builder.() -> Unit
  ) : ConfigItem()
}