package xyz.dussim.viessmann.api.features

interface DeviceFeatureFactory<F : ViessmannFeature.Device> {
    val featureName: String

    operator fun invoke(feature: ViessmannFeature.Device): F

    operator fun invoke(feature: ViessmannFeature): F = invoke(feature.asDevice())
}

interface GatewayFeatureFactory<F : ViessmannFeature.Gateway> {
    val featureName: String

    operator fun invoke(feature: ViessmannFeature.Gateway): F

    operator fun invoke(feature: ViessmannFeature): F = invoke(feature.asGateway())
}

class ViessmannFeatureResolver(
    features: List<ViessmannFeature>,
) {
    private val features = features.associateBy { it.feature }

    operator fun <F : ViessmannFeature.Gateway> get(
        factory: GatewayFeatureFactory<F>,
        index: Int = 0,
    ): F =
        features
            .getValue(factory.featureName.replace("{}", index.toString()))
            .let(factory::invoke)

    operator fun <F : ViessmannFeature.Device> get(
        factory: DeviceFeatureFactory<F>,
        index: Int = 0,
    ): F =
        features
            .getValue(factory.featureName.replace("{}", index.toString()))
            .let(factory::invoke)
}
