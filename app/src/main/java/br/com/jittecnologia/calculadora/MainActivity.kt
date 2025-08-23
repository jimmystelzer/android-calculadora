package br.com.jittecnologia.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.jittecnologia.calculadora.ui.theme.CalculadoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by rememberSaveable { mutableStateOf("0") }
    var currentInput by rememberSaveable { mutableStateOf("") }
    var currentOperator by rememberSaveable { mutableStateOf<String?>(null) }
    var operand1 by rememberSaveable { mutableStateOf<Double?>(null) }

    fun onNumberClick(number: String) {
        if (number == "," && currentInput.contains(",")) return
        if (display == "0" && number != ",") {
            display = number
        } else {
            display += number
        }
        currentInput += number
    }

    fun onOperatorClick(operator: String) {
        if (currentInput.isNotEmpty()) {
            if (currentInput.endsWith(",")) {
                currentInput = currentInput.dropLast(1)
            }
            operand1 = display.replace(",", ".").toDouble()
            currentInput = ""
        }
        currentOperator = operator
        display += " $operator "
    }

    fun onEqualsClick() {
        if (operand1 != null && currentInput.isNotEmpty() && currentOperator != null) {
            if (currentInput.endsWith(",")) {
                currentInput = currentInput.dropLast(1)
            }
            val operand2 = currentInput.replace(",", ".").toDouble()
            val result = when (currentOperator) {
                "+" -> operand1!! + operand2
                "-" -> operand1!! - operand2
                "*" -> operand1!! * operand2
                "/" -> if (operand2 != 0.0) operand1!! / operand2 else Double.NaN
                else -> 0.0
            }
            display = if (result.isNaN()) "Erro" else result.toString().replace(".", ",")
            currentInput = display
            operand1 = null
            currentOperator = null
        }
    }

    fun onClearClick() {
        display = "0"
        currentInput = ""
        currentOperator = null
        operand1 = null
    }

    fun onDeleteClick() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            display = display.dropLast(1)
        }
        if (display.isEmpty()) {
            display = "0"
        }
    }

    fun onPercentageClick() {
        if (currentInput.isNotEmpty()) {
            val number = currentInput.replace(",", ".").toDouble()
            val result = if (operand1 != null && currentOperator != null) {
                (operand1!! * number) / 100
            } else {
                number / 100
            }
            display = result.toString().replace(".", ",")
            currentInput = display
        }
    }

    val buttons = listOf(
        "C", "<-", "%", "/",
        "7", "8", "9", "*",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ",", "="
    )

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = display,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "C", onClick = { onClearClick() }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "<-", onClick = { onDeleteClick() }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "%", onClick = { onPercentageClick() }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "/", onClick = { onOperatorClick("/") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "7", onClick = { onNumberClick("7") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "8", onClick = { onNumberClick("8") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "9", onClick = { onNumberClick("9") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "*", onClick = { onOperatorClick("*") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "4", onClick = { onNumberClick("4") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "5", onClick = { onNumberClick("5") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "6", onClick = { onNumberClick("6") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "-", onClick = { onOperatorClick("-") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "1", onClick = { onNumberClick("1") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "2", onClick = { onNumberClick("2") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "3", onClick = { onNumberClick("3") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "+", onClick = { onOperatorClick("+") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "0", onClick = { onNumberClick("0") }, modifier = Modifier.weight(2f).aspectRatio(3f).padding(4.dp))
                        CalculatorButton(text = ",", onClick = { onNumberClick(",") }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                        CalculatorButton(text = "=", onClick = { onEqualsClick() }, modifier = Modifier.weight(1f).aspectRatio(1.5f).padding(4.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = display,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "C", onClick = { onClearClick() }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "<-", onClick = { onDeleteClick() }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "%", onClick = { onPercentageClick() }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "/", onClick = { onOperatorClick("/") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "7", onClick = { onNumberClick("7") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "8", onClick = { onNumberClick("8") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "9", onClick = { onNumberClick("9") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "*", onClick = { onOperatorClick("*") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "4", onClick = { onNumberClick("4") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "5", onClick = { onNumberClick("5") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "6", onClick = { onNumberClick("6") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "-", onClick = { onOperatorClick("-") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "1", onClick = { onNumberClick("1") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "2", onClick = { onNumberClick("2") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "3", onClick = { onNumberClick("3") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "+", onClick = { onOperatorClick("+") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CalculatorButton(text = "0", onClick = { onNumberClick("0") }, modifier = Modifier.weight(2f).aspectRatio(2f).padding(4.dp))
                        CalculatorButton(text = ",", onClick = { onNumberClick(",") }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        CalculatorButton(text = "=", onClick = { onEqualsClick() }, modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOperator = text in listOf("/", "*", "-", "+", "=", "%", "<-")
    val buttonColors = if (isOperator) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    } else {
        ButtonDefaults.buttonColors()
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = buttonColors
    ) {
        Text(text = text, fontSize = 24.sp)
    }
}