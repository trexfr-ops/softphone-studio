package com.softphone.studio.model

/**
 * Encapsulates an active Polish virtual phone number line in the user's fleet.
 */
data class PhoneNumberItem(
    val id: String,
    val number: String,
    val countryTag: String,
    val carrierName: String,
    val daysRemaining: Int,
    val totalDays: Int = 3,
    val isActive: Boolean = true,
    val expirationDateStr: String = ""
) {
    val progressPercentage: Float
        get() = if (totalDays > 0) (daysRemaining.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f) else 0f
}
