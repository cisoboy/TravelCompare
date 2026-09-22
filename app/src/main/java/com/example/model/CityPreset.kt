package com.example.model

data class CityPreset(
    val id: String,
    val name: String,
    val country: String,
    val currencyCode: String,
    val currencySymbol: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultPickup: LocationPoint,
    val defaultDropoff: LocationPoint,
    val popularLocations: List<LocationPoint>,
    // Base pricing factors in local currency
    val baseFareMultiplier: Double = 1.0,
    val perKmRate: Double = 1.35,
    val perMinuteRate: Double = 0.28
)

object CityPresets {
    val LONDON = CityPreset(
        id = "london",
        name = "London",
        country = "United Kingdom",
        currencyCode = "GBP",
        currencySymbol = "£",
        centerLat = 51.5074,
        centerLng = -0.1278,
        defaultPickup = LocationPoint(
            name = "Piccadilly Circus",
            address = "West End, London W1J 9HP",
            latitude = 51.5100,
            longitude = -0.1340,
            tag = "Central"
        ),
        defaultDropoff = LocationPoint(
            name = "Heathrow Airport (LHR)",
            address = "Hounslow TW6 1AP, London",
            latitude = 51.4700,
            longitude = -0.4543,
            tag = "Airport"
        ),
        popularLocations = listOf(
            LocationPoint("Heathrow Airport (LHR)", "Hounslow TW6 1AP", 51.4700, -0.4543, "Airport"),
            LocationPoint("King's Cross Station", "Euston Rd, London N1 9AL", 51.5308, -0.1238, "Train"),
            LocationPoint("Piccadilly Circus", "West End, London W1J 9HP", 51.5100, -0.1340, "Central"),
            LocationPoint("Canary Wharf", "London E14 5AB", 51.5054, -0.0209, "Business"),
            LocationPoint("London Bridge Station", "London SE1 9QU", 51.5052, -0.0864, "Train"),
            LocationPoint("Tower Bridge", "Tower Bridge Rd, London SE1 2UP", 51.5055, -0.0754, "Attraction")
        ),
        baseFareMultiplier = 1.3,
        perKmRate = 1.65,
        perMinuteRate = 0.32
    )

    val PARIS = CityPreset(
        id = "paris",
        name = "Paris",
        country = "France",
        currencyCode = "EUR",
        currencySymbol = "€",
        centerLat = 48.8566,
        centerLng = 2.3522,
        defaultPickup = LocationPoint(
            name = "Gare du Nord",
            address = "18 Rue de Dunkerque, 75010 Paris",
            latitude = 48.8809,
            longitude = 2.3553,
            tag = "Train"
        ),
        defaultDropoff = LocationPoint(
            name = "Eiffel Tower",
            address = "Champ de Mars, 5 Av. Anatole France, 75007 Paris",
            latitude = 48.8584,
            longitude = 2.2945,
            tag = "Attraction"
        ),
        popularLocations = listOf(
            LocationPoint("Charles de Gaulle Airport (CDG)", "95700 Roissy-en-France", 49.0097, 2.5479, "Airport"),
            LocationPoint("Gare du Nord", "18 Rue de Dunkerque, 75010 Paris", 48.8809, 2.3553, "Train"),
            LocationPoint("Eiffel Tower", "Champ de Mars, 75007 Paris", 48.8584, 2.2945, "Attraction"),
            LocationPoint("Louvre Museum", "75001 Paris", 48.8606, 2.3376, "Attraction"),
            LocationPoint("La Défense Business Center", "92800 Puteaux", 48.8924, 2.2378, "Business"),
            LocationPoint("Gare de Lyon", "Place Louis-Armand, 75571 Paris", 48.8443, 2.3735, "Train")
        ),
        baseFareMultiplier = 1.25,
        perKmRate = 1.45,
        perMinuteRate = 0.30
    )

    val WARSAW = CityPreset(
        id = "warsaw",
        name = "Warsaw",
        country = "Poland",
        currencyCode = "PLN",
        currencySymbol = "zł",
        centerLat = 52.2297,
        centerLng = 21.0122,
        defaultPickup = LocationPoint(
            name = "Warszawa Centralna",
            address = "Aleje Jerozolimskie 54, 00-024 Warszawa",
            latitude = 52.2290,
            longitude = 21.0032,
            tag = "Train"
        ),
        defaultDropoff = LocationPoint(
            name = "Chopin Airport (WAW)",
            address = "Żwirki i Wigury 1, 00-906 Warszawa",
            latitude = 52.1672,
            longitude = 20.9679,
            tag = "Airport"
        ),
        popularLocations = listOf(
            LocationPoint("Chopin Airport (WAW)", "Żwirki i Wigury 1, 00-906 Warszawa", 52.1672, 20.9679, "Airport"),
            LocationPoint("Warszawa Centralna", "Aleje Jerozolimskie 54, Warszawa", 52.2290, 21.0032, "Train"),
            LocationPoint("Old Town Market Square", "Rynek Starego Miasta, Warszawa", 52.2497, 21.0122, "Attraction"),
            LocationPoint("Rondo Daszyńskiego Hub", "Wola, Warszawa", 52.2300, 20.9840, "Business"),
            LocationPoint("Westfield Arkadia", "Al. Jana Pawła II 82, Warszawa", 52.2571, 20.9845, "Shopping")
        ),
        baseFareMultiplier = 0.9,
        perKmRate = 2.80,
        perMinuteRate = 0.45
    )

    val BERLIN = CityPreset(
        id = "berlin",
        name = "Berlin",
        country = "Germany",
        currencyCode = "EUR",
        currencySymbol = "€",
        centerLat = 52.5200,
        centerLng = 13.4050,
        defaultPickup = LocationPoint(
            name = "Berlin Hauptbahnhof",
            address = "Europaplatz 1, 10557 Berlin",
            latitude = 52.5251,
            longitude = 13.3694,
            tag = "Train"
        ),
        defaultDropoff = LocationPoint(
            name = "Berlin Brandenburg Airport (BER)",
            address = "Melli-Beese-Ring 1, 12529 Schönefeld",
            latitude = 52.3667,
            longitude = 13.5033,
            tag = "Airport"
        ),
        popularLocations = listOf(
            LocationPoint("Berlin Airport (BER)", "12529 Schönefeld", 52.3667, 13.5033, "Airport"),
            LocationPoint("Berlin Hauptbahnhof", "Europaplatz 1, 10557 Berlin", 52.5251, 13.3694, "Train"),
            LocationPoint("Alexanderplatz", "10178 Berlin", 52.5219, 13.4132, "Central"),
            LocationPoint("Brandenburg Gate", "Pariser Platz, 10117 Berlin", 52.5163, 13.3777, "Attraction"),
            LocationPoint("Potsdamer Platz", "10785 Berlin", 52.5096, 13.3759, "Business")
        ),
        baseFareMultiplier = 1.2,
        perKmRate = 1.40,
        perMinuteRate = 0.30
    )

    val AMSTERDAM = CityPreset(
        id = "amsterdam",
        name = "Amsterdam",
        country = "Netherlands",
        currencyCode = "EUR",
        currencySymbol = "€",
        centerLat = 52.3676,
        centerLng = 4.9041,
        defaultPickup = LocationPoint(
            name = "Amsterdam Centraal",
            address = "Stationsplein, 1012 AB Amsterdam",
            latitude = 52.3791,
            longitude = 4.9003,
            tag = "Train"
        ),
        defaultDropoff = LocationPoint(
            name = "Schiphol Airport (AMS)",
            address = "Evert van de Beekstraat 202, 1118 CP Schiphol",
            latitude = 52.3105,
            longitude = 4.7683,
            tag = "Airport"
        ),
        popularLocations = listOf(
            LocationPoint("Schiphol Airport (AMS)", "1118 CP Schiphol", 52.3105, 4.7683, "Airport"),
            LocationPoint("Amsterdam Centraal", "Stationsplein, Amsterdam", 52.3791, 4.9003, "Train"),
            LocationPoint("Dam Square", "1012 JS Amsterdam", 52.3731, 4.8926, "Central"),
            LocationPoint("Zuidas Financial District", "1082 MD Amsterdam", 52.3364, 4.8722, "Business"),
            LocationPoint("Museumplein", "1071 DJ Amsterdam", 52.3582, 4.8811, "Attraction")
        ),
        baseFareMultiplier = 1.25,
        perKmRate = 1.50,
        perMinuteRate = 0.32
    )

    val NEW_YORK = CityPreset(
        id = "newyork",
        name = "New York",
        country = "United States",
        currencyCode = "USD",
        currencySymbol = "$",
        centerLat = 40.7128,
        centerLng = -74.0060,
        defaultPickup = LocationPoint(
            name = "Times Square",
            address = "Manhattan, NY 10036",
            latitude = 40.7580,
            longitude = -73.9855,
            tag = "Central"
        ),
        defaultDropoff = LocationPoint(
            name = "JFK International Airport",
            address = "Queens, NY 11430",
            latitude = 40.6413,
            longitude = -73.7781,
            tag = "Airport"
        ),
        popularLocations = listOf(
            LocationPoint("JFK Airport", "Queens, NY 11430", 40.6413, -73.7781, "Airport"),
            LocationPoint("Times Square", "Manhattan, NY 10036", 40.7580, -73.9855, "Central"),
            LocationPoint("Grand Central Terminal", "89 E 42nd St, NY 10017", 40.7527, -73.9772, "Train"),
            LocationPoint("Wall Street Financial", "New York, NY 10005", 40.7074, -74.0113, "Business"),
            LocationPoint("LaGuardia Airport (LGA)", "Queens, NY 11371", 40.7769, -73.8740, "Airport")
        ),
        baseFareMultiplier = 1.35,
        perKmRate = 1.75,
        perMinuteRate = 0.40
    )

    val ALL_CITIES = listOf(LONDON, PARIS, WARSAW, BERLIN, AMSTERDAM, NEW_YORK)
}
