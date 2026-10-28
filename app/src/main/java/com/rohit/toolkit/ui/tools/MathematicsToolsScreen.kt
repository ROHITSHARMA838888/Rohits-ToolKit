package com.rohit.toolkit.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.toolkit.ui.components.CommonToolScaffold
import java.text.DecimalFormat

@Composable
fun MathematicsToolScreen(
    toolId: String,
    onBack: () -> Unit
) {
    when (toolId) {
        "math_gst" -> GstCalculatorScreen(onBack)
        "math_percentage" -> PercentageCalculatorScreen(onBack)
        "math_profit_loss" -> ProfitLossCalculatorScreen(onBack)
        "math_discount" -> DiscountCalculatorScreen(onBack)
        "math_ratio" -> RatioCalculatorScreen(onBack)
        "math_converter" -> UnitConverterScreen(onBack)
        else -> GstCalculatorScreen(onBack)
    }
}

// 1. GST Calculator
@Composable
fun GstCalculatorScreen(onBack: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var gstRateText by remember { mutableStateOf("18") }
    var isInclusive by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val gstRate = gstRateText.toDoubleOrNull() ?: 18.0

    val df = remember { DecimalFormat("#,##0.00") }

    val (netAmount, gstAmount, totalAmount) = remember(amount, gstRate, isInclusive) {
        if (amount <= 0) Triple(0.0, 0.0, 0.0)
        else if (isInclusive) {
            val net = amount / (1 + (gstRate / 100.0))
            val tax = amount - net
            Triple(net, tax, amount)
        } else {
            val tax = amount * (gstRate / 100.0)
            val total = amount + tax
            Triple(amount, tax, total)
        }
    }

    CommonToolScaffold(title = "GST Calculator", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Base / Total Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select GST Rate (%):", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("5", "12", "18", "28").forEach { rate ->
                        FilterChip(
                            selected = gstRateText == rate,
                            onClick = { gstRateText = rate },
                            label = { Text("$rate%") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isInclusive,
                        onCheckedChange = { isInclusive = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GST Inclusive (Tax already inside price)")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Calculation Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                ResultRow("Net Amount", "₹ ${df.format(netAmount)}")
                ResultRow("CGST (${gstRate / 2}%)", "₹ ${df.format(gstAmount / 2)}")
                ResultRow("SGST (${gstRate / 2}%)", "₹ ${df.format(gstAmount / 2)}")
                ResultRow("Total GST Tax", "₹ ${df.format(gstAmount)}")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Final Total Amount", "₹ ${df.format(totalAmount)}", isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { amountText = ""; gstRateText = "18" },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Reset Form")
        }
    }
}

// 2. Percentage & Marksheet Calculator
@Composable
fun PercentageCalculatorScreen(onBack: () -> Unit) {
    var mode by remember { mutableStateOf(0) } // 0: Standard, 1: Marksheet
    var marksObtained by remember { mutableStateOf("") }
    var totalMarks by remember { mutableStateOf("") }

    val obtained = marksObtained.toDoubleOrNull() ?: 0.0
    val total = totalMarks.toDoubleOrNull() ?: 0.0

    val percentage = if (total > 0) (obtained / total) * 100.0 else 0.0
    val grade = when {
        percentage >= 90 -> "A+ (Outstanding)"
        percentage >= 80 -> "A (Excellent)"
        percentage >= 70 -> "B+ (Very Good)"
        percentage >= 60 -> "B (First Division)"
        percentage >= 50 -> "C (Second Division)"
        percentage >= 33 -> "D (Third Division)"
        obtained > 0 -> "F (Needs Improvement)"
        else -> "-"
    }

    CommonToolScaffold(title = "Percentage Calculator", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = mode) {
            Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("Standard %") })
            Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("Marksheet %") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = marksObtained,
                    onValueChange = { marksObtained = it },
                    label = { Text(if (mode == 0) "Value / Part" else "Marks Obtained") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { totalMarks = it },
                    label = { Text(if (mode == 0) "Total / Whole" else "Maximum / Total Marks") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Result", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                ResultRow("Percentage", String.format("%.2f %%", percentage), isHighlight = true)
                if (mode == 1) {
                    ResultRow("Grade / Division", grade)
                }
            }
        }
    }
}

// 3. Profit & Loss Calculator
@Composable
fun ProfitLossCalculatorScreen(onBack: () -> Unit) {
    var costPriceText by remember { mutableStateOf("") }
    var sellingPriceText by remember { mutableStateOf("") }

    val cp = costPriceText.toDoubleOrNull() ?: 0.0
    val sp = sellingPriceText.toDoubleOrNull() ?: 0.0

    val diff = sp - cp
    val percent = if (cp > 0) (Math.abs(diff) / cp) * 100.0 else 0.0

    CommonToolScaffold(title = "Profit & Loss", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it },
                    label = { Text("Cost Price (CP) ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it },
                    label = { Text("Selling Price (SP) ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (diff >= 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (diff >= 0) "Profit Summary" else "Loss Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(12.dp))

                ResultRow("Status", if (diff > 0) "Net Profit" else if (diff < 0) "Net Loss" else "Break Even")
                ResultRow("Amount", String.format("₹ %.2f", Math.abs(diff)), isHighlight = true)
                ResultRow("Percentage", String.format("%.2f %%", percent))
            }
        }
    }
}

// 4. Discount Calculator
@Composable
fun DiscountCalculatorScreen(onBack: () -> Unit) {
    var priceText by remember { mutableStateOf("") }
    var discountPercentText by remember { mutableStateOf("10") }

    val price = priceText.toDoubleOrNull() ?: 0.0
    val discRate = discountPercentText.toDoubleOrNull() ?: 0.0

    val savings = price * (discRate / 100.0)
    val finalPrice = price - savings

    CommonToolScaffold(title = "Discount Calculator", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Original Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = discountPercentText,
                    onValueChange = { discountPercentText = it },
                    label = { Text("Discount Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Savings Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                ResultRow("Original Price", String.format("₹ %.2f", price))
                ResultRow("Discount Amount (You Save)", String.format("₹ %.2f", savings))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ResultRow("Final Price to Pay", String.format("₹ %.2f", finalPrice), isHighlight = true)
            }
        }
    }
}

// 5. Ratio Calculator
@Composable
fun RatioCalculatorScreen(onBack: () -> Unit) {
    var aText by remember { mutableStateOf("") }
    var bText by remember { mutableStateOf("") }
    var cText by remember { mutableStateOf("") }

    val a = aText.toDoubleOrNull() ?: 0.0
    val b = bText.toDoubleOrNull() ?: 0.0
    val c = cText.toDoubleOrNull() ?: 0.0

    // Solve for D in A:B = C:D => D = (B * C) / A
    val d = if (a != 0.0) (b * c) / a else 0.0

    CommonToolScaffold(title = "Ratio Calculator", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Proportion Solver (A : B = C : D)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = aText,
                        onValueChange = { aText = it },
                        label = { Text("A") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = bText,
                        onValueChange = { bText = it },
                        label = { Text("B") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cText,
                        onValueChange = { cText = it },
                        label = { Text("C") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = if (d != 0.0) String.format("%.2f", d) else "",
                        onValueChange = {},
                        label = { Text("D (Result)") },
                        readOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// 6. Length & Unit Converter
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    var inputValue by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("Length") }

    val inputNum = inputValue.toDoubleOrNull() ?: 1.0

    CommonToolScaffold(title = "Unit Converter", categoryName = "Mathematics", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Length", "Weight", "Data").forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text(if (selectedCategory == "Length") "Meters (m)" else if (selectedCategory == "Weight") "Kilograms (kg)" else "Megabytes (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Converted Equivalents", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))

                when (selectedCategory) {
                    "Length" -> {
                        ResultRow("Kilometers (km)", "${inputNum / 1000.0} km")
                        ResultRow("Centimeters (cm)", "${inputNum * 100.0} cm")
                        ResultRow("Millimeters (mm)", "${inputNum * 1000.0} mm")
                        ResultRow("Feet (ft)", String.format("%.3f ft", inputNum * 3.28084))
                        ResultRow("Inches (in)", String.format("%.2f in", inputNum * 39.3701))
                    }
                    "Weight" -> {
                        ResultRow("Grams (g)", "${inputNum * 1000.0} g")
                        ResultRow("Pounds (lbs)", String.format("%.3f lbs", inputNum * 2.20462))
                        ResultRow("Ounces (oz)", String.format("%.2f oz", inputNum * 35.274))
                        ResultRow("Metric Ton (t)", "${inputNum / 1000.0} t")
                    }
                    "Data" -> {
                        ResultRow("Gigabytes (GB)", "${inputNum / 1024.0} GB")
                        ResultRow("Terabytes (TB)", "${inputNum / (1024.0 * 1024.0)} TB")
                        ResultRow("Kilobytes (KB)", "${inputNum * 1024.0} KB")
                        ResultRow("Bytes (B)", "${inputNum * 1024.0 * 1024.0} Bytes")
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isHighlight) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
