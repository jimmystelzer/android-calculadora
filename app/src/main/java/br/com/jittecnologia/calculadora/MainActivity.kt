package br.com.jittecnologia.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.jittecnologia.calculadora.ui.theme.CalculadoraTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.RoundingMode
import java.text.DecimalFormat

enum class AppMode { CALCULATOR, SHOPPING }

data class ShoppingItem(
    val id: Int,
    var price: String = "",
    var quantity: Int = 1
)

class CalculatorViewModel : ViewModel() {
    private val _currentMode = MutableStateFlow(AppMode.CALCULATOR)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _activeShoppingIndex = MutableStateFlow(0)
    val activeShoppingIndex: StateFlow<Int> = _activeShoppingIndex.asStateFlow()

    // Calculadora States
    private val _display = MutableStateFlow("0")
    val display: StateFlow<String> = _display.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    // Shopping States
    private val _shoppingItems = MutableStateFlow(listOf(ShoppingItem(id = 0)))
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    private val df = DecimalFormat("#,##########.##########").apply {
        roundingMode = RoundingMode.HALF_UP
    }

    private fun formatValue(value: Double): String {
        return if (value.isNaN()) "Erro" else df.format(value).replace(".", ",")
    }

    fun setMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun setActiveShoppingIndex(index: Int) {
        _activeShoppingIndex.value = index
    }

    fun onNumberClick(number: String) {
        if (_currentMode.value == AppMode.CALCULATOR) {
            if (_display.value == "0" && number != ",") {
                _display.value = number
            } else {
                val lastPart = _display.value.split(" ").last()
                if (number == "," && lastPart.contains(",")) return
                _display.value += number
            }
        } else {
            val index = _activeShoppingIndex.value
            val items = _shoppingItems.value.toMutableList()
            if (index < items.size) {
                val currentPrice = items[index].price
                if (number == "," && currentPrice.contains(",")) return
                updateShoppingPrice(index, currentPrice + number)
            }
        }
    }

    fun onOperatorClick(operator: String) {
        if (_currentMode.value == AppMode.CALCULATOR) {
            val currentDisplay = _display.value
            if (currentDisplay.isEmpty() || currentDisplay == "Erro") return
            if (currentDisplay.last() == ' ') {
                _display.value = currentDisplay.dropLast(3) + " $operator "
            } else {
                _display.value += " $operator "
            }
        } else {
            if (operator == "+") {
                updateShoppingQuantity(_activeShoppingIndex.value, true)
            } else if (operator == "-") {
                updateShoppingQuantity(_activeShoppingIndex.value, false)
            }
        }
    }

    fun onEqualsClick() {
        if (_currentMode.value == AppMode.CALCULATOR) {
            val expressionToEval = _display.value
            if (expressionToEval.isEmpty() || expressionToEval == "0") return
            try {
                val result = evaluateExpression(expressionToEval)
                _expression.value = "$expressionToEval ="
                _display.value = formatValue(result)
            } catch (e: Exception) {
                _display.value = "Erro"
            }
        } else {
            val nextIndex = _activeShoppingIndex.value + 1
            if (nextIndex < _shoppingItems.value.size) {
                _activeShoppingIndex.value = nextIndex
            }
        }
    }

    private fun evaluateExpression(expr: String): Double {
        val tokens = expr.replace(",", ".").split(" ").filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return 0.0
        val afterMd = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "×" || token == "÷") {
                val left = afterMd.removeAt(afterMd.size - 1).toDouble()
                val right = tokens[i + 1].toDouble()
                val res = if (token == "×") left * right else left / right
                afterMd.add(res.toString())
                i += 2
            } else {
                afterMd.add(token)
                i++
            }
        }
        var result = afterMd[0].toDouble()
        var j = 1
        while (j < afterMd.size) {
            val op = afterMd[j]
            val nextVal = afterMd[j + 1].toDouble()
            result = if (op == "+") result + nextVal else result - nextVal
            j += 2
        }
        return result
    }

    fun onClearClick() {
        if (_currentMode.value == AppMode.CALCULATOR) {
            _display.value = "0"
            _expression.value = ""
        } else {
            _shoppingItems.value = listOf(ShoppingItem(id = 0))
            _activeShoppingIndex.value = 0
        }
    }

    fun onDeleteClick() {
        if (_currentMode.value == AppMode.CALCULATOR) {
            val current = _display.value
            if (current.isEmpty() || current == "0") return
            _display.value = if (current.last() == ' ') current.dropLast(3) else current.dropLast(1)
            if (_display.value.isEmpty()) _display.value = "0"
        } else {
            val index = _activeShoppingIndex.value
            val items = _shoppingItems.value.toMutableList()
            if (index < items.size) {
                val currentPrice = items[index].price
                if (currentPrice.isNotEmpty()) {
                    updateShoppingPrice(index, currentPrice.dropLast(1))
                }
            }
        }
    }

    fun onPercentageClick() {
        if (_currentMode.value == AppMode.CALCULATOR) {
            val parts = _display.value.split(" ").toMutableList()
            val lastPart = parts.last()
            if (lastPart.isNotEmpty() && lastPart != "Erro") {
                try {
                    val num = lastPart.replace(",", ".").toDouble()
                    parts[parts.size - 1] = formatValue(num / 100)
                    _display.value = parts.joinToString(" ")
                } catch (e: Exception) { }
            }
        }
    }

    fun updateShoppingPrice(index: Int, price: String) {
        val newList = _shoppingItems.value.toMutableList()
        newList[index] = newList[index].copy(price = price)
        
        if (index == newList.size - 1 && price.isNotEmpty()) {
            newList.add(ShoppingItem(id = newList.size))
        }
        _shoppingItems.value = newList
    }

    fun updateShoppingQuantity(index: Int, increment: Boolean) {
        val newList = _shoppingItems.value.toMutableList()
        if (index < newList.size) {
            val currentQty = newList[index].quantity
            val newQty = if (increment) (currentQty + 1).coerceAtMost(100) else (currentQty - 1).coerceAtLeast(1)
            newList[index] = newList[index].copy(quantity = newQty)
            _shoppingItems.value = newList
        }
    }

    fun getShoppingTotal(): Double {
        return _shoppingItems.value.sumOf { 
            val p = it.price.replace(",", ".").toDoubleOrNull() ?: 0.0
            p * it.quantity
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .safeDrawingPadding(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CalculatorScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val display by viewModel.display.collectAsState()
    val expression by viewModel.expression.collectAsState()
    val mode by viewModel.currentMode.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val activeIndex by viewModel.activeShoppingIndex.collectAsState()
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val isLandscape = maxWidth > maxHeight
        val screenHeight = maxHeight
        val screenWidth = maxWidth

        val maxKeyboardHeightRatio = if (isLandscape) 0.8f else 0.4f
        
        // Cálculo dinâmico para garantir que o teclado não passe de 40% da altura
        val gridHeightLimit = screenHeight * maxKeyboardHeightRatio
        val maxButtonHeight = gridHeightLimit / 5
        val buttonWidth = if (isLandscape) (screenWidth * 0.4f) / 4 else screenWidth / 4
        val calculatedAspectRatio = (buttonWidth / maxButtonHeight).coerceAtLeast(if (isLandscape) 1.5f else 1.1f)

        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Coluna da Esquerda: Display / Lista de Compras / Total
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight(),
                    verticalArrangement = if (mode == AppMode.CALCULATOR) Arrangement.Center else Arrangement.Top
                ) {
                    if (mode == AppMode.SHOPPING) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Box(modifier = Modifier.heightIn(min = 100.dp, max = screenHeight * 0.7f)) {
                                ShoppingModeView(viewModel, shoppingItems, activeIndex)
                            }
                            
                            Text(
                                text = "TOTAL: R$ ${String.format("%.2f", viewModel.getShoppingTotal()).replace(".", ",")}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    } else {
                        CalculatorDisplay(
                            expression = expression,
                            display = display,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Coluna da Direita: Seleção de Modo + Teclado
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalIconToggleButton(
                            checked = mode == AppMode.CALCULATOR,
                            onCheckedChange = { viewModel.setMode(AppMode.CALCULATOR) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = "Calculadora", modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        FilledTonalIconToggleButton(
                            checked = mode == AppMode.SHOPPING,
                            onCheckedChange = { viewModel.setMode(AppMode.SHOPPING) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.LocalMall, contentDescription = "Compras", modifier = Modifier.size(24.dp))
                        }
                    }

                    CalculatorButtonGrid(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        aspectRatio = calculatedAspectRatio
                    )
                }
            }
        } else {
            // Modo Portrait
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    if (mode == AppMode.SHOPPING) {
                        Box(modifier = Modifier.heightIn(min = 150.dp, max = screenHeight * 0.45f)) {
                            ShoppingModeView(viewModel, shoppingItems, activeIndex)
                        }
                        
                        Text(
                            text = "TOTAL: R$ ${String.format("%.2f", viewModel.getShoppingTotal()).replace(".", ",")}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            textAlign = TextAlign.End
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                        CalculatorDisplay(
                            expression = expression,
                            display = display,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            fontSize = 48.sp
                        )
                    }
                }

                Column(modifier = Modifier.wrapContentHeight()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalIconToggleButton(
                            checked = mode == AppMode.CALCULATOR,
                            onCheckedChange = { viewModel.setMode(AppMode.CALCULATOR) },
                            modifier = Modifier.size(if (screenHeight < 600.dp) 48.dp else 56.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = "Calculadora", modifier = Modifier.size(if (screenHeight < 600.dp) 24.dp else 28.dp))
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        FilledTonalIconToggleButton(
                            checked = mode == AppMode.SHOPPING,
                            onCheckedChange = { viewModel.setMode(AppMode.SHOPPING) },
                            modifier = Modifier.size(if (screenHeight < 600.dp) 48.dp else 56.dp)
                        ) {
                            Icon(Icons.Default.LocalMall, contentDescription = "Compras", modifier = Modifier.size(if (screenHeight < 600.dp) 24.dp else 28.dp))
                        }
                    }

                    CalculatorButtonGrid(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        aspectRatio = calculatedAspectRatio
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingModeView(viewModel: CalculatorViewModel, items: List<ShoppingItem>, activeIndex: Int) {
    val listState = rememberLazyListState()
    
    // Garante que o item ativo esteja sempre visível na área de visão
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < items.size) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            val isActive = index == activeIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isActive) 2.dp else 0.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
                        .clickable { viewModel.setActiveShoppingIndex(index) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (item.price.isEmpty()) "R$ 0,00" else "R$ ${item.price}",
                        color = if (item.price.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }

                Text("×", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(56.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { viewModel.updateShoppingQuantity(index, false) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos")
                    }
                    Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                    IconButton(onClick = { viewModel.updateShoppingQuantity(index, true) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Mais")
                    }
                }

                Text("=", fontSize = 18.sp)

                val priceNum = item.price.replace(",", ".").toDoubleOrNull() ?: 0.0
                Text(
                    text = String.format("%.2f", priceNum * item.quantity).replace(".", ","),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun CalculatorDisplay(
    expression: String,
    display: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    textAlign: TextAlign = TextAlign.End,
    horizontalAlignment: Alignment.Horizontal = Alignment.End
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (expression.isNotEmpty()) {
            Text(
                text = expression,
                fontSize = fontSize * 0.5f,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = display,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = fontSize * 1.1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CalculatorButtonGrid(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f
) {
    val mode by viewModel.currentMode.collectAsState()
    val buttons = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ",", "=")
    )

    Column(modifier = modifier) {
        buttons.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { text ->
                    val weight = if (text == "0") 2f else 1f
                    val isEnabled = if (mode == AppMode.SHOPPING) {
                        text !in listOf("%", "÷", "×")
                    } else {
                        true
                    }
                    
                    CalculatorButton(
                        text = text,
                        enabled = isEnabled,
                        onClick = {
                            when (text) {
                                "C" -> viewModel.onClearClick()
                                "⌫" -> viewModel.onDeleteClick()
                                "%" -> viewModel.onPercentageClick()
                                "=" -> viewModel.onEqualsClick()
                                "÷", "×", "-", "+" -> viewModel.onOperatorClick(text)
                                else -> viewModel.onNumberClick(text)
                            }
                        },
                        modifier = Modifier
                            .weight(weight)
                            .aspectRatio(aspectRatio * weight)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isOperator = text in listOf("÷", "×", "-", "+", "=", "%", "⌫")
    val isSpecial = text == "C"
    
    val containerColor = when {
        isSpecial -> MaterialTheme.colorScheme.errorContainer
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    
    val contentColor = when {
        isSpecial -> MaterialTheme.colorScheme.onErrorContainer
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}
