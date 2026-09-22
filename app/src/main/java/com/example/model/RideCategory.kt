package com.example.model

enum class RideCategory(
    val title: String,
    val description: String,
    val iconName: String,
    val capacity: Int
) {
    ECONOMY(
        title = "Economy",
        description = "Everyday affordable rides",
        iconName = "directions_car",
        capacity = 4
    ),
    COMFORT(
        title = "Comfort",
        description = "Newer cars with extra legroom & quiet ride",
        iconName = "airline_seat_recline_extra",
        capacity = 4
    ),
    GREEN(
        title = "Electric / Green",
        description = "100% hybrid or zero-emission electric vehicles",
        iconName = "electric_car",
        capacity = 4
    ),
    XL(
        title = "XL / Group",
        description = "Spacious vans or SUVs for up to 6 people",
        iconName = "group",
        capacity = 6
    ),
    PREMIUM(
        title = "Premium",
        description = "Luxury vehicles with top-rated professional chauffeurs",
        iconName = "star",
        capacity = 4
    )
}
