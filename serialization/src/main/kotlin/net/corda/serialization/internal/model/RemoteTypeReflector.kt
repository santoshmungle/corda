package net.corda.serialization.internal.model

/**
 * Represents the reflection of some [RemoteTypeInformation] by some [LocalTypeInformation], which we use to make
 * decisions about evolution.
 */
data class ReflectedTypeInformation(
        val remoteTypeInformation: RemoteTypeInformation,
        val localTypeInformation: LocalTypeInformation,
        val localTypeDescriptor: TypeDescriptor)

/**
 * Given [RemoteTypeInformation], a [RemoteTypeReflector] will find (or, if necessary, create) a local type reflecting
 * the remote type.
 */
 interface RemoteTypeReflector {
     fun reflect(remoteTypeInformation: RemoteTypeInformation): ReflectedTypeInformation
 }

/**
 * A [TypeLoadingRemoteTypeReflector] uses a [TypeLoader] to load local types reflecting remote types,
 * a [LocalTypeModel] to obtain [LocalTypeInformation] for these local types, and a [TypeModellingFingerPrinter] to
 * create type descriptors for local types which can be compared to the type descriptors of their remote counterparts.
 *
 * If the type descriptors don't match, we can compare the two sets of type information and decide whether evolution is
 * necessary.
 *
 * @param typeLoader The [TypeLoader] to use to load local types reflecting remote types.
 * @param localTypeModel The [LocalTypeModel] to use to obtain [LocalTypeInformation] for the local [Type]s so obtained.
 */
class TypeLoadingRemoteTypeReflector(
        private val typeLoader: TypeLoader,
        private val localTypeModel: LocalTypeModel,
        private val fingerPrinter: FingerPrinter): RemoteTypeReflector {

    // Reflected type information is cached by remote type information.
    private val cache = DefaultCacheProvider.createCache<RemoteTypeInformation, ReflectedTypeInformation>()

    override fun reflect(remoteTypeInformation: RemoteTypeInformation): ReflectedTypeInformation =
        cache.computeIfAbsent(remoteTypeInformation) { _ ->
            val type = typeLoader.load(remoteTypeInformation)
            val localTypeInformation = localTypeModel.inspect(type)
            val fingerprint = fingerPrinter.fingerprint(localTypeInformation)

            ReflectedTypeInformation(
                    remoteTypeInformation,
                    localTypeInformation,
                    fingerprint)
        }
}