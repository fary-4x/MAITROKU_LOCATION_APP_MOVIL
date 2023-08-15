package com.example.mei_troku.DataClass

data class PlaceAddress(
    val displayName: String?,
    val addressLines: List<String>,
    val admin: String?,
    val subAdmin: String?,
    val locality: String?,
    val thoroughfare: String?,
    val postalCode: String?,
    val countryCode: String?,
    val countryName: String?,
    val latitude: Double,
    val longitude: Double
)
