package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.LocationPoint
import com.example.model.RideOption
import com.example.model.RideProvider

object RideLauncher {

    fun launchProviderApp(
        context: Context,
        option: RideOption,
        pickup: LocationPoint,
        dropoff: LocationPoint
    ) {
        val provider = option.provider
        val intent = when (provider) {
            RideProvider.UBER -> {
                // Uber URI scheme: uber://?action=setPickup&...
                val uberUri = "uber://?action=setPickup" +
                        "&pickup[latitude]=${pickup.latitude}" +
                        "&pickup[longitude]=${pickup.longitude}" +
                        "&pickup[nickname]=${Uri.encode(pickup.name)}" +
                        "&dropoff[latitude]=${dropoff.latitude}" +
                        "&dropoff[longitude]=${dropoff.longitude}" +
                        "&dropoff[nickname]=${Uri.encode(dropoff.name)}"

                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uberUri))
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Verify if app is installed
                if (isIntentResolvable(context, appIntent)) {
                    appIntent
                } else {
                    // Fallback to Uber mobile web
                    val webUri = "https://m.uber.com/ul/?action=setPickup" +
                            "&pickup[latitude]=${pickup.latitude}" +
                            "&pickup[longitude]=${pickup.longitude}" +
                            "&dropoff[latitude]=${dropoff.latitude}" +
                            "&dropoff[longitude]=${dropoff.longitude}"
                    Intent(Intent.ACTION_VIEW, Uri.parse(webUri)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }

            RideProvider.BOLT -> {
                // Bolt URI scheme: bolt://...
                val boltUri = "bolt://?pickup_lat=${pickup.latitude}&pickup_lng=${pickup.longitude}" +
                        "&dropoff_lat=${dropoff.latitude}&dropoff_lng=${dropoff.longitude}"

                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(boltUri))
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (isIntentResolvable(context, appIntent)) {
                    appIntent
                } else {
                    // Fallback to Bolt mobile web
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://bolt.eu/")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }
        }

        try {
            context.startActivity(intent)
            Toast.makeText(
                context,
                "Opening ${provider.displayName} for ${option.productName}...",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // Safe fallback to browser
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(provider.webFallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (ignored: Exception) {
                Toast.makeText(context, "Could not open ${provider.displayName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isIntentResolvable(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }
}
