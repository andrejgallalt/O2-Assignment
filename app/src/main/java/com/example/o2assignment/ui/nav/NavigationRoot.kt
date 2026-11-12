package com.example.o2assignment.ui.nav

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.o2assignment.ui.card.ActivateScreen
import com.example.o2assignment.ui.card.CardScreen
import com.example.o2assignment.ui.card.CardViewModel
import com.example.o2assignment.ui.card.ScratchScreen

@Composable
fun NavigationRoot() {
    val navController = rememberNavController()
    val viewModel: CardViewModel = hiltViewModel() //keep the shared view model between screens

    NavHost(navController = navController, startDestination = "card") {
        composable("card") { CardScreen(navController, viewModel) }
        composable("scratch") { ScratchScreen(navController, viewModel) }
        composable("activate") { ActivateScreen(navController, viewModel) }
    }
}