@file:JvmName("Driver")

package net.corda.testing.driver

import net.corda.core.DoNotImplement
import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.NodeInfo
import net.corda.core.node.ServiceHub
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.Node
import net.corda.testing.driver.PortAllocation.Incremental
import net.corda.testing.driver.internal.internalServices
import net.corda.testing.node.TestCordapp
import net.corda.testing.node.User
import net.corda.testing.node.internal.*
import rx.Observable
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * Object ecapsulating a notary started automatically by the driver.
 */
data class NotaryHandle(val identity: Party, val validating: Boolean, val nodeHandles: CordaFuture<List<NodeHandle>>)

/**
 * A base interface which represents a node as part of the [driver] dsl, extended by [InProcess] and [OutOfProcess]
 */
@DoNotImplement
interface NodeHandle : AutoCloseable {
    /** Get the [NodeInfo] for this node */
    val nodeInfo: NodeInfo
    /**
     * Interface to the node's RPC system. The first RPC user will be used to login if are any, otherwise a default one
     * will be added and that will be used.
     */
    val rpc: CordaRPCOps
    /** Get the p2p address for this node **/
    val p2pAddress: NetworkHostAndPort
    /** Get the rpc address for this node **/
    val rpcAddress: NetworkHostAndPort
    /** Get the rpc admin address for this node **/
    val rpcAdminAddress: NetworkHostAndPort
    /** Get a [List] of [User]'s for this node **/
    val rpcUsers: List<User>
    /** The location of the node's base directory **/
    val baseDirectory: Path

    /**
     * Stops the referenced node.
     */
    fun stop()
}


/** Interface which represents an out of process node and exposes its process handle. **/
@DoNotImplement
interface OutOfProcess : NodeHandle {
    /** The process in which this node is running **/
    val process: Process
}

/** Interface which represents an in process node and exposes available services. **/
@DoNotImplement
interface InProcess : NodeHandle {
    /** Services which are available to this node **/
    val services: ServiceHub

    /**
     * Register a flow that is initiated by another flow
     */
    fun <T : FlowLogic<*>> registerInitiatedFlow(initiatedFlowClass: Class<T>): Observable<T>

    /**
     * Starts an already constructed flow. Note that you must be on the server thread to call this method.
     * @param context indicates who started the flow, see: [InvocationContext].
     */
    fun <T> startFlow(logic: FlowLogic<T>): CordaFuture<T> = internalServices.startFlow(logic, internalServices.newContext()).getOrThrow().resultFuture
}

/**
 * Class which represents a handle to a webserver process and its [NetworkHostAndPort] for testing purposes.
 *
 * @property listenAddress The [NetworkHostAndPort] for communicating with this webserver.
 * @property process The [Process] in which the websever is running
 * */
@Deprecated("The webserver is for testing purposes only and will be removed soon")
data class WebserverHandle(
        val listenAddress: NetworkHostAndPort,
        val process: Process
)

/**
 * An abstract helper class which is used within the driver to allocate unused ports for testing. Use either
 * the [Incremental] or [RandomFree] concrete implementations.
 */
@DoNotImplement
abstract class PortAllocation {
    /** Get the next available port **/
    abstract fun nextPort(): Int

    /** Get the next available port via [nextPort] and then return a [NetworkHostAndPort] **/
    fun nextHostAndPort() = NetworkHostAndPort("localhost", nextPort())

    /**
     * An implementation of [PortAllocation] which allocates ports sequentially
     */
    class Incremental(startingPort: Int) : PortAllocation() {
        /** The backing [AtomicInteger] used to keep track of the currently allocated port */
        val portCounter = AtomicInteger(startingPort)

        override fun nextPort() = portCounter.andIncrement
    }
}

/**
 * Helper builder for configuring a [Node] from Java.
 *
 * @property providedName Optional name of the node, which will be its legal name in [Party]. Defaults to something
 *     random. Note that this must be unique as the driver uses it as a primary key!
 * @property rpcUsers List of users who are authorised to use the RPC system. Defaults to a single user with
 *     all permissions.
 * @property verifierType The type of transaction verifier to use. See: [VerifierType]
 * @property customOverrides A map of custom node configuration overrides.
 * @property startInSameProcess Determines if the node should be started inside the same process the Driver is running
 *     in. If null the Driver-level value will be used.
 * @property maximumHeapSize The maximum JVM heap size to use for the node.
 * @property logLevel Logging level threshold.
 * @property additionalCordapps Additional [TestCordapp]s that this node will have available, in addition to the ones common to all nodes managed by the [DriverDSL].
 * @property regenerateCordappsOnStart Whether existing [TestCordapp]s unique to this node will be re-generated on start. Useful when stopping and restarting the same node.
 */
@Suppress("unused")
data class NodeParameters(
        val providedName: CordaX500Name? = null,
        val rpcUsers: List<User> = emptyList(),
        val verifierType: VerifierType = VerifierType.InMemory,
        val customOverrides: Map<String, Any?> = emptyMap(),
        val startInSameProcess: Boolean? = null,
        val maximumHeapSize: String = "512m",
        val logLevel: String? = null,
        val additionalCordapps: Collection<TestCordapp> = emptySet(),
        val regenerateCordappsOnStart: Boolean = false,
        val flowOverrides: Map<Class<out FlowLogic<*>>, Class<out FlowLogic<*>>> = emptyMap()
) {
    /**
     * Helper builder for configuring a [Node] from Java.
     *
     * @param providedName Optional name of the node, which will be its legal name in [Party]. Defaults to something
     *     random. Note that this must be unique as the driver uses it as a primary key!
     * @param rpcUsers List of users who are authorised to use the RPC system. Defaults to a single user with
     *     all permissions.
     * @param verifierType The type of transaction verifier to use. See: [VerifierType]
     * @param customOverrides A map of custom node configuration overrides.
     * @param startInSameProcess Determines if the node should be started inside the same process the Driver is running
     *     in. If null the Driver-level value will be used.
     * @param maximumHeapSize The maximum JVM heap size to use for the node.
     * @param logLevel Logging level threshold.
     */
    constructor(
            providedName: CordaX500Name?,
            rpcUsers: List<User>,
            verifierType: VerifierType,
            customOverrides: Map<String, Any?>,
            startInSameProcess: Boolean?,
            maximumHeapSize: String,
            logLevel: String? = null
    ) : this(
            providedName,
            rpcUsers,
            verifierType,
            customOverrides,
            startInSameProcess,
            maximumHeapSize,
            logLevel,
            additionalCordapps = emptySet(),
            regenerateCordappsOnStart = false
    )

    /**
     * Helper builder for configuring a [Node] from Java.
     *
     * @param providedName Optional name of the node, which will be its legal name in [Party]. Defaults to something
     *     random. Note that this must be unique as the driver uses it as a primary key!
     * @param rpcUsers List of users who are authorised to use the RPC system. Defaults to a single user with
     *     all permissions.
     * @param verifierType The type of transaction verifier to use. See: [VerifierType]
     * @param customOverrides A map of custom node configuration overrides.
     * @param startInSameProcess Determines if the node should be started inside the same process the Driver is running
     *     in. If null the Driver-level value will be used.
     * @param maximumHeapSize The maximum JVM heap size to use for the node.
     */
    constructor(
            providedName: CordaX500Name?,
            rpcUsers: List<User>,
            verifierType: VerifierType,
            customOverrides: Map<String, Any?>,
            startInSameProcess: Boolean?,
            maximumHeapSize: String
    ) : this(
            providedName,
            rpcUsers,
            verifierType,
            customOverrides,
            startInSameProcess,
            maximumHeapSize,
            null,
            additionalCordapps = emptySet(),
            regenerateCordappsOnStart = false)

    /**
     * Helper builder for configuring a [Node] from Java.
     *
     * @param providedName Optional name of the node, which will be its legal name in [Party]. Defaults to something
     *     random. Note that this must be unique as the driver uses it as a primary key!
     * @param rpcUsers List of users who are authorised to use the RPC system. Defaults to a single user with
     *     all permissions.
     * @param verifierType The type of transaction verifier to use. See: [VerifierType]
     * @param customOverrides A map of custom node configuration overrides.
     * @param startInSameProcess Determines if the node should be started inside the same process the Driver is running
     *     in. If null the Driver-level value will be used.
     * @param maximumHeapSize The maximum JVM heap size to use for the node.
     * @param additionalCordapps Additional [TestCordapp]s that this node will have available, in addition to the ones common to all nodes managed by the [DriverDSL].
     * @param regenerateCordappsOnStart Whether existing [TestCordapp]s unique to this node will be re-generated on start. Useful when stopping and restarting the same node.
     */
    constructor(
            providedName: CordaX500Name?,
            rpcUsers: List<User>,
            verifierType: VerifierType,
            customOverrides: Map<String, Any?>,
            startInSameProcess: Boolean?,
            maximumHeapSize: String,
            additionalCordapps: Set<TestCordapp> = emptySet(),
            regenerateCordappsOnStart: Boolean = false
    ) : this(
            providedName,
            rpcUsers,
            verifierType,
            customOverrides,
            startInSameProcess,
            maximumHeapSize,
            null,
            additionalCordapps,
            regenerateCordappsOnStart)

    fun copy(
            providedName: CordaX500Name?,
            rpcUsers: List<User>,
            verifierType: VerifierType,
            customOverrides: Map<String, Any?>,
            startInSameProcess: Boolean?,
            maximumHeapSize: String
    ) = this.copy(
            providedName,
            rpcUsers,
            verifierType,
            customOverrides,
            startInSameProcess,
            maximumHeapSize,
            null)

    fun copy(
            providedName: CordaX500Name?,
            rpcUsers: List<User>,
            verifierType: VerifierType,
            customOverrides: Map<String, Any?>,
            startInSameProcess: Boolean?,
            maximumHeapSize: String,
            logLevel: String?
    ) = this.copy(
            providedName,
            rpcUsers,
            verifierType,
            customOverrides,
            startInSameProcess,
            maximumHeapSize,
            logLevel,
            additionalCordapps = additionalCordapps,
            regenerateCordappsOnStart = regenerateCordappsOnStart)

    fun withProvidedName(providedName: CordaX500Name?): NodeParameters = copy(providedName = providedName)
    fun withRpcUsers(rpcUsers: List<User>): NodeParameters = copy(rpcUsers = rpcUsers)
    fun withVerifierType(verifierType: VerifierType): NodeParameters = copy(verifierType = verifierType)
    fun withCustomOverrides(customOverrides: Map<String, Any?>): NodeParameters = copy(customOverrides = customOverrides)
    fun withStartInSameProcess(startInSameProcess: Boolean?): NodeParameters = copy(startInSameProcess = startInSameProcess)
    fun withMaximumHeapSize(maximumHeapSize: String): NodeParameters = copy(maximumHeapSize = maximumHeapSize)
    fun withLogLevel(logLevel: String?): NodeParameters = copy(logLevel = logLevel)
    fun withAdditionalCordapps(additionalCordapps: Set<TestCordapp>): NodeParameters = copy(additionalCordapps = additionalCordapps)
    fun withDeleteExistingCordappsDirectory(regenerateCordappsOnStart: Boolean): NodeParameters = copy(regenerateCordappsOnStart = regenerateCordappsOnStart)
}

/**
 * A class containing configuration information for Jolokia JMX, to be used when creating a node via the [driver]
 *
 * @property startJmxHttpServer Indicates whether the spawned nodes should start with a Jolokia JMX agent to enable remote
 * JMX monitoring using HTTP/JSON
 * @property jmxHttpServerPortAllocation The port allocation strategy to use for remote Jolokia/JMX monitoring over HTTP.
 * Defaults to incremental.
 */
data class JmxPolicy(val startJmxHttpServer: Boolean = false,
                     val jmxHttpServerPortAllocation: PortAllocation? =
                             if (startJmxHttpServer) PortAllocation.Incremental(7005) else null)

/**
 * [driver] allows one to start up nodes like this:
 *   driver {
 *     val noService = startNode(providedName = DUMMY_BANK_A.name)
 *     val notary = startNode(providedName = DUMMY_NOTARY.name)
 *
 *     (...)
 *   }
 *
 * Note that [DriverDSL.startNode] does not wait for the node to start up synchronously, but rather returns a [CordaFuture]
 * of the [NodeInfo] that may be waited on, which completes when the new node registered with the network map service or
 * loaded node data from database.
 *
 * @param defaultParameters The default parameters for the driver. Allows the driver to be configured in builder style
 *   when called from Java code.
 * @property dsl The dsl itself.
 * @return The value returned in the [dsl] closure.
 */
fun <A> driver(defaultParameters: DriverParameters = DriverParameters(), dsl: DriverDSL.() -> A): A {
    return genericDriver(
            driverDsl = DriverDSLImpl(
                    portAllocation = defaultParameters.portAllocation,
                    debugPortAllocation = defaultParameters.debugPortAllocation,
                    systemProperties = defaultParameters.systemProperties,
                    driverDirectory = defaultParameters.driverDirectory.toAbsolutePath(),
                    useTestClock = defaultParameters.useTestClock,
                    isDebug = defaultParameters.isDebug,
                    startNodesInProcess = defaultParameters.startNodesInProcess,
                    waitForAllNodesToFinish = defaultParameters.waitForAllNodesToFinish,
                    notarySpecs = defaultParameters.notarySpecs,
                    jmxPolicy = defaultParameters.jmxPolicy,
                    compatibilityZone = null,
                    networkParameters = defaultParameters.networkParameters,
                    notaryCustomOverrides = defaultParameters.notaryCustomOverrides,
                    inMemoryDB = defaultParameters.inMemoryDB,
                    cordappsForAllNodes = defaultParameters.cordappsForAllNodes()
            ),
            coerce = { it },
            dsl = dsl,
            initialiseSerialization = defaultParameters.initialiseSerialization
    )
}
