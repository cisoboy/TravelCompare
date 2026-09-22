# Ride Compare

A modern Android application built with Jetpack Compose that compares real-time ride-hailing fares, arrival times (ETAs), and service tiers between **Uber** and **Bolt** side-by-side.

---

## Key Features

- **Live Rate Comparison**: Compares prices, pickup ETAs, and trip durations across multiple ride categories (Economy, Comfort, Green / Electric, XL / Group, Premium).
- **Personal Account Linking**: Sign in to your Uber and Bolt accounts to automatically apply membership perks (such as **Uber One** 5% off and **Bolt Plus** 10% off) as well as personal discount vouchers.
- **Custom Voucher Management**: Add, toggle, and manage promo codes with customizable percentage or flat discounts, max caps, and ride tier restrictions.
- **Location & Google Places Autocomplete**:
  - One-tap device GPS location detection to set current pickup coordinates.
  - Search any address, landmark, or venue with Google Places autocomplete.
  - Interactive coordinate editor with decimal and DMS notation support.
- **Detailed Fare Breakdown**: Inspect full cost breakdowns for each ride, including base fare, distance rate, time rate, surge multiplier, booking fees, and applied promo deductions.
- **Interactive Route Map**: Visual route canvas previewing pickup and drop-off markers, distance in kilometers, and estimated drive times.
- **City Presets**: Instant switching between international markets (London, Paris, Berlin, Warsaw, New York) with local currencies and realistic city pricing formulas.
- **One-Tap App Launch**: Deep-links directly to the installed Uber or Bolt application with pre-filled pickup and destination coordinates (with web fallback).
- **Saved Routes**: Bookmark frequently traveled routes for quick one-tap price checks.

---

## Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Language**: Kotlin 2.0+ with Coroutines & StateFlow
- **Architecture**: MVVM (Model-View-ViewModel) + Repository pattern
- **Local Database**: [Room Database](https://developer.android.com/training/data-storage/room) (SQLite) with KSP for offline persistence of accounts, vouchers, and saved routes
- **Location & Places**: Google Play Services Location API & Google Places API
- **Testing**: Robolectric local JVM testing and Roborazzi screenshot verification

---

## Getting Started

### Prerequisites

- Android Studio Ladybug / Meerkat or newer
- JDK 17+
- Android SDK 34+ (target SDK 36, min SDK 26)

### Installation & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ride-compare.git
   cd ride-compare
   ```

2. Open the project in Android Studio.

3. Build and run on an Android device or emulator:
   ```bash
   ./gradlew installDebug
   ```

4. Run unit and Robolectric tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## License

This project is licensed under the [MIT License](LICENSE).
