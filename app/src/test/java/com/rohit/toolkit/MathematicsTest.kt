package com.rohit.toolkit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathematicsTest {

    @Test
    fun testGstExclusiveCalculation() {
        val amount = 1000.0
        val rate = 18.0
        val tax = amount * (rate / 100.0)
        val total = amount + tax

        assertEquals(180.0, tax, 0.001)
        assertEquals(1180.0, total, 0.001)
    }

    @Test
    fun testGstInclusiveCalculation() {
        val totalAmount = 1180.0
        val rate = 18.0
        val net = totalAmount / (1 + (rate / 100.0))
        val tax = totalAmount - net

        assertEquals(1000.0, net, 0.001)
        assertEquals(180.0, tax, 0.001)
    }

    @Test
    fun testMarksheetPercentage() {
        val obtained = 425.0
        val total = 500.0
        val percentage = (obtained / total) * 100.0

        assertEquals(85.0, percentage, 0.001)
    }

    @Test
    fun testProfitLossCalculation() {
        val cp = 200.0
        val sp = 250.0
        val profit = sp - cp
        val profitPercent = (profit / cp) * 100.0

        assertEquals(50.0, profit, 0.001)
        assertEquals(25.0, profitPercent, 0.001)
    }

    @Test
    fun testProportionRatio() {
        // A:B = C:D => D = (B * C) / A
        val a = 2.0
        val b = 4.0
        val c = 6.0
        val d = (b * c) / a

        assertEquals(12.0, d, 0.001)
    }
}
