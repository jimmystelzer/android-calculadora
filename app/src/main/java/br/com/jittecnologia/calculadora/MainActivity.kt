package br.com.jittecnologia.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var display by remember { mutableStateOf("0") }
    var currentInput by remember { mutableStateOf("") }
    var currentOperator by remember { mutableStateOf<String?>(null) }
    var operand1 by remember { mutableStateOf<Double?>(null) }

    fun onNumberClick(number: String) {
        if (display == "0") {
            display = number
        } else {
            display += number
        }
        currentInput += number
    }

    fun onOperatorClick(operator: String) {
        if (currentInput.isNotEmpty()) {
            operand1 = display.toDouble()
            currentInput = ""
        }
        currentOperator = operator
        display += " $operator "
    }

    fun onEqualsClick() {
        if (operand1 != null && currentInput.isNotEmpty() && currentOperator != null) {
            val operand2 = currentInput.toDouble()
            val result = when (currentOperator) {
                "+" -> operand1!! + operand2
                "-" -> operand1!! - operand2
                "*" -> operand1!! * operand2
                "/" -> if (operand2 != 0.0) operand1!! / operand2 else Double.NaN
                else -> 0.0
            }
            display = if (result.isNaN()) "Erro" else result.toString()
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

        val buttonModifier = Modifier
            .padding(4.dp)
            .weight(1f)

        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(text = "7", onClick = { onNumberClick("7") }, modifier = buttonModifier)
            CalculatorButton(text = "8", onClick = { onNumberClick("8") }, modifier = buttonModifier)
            CalculatorButton(text = "9", onClick = { onNumberClick("9") }, modifier = buttonModifier)
            CalculatorButton(text = "/", onClick = { onOperatorClick("/") }, modifier = buttonModifier)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(text = "4", onClick = { onNumberClick("4") }, modifier = buttonModifier)
            CalculatorButton(text = "5", onClick = { onNumberClick("5") }, modifier = buttonModifier)
            CalculatorButton(text = "6", onClick = { onNumberClick("6") }, modifier = buttonModifier)
            CalculatorButton(text = "*", onClick = { onOperatorClick("*") }, modifier = buttonModifier)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(text = "1", onClick = { onNumberClick("1") }, modifier = buttonModifier)
            CalculatorButton(text = "2", onClick = { onNumberClick("2") }, modifier = buttonModifier)
            CalculatorButton(text = "3", onClick = { onNumberClick("3") }, modifier = buttonModifier)
            CalculatorButton(text = "-", onClick = { onOperatorClick("-") }, modifier = buttonModifier)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(text = "0", onClick = { onNumberClick("0") }, modifier = buttonModifier)
            CalculatorButton(text = "C", onClick = { onClearClick() }, modifier = buttonModifier)
            CalculatorButton(text = "=", onClick = { onEqualsClick() }, modifier = buttonModifier)
            CalculatorButton(text = "+", onClick = { onOperatorClick("+") }, modifier = buttonModifier)
        }
    }
}

@Composable
fun RowScope.CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f)
    ) {
        Text(text = text, fontSize = 24.sp)
    }
}
