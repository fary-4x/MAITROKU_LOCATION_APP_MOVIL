package com.example.mei_troku

import android.content.Context
import android.location.Address
import com.example.mei_troku.DataClass.PlaceAddress
import org.osmdroid.bonuspack.location.GeocoderNominatim
import java.util.Locale


class PlaceSearchManager(private val context: Context) {
    private val geocoder: GeocoderNominatim

    init {
        geocoder = GeocoderNominatim("OSMBonusPackTutoUserAgent");
    }

    fun searchPlaces(query: String?): List<PlaceAddress>? {
        return try {
            val addresses = geocoder.getFromLocationName(query, 10)
            addresses.map { address ->
                val extras = address.extras
                PlaceAddress(
                    extras?.getString("display_name"),
                    address.getAddressLine(0)?.split(", ") ?: emptyList(),
                    address.adminArea,
                    address.subAdminArea,
                    address.locality,
                    address.thoroughfare,
                    address.postalCode,
                    address.countryCode,
                    address.countryName,
                    address.latitude,
                    address.longitude
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
