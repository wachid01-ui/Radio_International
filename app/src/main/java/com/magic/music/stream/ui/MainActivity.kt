package com.magic.music.stream.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.magic.music.stream.ui.screens.SingleStreamScreen
import com.magic.music.stream.ui.theme.MagicMusicTheme
import com.magic.music.stream.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicMusicTheme {
                SingleStreamScreen(viewModel = viewModel)
            }
        }
    }
}
