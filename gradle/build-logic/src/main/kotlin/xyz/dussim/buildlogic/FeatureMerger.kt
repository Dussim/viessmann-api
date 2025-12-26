package xyz.dussim.buildlogic

import xyz.dussim.viessmann.feature.api.Command
import xyz.dussim.viessmann.feature.api.DeviceFeature
import xyz.dussim.viessmann.feature.api.EfficientStringKeyMap
import xyz.dussim.viessmann.feature.api.ListEmptyValue
import xyz.dussim.viessmann.feature.api.Property

object FeatureMerger {
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun mergeAll(features: List<DeviceFeature>): List<DeviceFeature> =
        features
            .groupBy { feature ->
                collapseFeatureName(feature.feature)
            }.map { (collapsedName, featuresToMerge) ->
                mergeFeatures(featuresToMerge).copy(feature = collapsedName)
            }

    private fun collapseFeatureName(name: String): String =
        name.split('.').joinToString(".") { segment ->
            if (segment.isNotEmpty() && segment.all { it.isDigit() }) "{}" else segment
        }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun mergeFeatures(features: List<DeviceFeature>): DeviceFeature {
        val first = features.first()
        if (features.size == 1) return first

        val allProperties = mutableMapOf<String, Property>()
        val allCommands = mutableMapOf<String, Command>()

        features.forEach { feature ->
            feature.properties.forEach { (name, property) ->
                val existing = allProperties[name]
                if (existing == null || (existing.value is ListEmptyValue && property.value !is ListEmptyValue)) {
                    allProperties[name] = property
                }
            }
            feature.commands.forEach { (name, command) ->
                val existing = allCommands[name]
                if (existing == null || (existing.params.isEmpty() && command.params.isNotEmpty())) {
                    allCommands[name] = command
                }
            }
        }

        return first.copy(
            properties = EfficientStringKeyMap.createFrom(allProperties),
            commands = EfficientStringKeyMap.createFrom(allCommands),
        )
    }
}
