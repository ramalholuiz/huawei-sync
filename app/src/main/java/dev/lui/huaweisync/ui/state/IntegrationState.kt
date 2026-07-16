package dev.lui.huaweisync.ui.state

/** Providers shown on the integrations surface. This is presentation-only; it is not an SDK model. */
enum class IntegrationProvider(val displayName: String, val monogram: String) {
    HEALTH_CONNECT("Health Connect", "HC"),
    GYMRATS("GymRats", "GR"),
    SAMSUNG_HEALTH("Samsung Health", "SH"),
    GARMIN("Garmin", "GA"),
    FITBIT("Fitbit", "FB"),
    OURA("Oura", "OU"),
    STRAVA("Strava", "ST"),
    GOOGLE_FIT("Google Fit", "GF"),
}

/** Closed vocabulary prevents the screen from inventing successful delivery or consumer evidence. */
enum class IntegrationStatus(val label: String) {
    CONFIRMED_IN_HEALTH_CONNECT("Confirmed in Health Connect"),
    READY_FOR_GYMRATS_TO_READ("Ready for GymRats to read"),
    AVAILABLE_THROUGH_HEALTH_CONNECT("Available through Health Connect"),
    NOT_CONFIGURED("Not configured"),
    PREVIEW("Preview"),
    COMING_SOON("Coming soon"),
    ACTION_REQUIRED("Action required"),
    READY_TO_WRITE("Ready to write"),
    WRITE_IN_PROGRESS("Write in progress"),
    AWAITING_READBACK("Awaiting Health Connect readback"),
    RECONCILIATION_REQUIRED("Reconciliation required"),
    RETRY_REQUIRED("Retry required"),
    UNAVAILABLE("Unavailable"),
}

enum class IntegrationEvidenceLevel {
    READBACK,
    CONFIGURATION,
    LOCAL_STATE,
    ROADMAP_ONLY,
}

data class IntegrationItemState(
    val provider: IntegrationProvider,
    val status: IntegrationStatus,
    val detail: String,
    val evidenceLevel: IntegrationEvidenceLevel,
)

data class IntegrationState(
    val healthConnect: IntegrationItemState,
    val gymRats: IntegrationItemState,
    val samsungHealth: IntegrationItemState,
    val futureProviders: List<IntegrationItemState>,
) {
    val activePath: List<IntegrationItemState> = listOf(healthConnect, gymRats, samsungHealth)
    val allProviders: List<IntegrationItemState> = activePath + futureProviders

    init {
        require(gymRats.status == IntegrationStatus.READY_FOR_GYMRATS_TO_READ) {
            "GymRats must remain ready-to-read until Gate 2 supplies consumer evidence."
        }
        require(
            samsungHealth.status == IntegrationStatus.AVAILABLE_THROUGH_HEALTH_CONNECT ||
                samsungHealth.status == IntegrationStatus.NOT_CONFIGURED,
        ) { "Samsung Health may only describe configuration through Health Connect." }
        require(
            futureProviders.all {
                it.status == IntegrationStatus.PREVIEW ||
                    it.status == IntegrationStatus.COMING_SOON
            },
        ) { "Future providers may only be Preview or Coming soon." }
    }

    companion object {
        fun from(
            productState: ProductSyncState?,
            samsungHealthConfigured: Boolean = false,
        ): IntegrationState {
            val healthConnect = healthConnectItem(productState)
            return IntegrationState(
                healthConnect = healthConnect,
                gymRats = IntegrationItemState(
                    provider = IntegrationProvider.GYMRATS,
                    status = IntegrationStatus.READY_FOR_GYMRATS_TO_READ,
                    detail = "Health Connect holds the workout for compatible apps to read. Gate 2 has not confirmed consumer import.",
                    evidenceLevel = IntegrationEvidenceLevel.LOCAL_STATE,
                ),
                samsungHealth = IntegrationItemState(
                    provider = IntegrationProvider.SAMSUNG_HEALTH,
                    status = if (samsungHealthConfigured) {
                        IntegrationStatus.AVAILABLE_THROUGH_HEALTH_CONNECT
                    } else {
                        IntegrationStatus.NOT_CONFIGURED
                    },
                    detail = if (samsungHealthConfigured) {
                        "Uses the Android Health Connect hub; there is no direct provider connection."
                    } else {
                        "No Samsung Health configuration evidence is available on this device."
                    },
                    evidenceLevel = if (samsungHealthConfigured) {
                        IntegrationEvidenceLevel.CONFIGURATION
                    } else {
                        IntegrationEvidenceLevel.LOCAL_STATE
                    },
                ),
                futureProviders = futureProviderItems(),
            )
        }

        private fun healthConnectItem(productState: ProductSyncState?): IntegrationItemState {
            val confirmed = productState?.healthConnectStatus ==
                ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT &&
                productState.verification.realReadbackConfirmed
            if (confirmed) {
                return IntegrationItemState(
                    provider = IntegrationProvider.HEALTH_CONNECT,
                    status = IntegrationStatus.CONFIRMED_IN_HEALTH_CONNECT,
                    detail = "Official readback matched the deterministic workout identity and expected version.",
                    evidenceLevel = IntegrationEvidenceLevel.READBACK,
                )
            }

            val status = when (productState?.healthConnectStatus) {
                ProductHealthConnectStatus.READY_TO_SYNC -> IntegrationStatus.READY_TO_WRITE
                ProductHealthConnectStatus.WRITE_IN_PROGRESS -> IntegrationStatus.WRITE_IN_PROGRESS
                ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK -> IntegrationStatus.AWAITING_READBACK
                ProductHealthConnectStatus.RECONCILIATION_REQUIRED -> IntegrationStatus.RECONCILIATION_REQUIRED
                ProductHealthConnectStatus.RETRY_REQUIRED,
                ProductHealthConnectStatus.FAILED,
                -> IntegrationStatus.RETRY_REQUIRED
                ProductHealthConnectStatus.UNAVAILABLE -> IntegrationStatus.UNAVAILABLE
                ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT -> IntegrationStatus.AWAITING_READBACK
                ProductHealthConnectStatus.UPDATE_REQUIRED,
                ProductHealthConnectStatus.PERMISSION_REQUIRED,
                ProductHealthConnectStatus.ACTION_REQUIRED,
                null,
                -> IntegrationStatus.ACTION_REQUIRED
            }
            return IntegrationItemState(
                provider = IntegrationProvider.HEALTH_CONNECT,
                status = status,
                detail = "Confirmation appears only after an official ExerciseSessionRecord readback match.",
                evidenceLevel = IntegrationEvidenceLevel.LOCAL_STATE,
            )
        }

        private fun futureProviderItems(): List<IntegrationItemState> = listOf(
            future(IntegrationProvider.GARMIN, IntegrationStatus.COMING_SOON),
            future(IntegrationProvider.FITBIT, IntegrationStatus.COMING_SOON),
            future(IntegrationProvider.OURA, IntegrationStatus.COMING_SOON),
            future(IntegrationProvider.STRAVA, IntegrationStatus.PREVIEW),
            future(IntegrationProvider.GOOGLE_FIT, IntegrationStatus.PREVIEW),
        )

        private fun future(
            provider: IntegrationProvider,
            status: IntegrationStatus,
        ) = IntegrationItemState(
            provider = provider,
            status = status,
            detail = "Roadmap concept only. No SDK, account, or data connection is active.",
            evidenceLevel = IntegrationEvidenceLevel.ROADMAP_ONLY,
        )
    }
}
