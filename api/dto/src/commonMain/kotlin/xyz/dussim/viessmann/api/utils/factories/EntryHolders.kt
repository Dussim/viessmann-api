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
import xyz.dussim.viessmann.api.utils.EntryHolder

private fun <T : ViessmannEnum> entryHolderOf(vararg values: T) =
    object : EntryHolder<T> {
        override val entries = values.toSet()
    }

private fun <T : ViessmannEnum> entryHolderOf(vararg values: EntryHolder<out T>) =
    object : EntryHolder<T> {
        override val entries = values.flatMap { it.entries }.toSet()
    }

internal val AccessLevelEntryHolder: EntryHolder<AccessLevel.Strict> =
    entryHolderOf(
        AccessLevel.Owner,
        AccessLevel.FamilyMember,
        AccessLevel.Maintainer,
        AccessLevel.Support,
        AccessLevel.Installer,
        AccessLevel.ServiceContractor,
        AccessLevel.Operator,
        AccessLevel.BuildingManager,
        AccessLevel.CommercialOwner,
        AccessLevel.Partner,
        AccessLevel.Consumer,
    )

internal val AggregatedStatusEntryHolder: EntryHolder<AggregatedStatus.Strict> =
    entryHolderOf(
        AggregatedStatus.Error,
        AggregatedStatus.Offline,
        AggregatedStatus.Maintenance,
        AggregatedStatus.WorksProperly,
        AggregatedStatus.RemoteDiagnosticSession,
        AggregatedStatus.NbIotConnected,
    )

internal val GenderEntryHolder =
    entryHolderOf(
        Gender.Male,
        Gender.Female,
        Gender.Other,
    )

internal val HeatingTypeEntryHolder =
    entryHolderOf(
        HeatingType.None,
        HeatingType.FloorHeating,
        HeatingType.Radiators,
        HeatingType.Both,
        HeatingType.Undefined,
    )

internal val GatewayStateEntryHolder =
    entryHolderOf(
        GatewayState.Produced,
        GatewayState.Registered,
    )

internal val GatewayTypeEntryHolder =
    entryHolderOf(
        GatewayType.Vitoconnect.OPTO2,
        GatewayType.Vitoconnect.OPTO3,
        GatewayType.Vitoconnect.Optolink,
        GatewayType.Vitoconnect.OpenTherm,
        GatewayType.Tcu.V101,
        GatewayType.Tcu.V102,
        GatewayType.Tcu.V201,
        GatewayType.Tcu.V301,
        GatewayType.Sa.V171,
        GatewayType.Sa.V171s,
        GatewayType.Sa.V180,
        GatewayType.Sa.V180NBIoT,
        GatewayType.Sa.V1800019WiFi,
        GatewayType.Sa.V180Lan,
        GatewayType.WiFi.SA0019,
        GatewayType.WiFi.SA0041,
        GatewayType.Thermostat.Nest,
        GatewayType.Thermostat.Smart,
        GatewayType.VitocomLan4,
        GatewayType.Lancard,
        GatewayType.OneBaseEvolveBox,
        GatewayType.VitocontrolAPro,
    )

internal val InstallationTypeEntryHolder =
    entryHolderOf(
        InstallationType.Commercial,
        InstallationType.Residential,
    )

internal val InvitationStatusEntryHolder =
    entryHolderOf(
        InvitationStatus.Accepted,
        InvitationStatus.Pending,
        InvitationStatus.Rejected,
        InvitationStatus.Canceled,
        InvitationStatus.Expired,
        InvitationStatus.Terminated,
    )

internal val OwnershipTypeEntryHolder =
    entryHolderOf(
        OwnershipType.ResidentialEndUser,
        OwnershipType.PreCommissioning,
        OwnershipType.Oma,
        OwnershipType.CommercialV1,
        OwnershipType.CommercialV2,
        OwnershipType.None,
    )

internal val TargetRealmEntryHolder =
    entryHolderOf(
        TargetRealm.Dc,
        TargetRealm.Genesis,
    )

internal val NetworkStatusEntryHolder =
    entryHolderOf(
        NetworkStatus.Offline,
        NetworkStatus.Online,
    )

internal val TokenTypeEntryHolder =
    entryHolderOf(
        TokenType.Invited,
        TokenType.Requested,
    )

internal val SerialEditorEntryHolder =
    entryHolderOf(
        SerialEditor.User,
        SerialEditor.DeviceCommunication,
        SerialEditor.Supporter,
    )

internal val ViessmannEnumEntryHolder =
    entryHolderOf(
        AccessLevelEntryHolder,
        AggregatedStatusEntryHolder,
        GenderEntryHolder,
        HeatingTypeEntryHolder,
        GatewayStateEntryHolder,
        GatewayTypeEntryHolder,
        InstallationTypeEntryHolder,
        InvitationStatusEntryHolder,
        OwnershipTypeEntryHolder,
        TargetRealmEntryHolder,
        NetworkStatusEntryHolder,
        TokenTypeEntryHolder,
        SerialEditorEntryHolder,
    )
