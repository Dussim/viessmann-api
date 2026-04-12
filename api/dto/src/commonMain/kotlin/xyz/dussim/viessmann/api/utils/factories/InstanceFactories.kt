package xyz.dussim.viessmann.api.utils.factories

import xyz.dussim.viessmann.api.enums.AccessLevel
import xyz.dussim.viessmann.api.enums.AggregatedStatus
import xyz.dussim.viessmann.api.enums.GatewayState
import xyz.dussim.viessmann.api.enums.GatewayType
import xyz.dussim.viessmann.api.enums.Gender
import xyz.dussim.viessmann.api.enums.HeatingType
import xyz.dussim.viessmann.api.enums.InstallationType
import xyz.dussim.viessmann.api.enums.InvitationStatus
import xyz.dussim.viessmann.api.enums.NetworkStatus
import xyz.dussim.viessmann.api.enums.OwnershipType
import xyz.dussim.viessmann.api.enums.SerialEditor
import xyz.dussim.viessmann.api.enums.TargetRealm
import xyz.dussim.viessmann.api.enums.TokenType
import xyz.dussim.viessmann.api.enums.ViessmannEnum
import xyz.dussim.viessmann.api.utils.InstanceFactory

internal val AccessLevelInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Owner" -> AccessLevel.Owner
            "FamilyMember" -> AccessLevel.FamilyMember
            "Maintainer" -> AccessLevel.Maintainer
            "Support" -> AccessLevel.Support
            "Installer" -> AccessLevel.Installer
            "ServiceContractor" -> AccessLevel.ServiceContractor
            "Operator" -> AccessLevel.Operator
            "BuildingManager" -> AccessLevel.BuildingManager
            "CommercialOwner" -> AccessLevel.CommercialOwner
            "Partner" -> AccessLevel.Partner
            "Consumer" -> AccessLevel.Consumer
            else -> AccessLevel.Unknown(value)
        }
    }

internal val AggregatedStatusInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Error" -> AggregatedStatus.Error
            "Offline" -> AggregatedStatus.Offline
            "Maintenance" -> AggregatedStatus.Maintenance
            "WorksProperly" -> AggregatedStatus.WorksProperly
            "RemoteDiagnosticSession" -> AggregatedStatus.RemoteDiagnosticSession
            "NbIotConnected" -> AggregatedStatus.NbIotConnected
            else -> AggregatedStatus.Unknown(value)
        }
    }

internal val GatewayStateInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Produced" -> GatewayState.Produced
            "Registered" -> GatewayState.Registered
            else -> GatewayState.Unknown(value)
        }
    }

internal val GatewayTypeInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "VitoconnectOPTO2" -> GatewayType.Vitoconnect.OPTO2
            "VitoconnectOPTO3" -> GatewayType.Vitoconnect.OPTO3
            "VitoconnectOptolink" -> GatewayType.Vitoconnect.Optolink
            "VitoconnectOpenTherm" -> GatewayType.Vitoconnect.OpenTherm
            "TCU101" -> GatewayType.Tcu.V101
            "TCU102" -> GatewayType.Tcu.V102
            "TCU201" -> GatewayType.Tcu.V201
            "TCU301" -> GatewayType.Tcu.V301
            "SA171" -> GatewayType.Sa.V171
            "SA171s" -> GatewayType.Sa.V171s
            "SA180" -> GatewayType.Sa.V180
            "SA180NBIoT" -> GatewayType.Sa.V180NBIoT
            "SA1800019WiFi" -> GatewayType.Sa.V1800019WiFi
            "SA180Lan" -> GatewayType.Sa.V180Lan
            "WiFi_SA0019" -> GatewayType.WiFi.SA0019
            "WiFi_SA0041" -> GatewayType.WiFi.SA0041
            "NestThermostat" -> GatewayType.Thermostat.Nest
            "SmartThermostat" -> GatewayType.Thermostat.Smart
            "VitocomLan4" -> GatewayType.VitocomLan4
            "Lancard" -> GatewayType.Lancard
            "One_Base_Evolve_Box" -> GatewayType.OneBaseEvolveBox
            "Vitocontrol_A_PRO" -> GatewayType.VitocontrolAPro
            "TCU10x_Vitodens_SE_S1" -> GatewayType.Tcu10xVitodensSeS1
            "TCU10x_ViAir_VitoconnectV" -> GatewayType.Tcu10xViAirVitoconnectV
            "TCU10x_Vitopure_EU" -> GatewayType.Tcu10xVitopureEu
            "TCU10x_Vitocal_262_060" -> GatewayType.Tcu10xVitocal262060
            "TCU10x_Vitoset" -> GatewayType.Tcu10xVitoset
            "SCU_1000" -> GatewayType.Scu1000
            "TCU301_Vitocal" -> GatewayType.Tcu301Vitocal
            "TCU301_Vitocharge" -> GatewayType.Tcu301Vitocharge
            "TCU301_Vitodens" -> GatewayType.Tcu301Vitodens
            "TCU301_Vitocrossal" -> GatewayType.Tcu301Vitocrossal
            "TCU10x_DevBoards" -> GatewayType.Tcu10xDevBoards
            "TCU301_DevBoards" -> GatewayType.Tcu301DevBoards
            "Infinity" -> GatewayType.Infinity
            "TCU10x_Vitodens_SE_NA" -> GatewayType.Tcu10xVitodensSeNa
            "TCU10x_RAC" -> GatewayType.Tcu10xRac
            else -> GatewayType.Unknown(value)
        }
    }

internal val GenderInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "MALE" -> Gender.Male
            "FEMALE" -> Gender.Female
            "OTHER" -> Gender.Other
            else -> Gender.Unknown(value)
        }
    }

internal val HeatingTypeInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "None" -> HeatingType.None
            "FloorHeating" -> HeatingType.FloorHeating
            "Radiators" -> HeatingType.Radiators
            "Both" -> HeatingType.Both
            "Undefined" -> HeatingType.Undefined
            else -> HeatingType.Unknown(value)
        }
    }

internal val InstallationTypeInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Residential" -> InstallationType.Residential
            "Commercial" -> InstallationType.Commercial
            else -> InstallationType.Unknown(value)
        }
    }

internal val InvitationStatusInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Pending" -> InvitationStatus.Pending
            "Accepted" -> InvitationStatus.Accepted
            "Rejected" -> InvitationStatus.Rejected
            "Canceled" -> InvitationStatus.Canceled
            "Expired" -> InvitationStatus.Expired
            "Terminated" -> InvitationStatus.Terminated
            else -> InvitationStatus.Unknown(value)
        }
    }
internal val OwnershipTypeInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "ResidentialEndUser" -> OwnershipType.ResidentialEndUser
            "PreCommissioning" -> OwnershipType.PreCommissioning
            "Oma" -> OwnershipType.Oma
            "CommercialV1" -> OwnershipType.CommercialV1
            "CommercialV2" -> OwnershipType.CommercialV2
            "None" -> OwnershipType.None
            else -> OwnershipType.Unknown(value)
        }
    }

internal val TargetRealmInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "DC" -> TargetRealm.Dc
            "Genesis" -> TargetRealm.Genesis
            else -> TargetRealm.Unknown(value)
        }
    }

internal val NetworkStatusInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Offline" -> NetworkStatus.Offline
            "Online" -> NetworkStatus.Online
            else -> NetworkStatus.Unknown(value)
        }
    }

internal val TokenTypeInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "Invited" -> TokenType.Invited
            "Requested" -> TokenType.Requested
            else -> TokenType.Unknown(value)
        }
    }

internal val SerialEditorInstanceFactory =
    InstanceFactory { value ->
        when (value) {
            "User" -> SerialEditor.User
            "DeviceCommunication" -> SerialEditor.DeviceCommunication
            "Supporter" -> SerialEditor.Supporter
            else -> SerialEditor.Unknown(value)
        }
    }

internal val AllInstancesFactories =
    listOf(
        AccessLevelInstanceFactory,
        AggregatedStatusInstanceFactory,
        GatewayStateInstanceFactory,
        GatewayTypeInstanceFactory,
        GenderInstanceFactory,
        HeatingTypeInstanceFactory,
        InstallationTypeInstanceFactory,
        InvitationStatusInstanceFactory,
        OwnershipTypeInstanceFactory,
        TargetRealmInstanceFactory,
        NetworkStatusInstanceFactory,
        TokenTypeInstanceFactory,
        SerialEditorInstanceFactory,
    )

internal val ViessmannEnumInstanceFactory =
    InstanceFactory { value ->
        AllInstancesFactories.firstNotNullOfOrNull { it.valueOfOrNull(value) } ?: ViessmannEnum.Unknown(value)
    }
