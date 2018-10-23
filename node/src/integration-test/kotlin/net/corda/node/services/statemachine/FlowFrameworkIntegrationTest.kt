package net.corda.node.services.statemachine

import co.paralleluniverse.fibers.Suspendable
import com.natpryce.hamkrest.assertion.assertThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.internal.concurrent.transpose
import net.corda.core.internal.packageName
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.unwrap
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.singleIdentity
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.internal.cordappsForPackages
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.Test

class FlowFrameworkIntegrationTest {
    @Test
    fun `unknown flow in session-init is replayed on restart`() {
        driver(DriverParameters(cordappsForAllNodes = emptyList(), notarySpecs = emptyList())) {
            val (alice, bob) = listOf(
                    startNode(NodeParameters(providedName = ALICE_NAME, additionalCordapps = cordappsForPackages(javaClass.packageName))),
                    startNode(NodeParameters(providedName = BOB_NAME))
            ).transpose().getOrThrow()

            assertThat(bob.rpc.registeredFlows()).doesNotContain(TestInitiator::class.java.name)
            alice.rpc.startFlow(::TestInitiator, bob.nodeInfo.singleIdentity(), "Hello").returnValue.getOrThrow()
        }
    }

    @StartableByRPC
    @InitiatingFlow
    class TestInitiator(private val otherSide: Party, private val send: String) : FlowLogic<String>() {
        @Suspendable
        override fun call(): String = initiateFlow(otherSide).sendAndReceive<String>(send).unwrap { it }
    }

    @InitiatedBy(TestInitiator::class)
    class TestResponder(private val otherSide: FlowSession) : FlowLogic<Unit>() {
        override fun call() {
            val string = otherSide.receive<String>().unwrap { it }
            otherSide.send("$string$string")
        }
    }
}
