package com.stall.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stall.calculator.ui.AppViewModel
import com.stall.calculator.ui.AppViewModelFactory
import com.stall.calculator.ui.screens.CartScreen
import com.stall.calculator.ui.screens.CategoryManageScreen
import com.stall.calculator.ui.screens.OrderScreen
import com.stall.calculator.ui.screens.ProductFormScreen
import com.stall.calculator.ui.screens.ProductManageScreen
import com.stall.calculator.ui.screens.SettingsScreen
import com.stall.calculator.ui.theme.CalculatorTheme
import com.stall.calculator.util.ExportCodec
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CalculatorApp
        setContent {
            CalculatorTheme {
                CalculatorNav(app)
            }
        }
    }
}

private object Destinations {
    const val ORDER = "order"
    const val CART = "cart"
    const val PRODUCTS = "products"
    const val PRODUCT_FORM = "product_form"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
}

@Composable
private fun CalculatorNav(app: CalculatorApp) {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(app.container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Destinations.ORDER
    ) {
        composable(Destinations.ORDER) {
            OrderScreen(
                state = state,
                onQueryChange = viewModel::updateOrderQuery,
                onSelectCategory = viewModel::updateSelectedCategory,
                onAddOne = viewModel::addToCart,
                onSetQty = viewModel::setQty,
                onComplete = viewModel::clearCart,
                onOpenCart = { navController.navigate(Destinations.CART) },
                onOpenProducts = { navController.navigate(Destinations.PRODUCTS) },
                onOpenCategories = { navController.navigate(Destinations.CATEGORIES) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        composable(Destinations.CART) {
            CartScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onSetQty = viewModel::setQty,
                onClear = viewModel::clearCart
            )
        }

        composable(Destinations.PRODUCTS) {
            ProductManageScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Destinations.PRODUCT_FORM) },
                onEdit = { id -> navController.navigate("${Destinations.PRODUCT_FORM}?productId=$id") },
                onDelete = viewModel::deleteProduct,
                onSetStatus = viewModel::setProductStatus,
                onOpenCategories = { navController.navigate(Destinations.CATEGORIES) },
                exportJson = {
                    ExportCodec.encode(viewModel.exportBundle())
                },
                importJson = { raw ->
                    val parsed = ExportCodec.decode(raw)
                    viewModel.importBundle(parsed, replaceExisting = true)
                }
            )
        }

        composable(
            route = "${Destinations.PRODUCT_FORM}?productId={productId}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("productId")?.takeIf { it > 0 }
            ProductFormScreen(
                state = state,
                productId = id,
                imageStorage = app.container.imageStorage,
                onBack = { navController.popBackStack() },
                onSave = { draft ->
                    viewModel.upsertProduct(draft)
                }
            )
        }

        composable(Destinations.CATEGORIES) {
            CategoryManageScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onUpsert = viewModel::upsertCategory,
                onDelete = viewModel::deleteCategory
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                settings = state.settings,
                onBack = { navController.popBackStack() },
                onUpdateShowCurrency = viewModel::updateShowCurrency,
                onUpdateConfirmBeforeComplete = viewModel::updateConfirmBeforeComplete,
                onUpdateAllowFreeProduct = viewModel::updateAllowFreeProduct,
                onUpdateRestoreCart = viewModel::updateRestoreCart,
                onUpdateShowSoldOut = viewModel::updateShowSoldOut
            )
        }
    }
}
