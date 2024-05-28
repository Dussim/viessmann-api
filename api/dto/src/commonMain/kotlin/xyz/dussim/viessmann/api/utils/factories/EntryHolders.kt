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
import xyz.dussim.viessmann.api.utils.EntryHolder

private val AccessLevelValues =
    setOf<AccessLevel.Strict>(
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

private val AggregatedStatusValues =
    setOf<AggregatedStatus.Strict>(
        AggregatedStatus.Error,
        AggregatedStatus.Offline,
        AggregatedStatus.Maintenance,
        AggregatedStatus.WorksProperly,
        AggregatedStatus.RemoteDiagnosticSession,
        AggregatedStatus.NbIotConnected,
    )

private val GenderValues =
    setOf<Gender.Strict>(
        Gender.Male,
        Gender.Female,
        Gender.Other,
    )

private val HeatingTypeValues =
    setOf<HeatingType.Strict>(
        HeatingType.None,
        HeatingType.FloorHeating,
        HeatingType.Radiators,
        HeatingType.Both,
        HeatingType.Undefined,
    )

private val GatewayStateValues =
    setOf<GatewayState.Strict>(
        GatewayState.Produced,
        GatewayState.Registered,
    )

private val GatewayTypeValues =
    setOf<GatewayType.Strict>(
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
private val InstallationTypeValues =
    setOf<InstallationType.Strict>(
        InstallationType.Commercial,
        InstallationType.Residential,
    )

private val InvitationStatusValues =
    setOf<InvitationStatus.Strict>(
        InvitationStatus.Accepted,
        InvitationStatus.Pending,
        InvitationStatus.Rejected,
    )

private val OwnershipTypeValues =
    setOf<OwnershipType.Strict>(
        OwnershipType.ResidentialEndUser,
        OwnershipType.PreCommissioning,
        OwnershipType.Oma,
        OwnershipType.CommercialV1,
        OwnershipType.CommercialV2,
        OwnershipType.None,
    )

private val TargetRealmValues =
    setOf<TargetRealm.Strict>(
        TargetRealm.Dc,
        TargetRealm.Genesis,
    )

private val NetworkStatusValues =
    setOf<NetworkStatus.Strict>(
        NetworkStatus.Offline,
        NetworkStatus.Online,
    )

private val TokenTypeValues =
    setOf<TokenType.Strict>(
        TokenType.Invited,
        TokenType.Requested,
    )

private val SerialEditorValues =
    setOf<SerialEditor.Strict>(
        SerialEditor.User,
        SerialEditor.DeviceCommunication,
        SerialEditor.Supporter,
    )

internal val AccessLevelEntryHolder =
    EntryHolder<AccessLevel.Strict> { AccessLevelValues }

internal val AggregatedStatusEntryHolder =
    EntryHolder<AggregatedStatus.Strict> { AggregatedStatusValues }

internal val GenderEntryHolder =
    EntryHolder<Gender.Strict> { GenderValues }

internal val HeatingTypeEntryHolder =
    EntryHolder<HeatingType.Strict> { HeatingTypeValues }

internal val GatewayStateEntryHolder =
    EntryHolder<GatewayState.Strict> { GatewayStateValues }

internal val GatewayTypeEntryHolder =
    EntryHolder<GatewayType.Strict> { GatewayTypeValues }

internal val InstallationTypeEntryHolder =
    EntryHolder<InstallationType.Strict> { InstallationTypeValues }

internal val InvitationStatusEntryHolder =
    EntryHolder<InvitationStatus.Strict> { InvitationStatusValues }

internal val OwnershipTypeEntryHolder =
    EntryHolder<OwnershipType.Strict> { OwnershipTypeValues }

internal val TargetRealmEntryHolder =
    EntryHolder<TargetRealm.Strict> { TargetRealmValues }

internal val NetworkStatusEntryHolder =
    EntryHolder<NetworkStatus.Strict> { NetworkStatusValues }

internal val TokenTypeEntryHolder =
    EntryHolder<TokenType.Strict> { TokenTypeValues }

internal val SerialEditorEntryHolder =
    EntryHolder<SerialEditor.Strict> { SerialEditorValues }
