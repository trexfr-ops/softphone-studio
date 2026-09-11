package com.softphone.studio.model

data class PhoneNumberItem(
    val id: String,
    val number: String,
    val countryTag: String,
    val carrierName: String,
    val daysRemaining: Int,
    val totalDays: Int = 30,
    val isActive: Boolean = true
) {
    val progressPercentage: Float
        get() = (daysRemaining.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
}
