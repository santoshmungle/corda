package net.corda.testing.driver

import net.corda.testing.node.internal.FINANCE_CORDAPP
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

class DriverParametersTest {
    @Test
    fun `extraCordappPackagesToScan and cordappsForAllNodes cannot be specified together`() {
        assertThatIllegalArgumentException()
                .isThrownBy { DriverParameters(cordappsForAllNodes = listOf(FINANCE_CORDAPP), extraCordappPackagesToScan = listOf("net.corda.finance")) }
                .withMessage("extraCordappPackagesToScan and cordappsForAllNodes cannot be specified together")
    }
}
