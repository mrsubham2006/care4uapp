package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.core.repository.CareNexusRepository
import com.example.ui.navigation.CareNexusMainNavigation
import com.example.ui.theme.CareNexusTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val repository = CareNexusRepository.getInstance(this)
    setContent {
      CareNexusTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          CareNexusMainNavigation(repository = repository)
        }
      }
    }
  }
}

