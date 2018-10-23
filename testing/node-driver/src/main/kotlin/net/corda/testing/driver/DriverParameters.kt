package net.corda.testing.driver

import net.corda.core.node.NetworkParameters
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.TestCordapp
import net.corda.testing.node.internal.getTimestampAsDirectoryName
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Builder for configuring a [driver].
 *
 * @property isDebug Indicates whether the spawned nodes should start in jdwt debug mode and have debug level logging.
 * @property driverDirectory The base directory node directories go into, defaults to "build/<timestamp>/". The node
 * directories themselves are "<baseDirectory>/<legalName>/", where legalName defaults to "<randomName>-<messagingPort>"
 * and may be specified in [DriverDSL.startNode].
 * @property portAllocation The port allocation strategy to use for the messaging and the web server addresses. Defaults
 * to incremental.
 * @property debugPortAllocation The port allocation strategy to use for jvm debugging. Defaults to incremental.
 * @property systemProperties A Map of extra system properties which will be given to each new node. Defaults to empty.
 * @property useTestClock If true the test clock will be used in Node.
 * @property startNodesInProcess Provides the default behaviour of whether new nodes should start inside this process or
 * not. Note that this may be overridden in [DriverDSL.startNode].
 * @property waitForAllNodesToFinish If true, the nodes will not shut down automatically after executing the code in the
 * driver DSL block. It will wait for them to be shut down externally instead.
 * @property notarySpecs The notaries advertised for this network. These nodes will be started automatically and will be
 * available from [DriverDSL.notaryHandles], and will be added automatically to the network parameters.
 * Defaults to a simple validating notary.
 * @property extraCordappPackagesToScan A [List] of additional cordapp packages to scan for any cordapp code, e.g.
 * contract verification code, flows and services. The calling package is automatically added. Cannot be used together
 * with [cordappsForAllNodes].
 * @property jmxPolicy Used to specify whether to expose JMX metrics via Jolokia HHTP/JSON.
 * @property networkParameters The network parameters to be used by all the nodes. [NetworkParameters.notaries] must be
 * empty as notaries are defined by [notarySpecs].
 * @property notaryCustomOverrides Extra settings that need to be passed to the notary.
 * @property initialiseSerialization Indicates whether to initialized the serialization subsystem.
 * @property inMemoryDB Whether to use in-memory H2 for new nodes rather then on-disk (the node starts quicker, however
 * the data is not persisted between node restarts). Has no effect if node is configured
 * in any way to use database other than H2.
 * @property cordappsForAllNodes [TestCordapp]s that will be added to each node started by the [DriverDSL]. Unlike
 * [extraCordappPackagesToScan], the calling package is not automically included as a CorDapp and must be specified
 * explictly. Cannot be used together with [extraCordappPackagesToScan].
 */
@Suppress("unused")
data class DriverParameters(
        val isDebug: Boolean = false,
        val driverDirectory: Path = Paths.get("build", getTimestampAsDirectoryName()),
        val portAllocation: PortAllocation = PortAllocation.Incremental(10000),
        val debugPortAllocation: PortAllocation = PortAllocation.Incremental(5005),
        val systemProperties: Map<String, String> = emptyMap(),
        val useTestClock: Boolean = false,
        val startNodesInProcess: Boolean = false,
        val waitForAllNodesToFinish: Boolean = false,
        val notarySpecs: List<NotarySpec> = listOf(NotarySpec(DUMMY_NOTARY_NAME)),
        val extraCordappPackagesToScan: List<String> = emptyList(),
        val jmxPolicy: JmxPolicy = JmxPolicy(),
        val networkParameters: NetworkParameters = testNetworkParameters(notaries = emptyList()),
        val notaryCustomOverrides: Map<String, Any?> = emptyMap(),
        val initialiseSerialization: Boolean = true,
        val inMemoryDB: Boolean = true,
        val cordappsForAllNodes: Collection<TestCordapp>? = null
    ) {
    constructor(
            isDebug: Boolean = false,
            driverDirectory: Path = Paths.get("build", getTimestampAsDirectoryName()),
            portAllocation: PortAllocation = PortAllocation.Incremental(10000),
            debugPortAllocation: PortAllocation = PortAllocation.Incremental(5005),
            systemProperties: Map<String, String> = emptyMap(),
            useTestClock: Boolean = false,
            startNodesInProcess: Boolean = false,
            waitForAllNodesToFinish: Boolean = false,
            notarySpecs: List<NotarySpec> = listOf(NotarySpec(DUMMY_NOTARY_NAME)),
            extraCordappPackagesToScan: List<String> = emptyList(),
            jmxPolicy: JmxPolicy = JmxPolicy(),
            networkParameters: NetworkParameters = testNetworkParameters(notaries = emptyList()),
            notaryCustomOverrides: Map<String, Any?> = emptyMap(),
            initialiseSerialization: Boolean = true,
            inMemoryDB: Boolean = true
    ) : this(
            isDebug,
            driverDirectory,
            portAllocation,
            debugPortAllocation,
            systemProperties,
            useTestClock,
            startNodesInProcess,
            waitForAllNodesToFinish,
            notarySpecs,
            extraCordappPackagesToScan,
            jmxPolicy,
            networkParameters,
            notaryCustomOverrides,
            initialiseSerialization,
            inMemoryDB,
            cordappsForAllNodes = null
    )

    constructor(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters
    ) : this(
            isDebug,
            driverDirectory,
            portAllocation,
            debugPortAllocation,
            systemProperties,
            useTestClock,
            startNodesInProcess,
            waitForAllNodesToFinish,
            notarySpecs,
            extraCordappPackagesToScan,
            jmxPolicy,
            networkParameters,
            emptyMap(),
            true,
            true,
            cordappsForAllNodes = null
    )

    constructor(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            cordappsForAllNodes: Collection<TestCordapp>? = null
    ) : this(
            isDebug,
            driverDirectory,
            portAllocation,
            debugPortAllocation,
            systemProperties,
            useTestClock,
            startNodesInProcess,
            waitForAllNodesToFinish,
            notarySpecs,
            extraCordappPackagesToScan,
            jmxPolicy,
            networkParameters,
            emptyMap(),
            true,
            true,
            cordappsForAllNodes
    )

    constructor(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            initialiseSerialization: Boolean,
            inMemoryDB: Boolean
    ) : this(
            isDebug,
            driverDirectory,
            portAllocation,
            debugPortAllocation,
            systemProperties,
            useTestClock,
            startNodesInProcess,
            waitForAllNodesToFinish,
            notarySpecs,
            extraCordappPackagesToScan,
            jmxPolicy,
            networkParameters,
            emptyMap(),
            initialiseSerialization,
            inMemoryDB,
            cordappsForAllNodes = null
    )

    constructor(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            initialiseSerialization: Boolean,
            inMemoryDB: Boolean,
            cordappsForAllNodes: Set<TestCordapp>? = null
    ) : this(
            isDebug,
            driverDirectory,
            portAllocation,
            debugPortAllocation,
            systemProperties,
            useTestClock,
            startNodesInProcess,
            waitForAllNodesToFinish,
            notarySpecs,
            extraCordappPackagesToScan,
            jmxPolicy,
            networkParameters,
            emptyMap(),
            initialiseSerialization,
            inMemoryDB,
            cordappsForAllNodes
    )

    init {
        require(cordappsForAllNodes == null || extraCordappPackagesToScan.isEmpty()) {
            "extraCordappPackagesToScan and cordappsForAllNodes cannot be specified together"
        }
    }

    fun withIsDebug(isDebug: Boolean): DriverParameters = copy(isDebug = isDebug)
    fun withDriverDirectory(driverDirectory: Path): DriverParameters = copy(driverDirectory = driverDirectory)
    fun withPortAllocation(portAllocation: PortAllocation): DriverParameters = copy(portAllocation = portAllocation)
    fun withDebugPortAllocation(debugPortAllocation: PortAllocation): DriverParameters = copy(debugPortAllocation = debugPortAllocation)
    fun withSystemProperties(systemProperties: Map<String, String>): DriverParameters = copy(systemProperties = systemProperties)
    fun withUseTestClock(useTestClock: Boolean): DriverParameters = copy(useTestClock = useTestClock)
    fun withInitialiseSerialization(initialiseSerialization: Boolean): DriverParameters = copy(initialiseSerialization = initialiseSerialization)
    fun withStartNodesInProcess(startNodesInProcess: Boolean): DriverParameters = copy(startNodesInProcess = startNodesInProcess)
    fun withWaitForAllNodesToFinish(waitForAllNodesToFinish: Boolean): DriverParameters = copy(waitForAllNodesToFinish = waitForAllNodesToFinish)
    fun withNotarySpecs(notarySpecs: List<NotarySpec>): DriverParameters = copy(notarySpecs = notarySpecs)
    fun withExtraCordappPackagesToScan(extraCordappPackagesToScan: List<String>): DriverParameters = copy(extraCordappPackagesToScan = extraCordappPackagesToScan)
    fun withJmxPolicy(jmxPolicy: JmxPolicy): DriverParameters = copy(jmxPolicy = jmxPolicy)
    fun withNetworkParameters(networkParameters: NetworkParameters): DriverParameters = copy(networkParameters = networkParameters)
    fun withNotaryCustomOverrides(notaryCustomOverrides: Map<String, Any?>): DriverParameters = copy(notaryCustomOverrides = notaryCustomOverrides)
    fun withInMemoryDB(inMemoryDB: Boolean): DriverParameters = copy(inMemoryDB = inMemoryDB)
    fun withCordappsForAllNodes(cordappsForAllNodes: Set<TestCordapp>?): DriverParameters = copy(cordappsForAllNodes = cordappsForAllNodes)

    fun copy(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters
    ) = this.copy(
            isDebug = isDebug,
            driverDirectory = driverDirectory,
            portAllocation = portAllocation,
            debugPortAllocation = debugPortAllocation,
            systemProperties = systemProperties,
            useTestClock = useTestClock,
            startNodesInProcess = startNodesInProcess,
            waitForAllNodesToFinish = waitForAllNodesToFinish,
            notarySpecs = notarySpecs,
            extraCordappPackagesToScan = extraCordappPackagesToScan,
            jmxPolicy = jmxPolicy,
            networkParameters = networkParameters,
            notaryCustomOverrides = emptyMap(),
            initialiseSerialization = true
    )

    fun copy(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            initialiseSerialization: Boolean
    ) = this.copy(
            isDebug = isDebug,
            driverDirectory = driverDirectory,
            portAllocation = portAllocation,
            debugPortAllocation = debugPortAllocation,
            systemProperties = systemProperties,
            useTestClock = useTestClock,
            startNodesInProcess = startNodesInProcess,
            waitForAllNodesToFinish = waitForAllNodesToFinish,
            notarySpecs = notarySpecs,
            extraCordappPackagesToScan = extraCordappPackagesToScan,
            jmxPolicy = jmxPolicy,
            networkParameters = networkParameters,
            notaryCustomOverrides = emptyMap(),
            initialiseSerialization = initialiseSerialization
    )

    fun copy(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            cordappsForAllNodes: Set<TestCordapp>?
    ) = this.copy(
            isDebug = isDebug,
            driverDirectory = driverDirectory,
            portAllocation = portAllocation,
            debugPortAllocation = debugPortAllocation,
            systemProperties = systemProperties,
            useTestClock = useTestClock,
            startNodesInProcess = startNodesInProcess,
            waitForAllNodesToFinish = waitForAllNodesToFinish,
            notarySpecs = notarySpecs,
            extraCordappPackagesToScan = extraCordappPackagesToScan,
            jmxPolicy = jmxPolicy,
            networkParameters = networkParameters,
            notaryCustomOverrides = emptyMap(),
            initialiseSerialization = true,
            cordappsForAllNodes = cordappsForAllNodes
    )

    fun copy(
            isDebug: Boolean,
            driverDirectory: Path,
            portAllocation: PortAllocation,
            debugPortAllocation: PortAllocation,
            systemProperties: Map<String, String>,
            useTestClock: Boolean,
            startNodesInProcess: Boolean,
            waitForAllNodesToFinish: Boolean,
            notarySpecs: List<NotarySpec>,
            extraCordappPackagesToScan: List<String>,
            jmxPolicy: JmxPolicy,
            networkParameters: NetworkParameters,
            initialiseSerialization: Boolean,
            cordappsForAllNodes: Set<TestCordapp>?
    ) = this.copy(
            isDebug = isDebug,
            driverDirectory = driverDirectory,
            portAllocation = portAllocation,
            debugPortAllocation = debugPortAllocation,
            systemProperties = systemProperties,
            useTestClock = useTestClock,
            startNodesInProcess = startNodesInProcess,
            waitForAllNodesToFinish = waitForAllNodesToFinish,
            notarySpecs = notarySpecs,
            extraCordappPackagesToScan = extraCordappPackagesToScan,
            jmxPolicy = jmxPolicy,
            networkParameters = networkParameters,
            notaryCustomOverrides = emptyMap(),
            initialiseSerialization = initialiseSerialization,
            cordappsForAllNodes = cordappsForAllNodes
    )
}
