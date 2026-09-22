package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RideCategory
import com.example.model.RideProvider
import com.example.model.UserAccount
import com.example.model.UserPromotion

private val UberColor = Color(0xFF1E1E24)
private val BoltBrandGreen = Color(0xFF34D186)
private val BoltDarkGreen = Color(0xFF00A352)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsPromotionsDialog(
    accounts: List<UserAccount>,
    promotions: List<UserPromotion>,
    currencySymbol: String,
    onLoginUber: (name: String, email: String, isUberOne: Boolean) -> Unit,
    onLogoutUber: () -> Unit,
    onLoginBolt: (name: String, email: String, isBoltPlus: Boolean) -> Unit,
    onLogoutBolt: () -> Unit,
    onTogglePromotion: (promoId: String, isActive: Boolean) -> Unit,
    onAddPromotion: (
        provider: RideProvider,
        code: String,
        title: String,
        discountPercent: Double,
        flatDiscount: Double,
        maxCap: Double,
        category: RideCategory?
    ) -> Unit,
    onDeletePromotion: (promoId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Uber, 1 = Bolt

    val uberAccount = accounts.find { it.provider == RideProvider.UBER && it.isLoggedIn }
    val boltAccount = accounts.find { it.provider == RideProvider.BOLT && it.isLoggedIn }

    val uberPromotions = promotions.filter { it.provider == RideProvider.UBER }
    val boltPromotions = promotions.filter { it.provider == RideProvider.BOLT }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("accounts_promotions_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Linked Accounts & Promos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use your personal promotions to get true real-time prices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_accounts_dialog")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Provider Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else BoltBrandGreen,
                        height = 3.dp
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_uber_account")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uberAccount != null) Color(0xFF00C853) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uber Account",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uberAccount != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Connected",
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_bolt_account")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (boltAccount != null) BoltBrandGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bolt Account",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (boltAccount != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Connected",
                                tint = BoltDarkGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> ProviderAccountSection(
                    provider = RideProvider.UBER,
                    account = uberAccount,
                    promotions = uberPromotions,
                    currencySymbol = currencySymbol,
                    brandColor = UberColor,
                    onLogin = { name, email, isMember -> onLoginUber(name, email, isMember) },
                    onLogout = onLogoutUber,
                    onTogglePromotion = onTogglePromotion,
                    onAddPromotion = onAddPromotion,
                    onDeletePromotion = onDeletePromotion
                )
                1 -> ProviderAccountSection(
                    provider = RideProvider.BOLT,
                    account = boltAccount,
                    promotions = boltPromotions,
                    currencySymbol = currencySymbol,
                    brandColor = BoltDarkGreen,
                    onLogin = { name, email, isMember -> onLoginBolt(name, email, isMember) },
                    onLogout = onLogoutBolt,
                    onTogglePromotion = onTogglePromotion,
                    onAddPromotion = onAddPromotion,
                    onDeletePromotion = onDeletePromotion
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderAccountSection(
    provider: RideProvider,
    account: UserAccount?,
    promotions: List<UserPromotion>,
    currencySymbol: String,
    brandColor: Color,
    onLogin: (name: String, email: String, isMember: Boolean) -> Unit,
    onLogout: () -> Unit,
    onTogglePromotion: (promoId: String, isActive: Boolean) -> Unit,
    onAddPromotion: (
        provider: RideProvider,
        code: String,
        title: String,
        discountPercent: Double,
        flatDiscount: Double,
        maxCap: Double,
        category: RideCategory?
    ) -> Unit,
    onDeletePromotion: (promoId: String) -> Unit
) {
    var isAddingPromo by remember { mutableStateOf(false) }

    // Add promo form fields
    var newPromoCode by remember { mutableStateOf("") }
    var newPromoTitle by remember { mutableStateOf("") }
    var discountPercentText by remember { mutableStateOf("20") }
    var flatDiscountText by remember { mutableStateOf("0") }
    var maxCapText by remember { mutableStateOf("5") }
    var selectedCategory by remember { mutableStateOf<RideCategory?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("${provider.name.lowercase()}_account_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Account Status Card
        item {
            if (account != null && account.isLoggedIn) {
                // Logged in card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(46.dp),
                                    shape = CircleShape,
                                    color = brandColor
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = account.userName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = account.userName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = account.userEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.testTag("logout_${provider.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Sign Out", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (account.membershipTier.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (provider == RideProvider.UBER) Color(0xFFFFF8E1) else Color(0xFFE8F5E9),
                                border = BorderStroke(
                                    1.dp,
                                    if (provider == RideProvider.UBER) Color(0xFFFFD54F) else BoltBrandGreen
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Membership",
                                        tint = if (provider == RideProvider.UBER) Color(0xFFF57F17) else BoltDarkGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${account.membershipTier} Active (${account.memberDiscountPercent.toInt()}% automatic discount on all rides)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (provider == RideProvider.UBER) Color(0xFF5D4037) else Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Not logged in: Sign-in form
                LoginFormCard(
                    provider = provider,
                    brandColor = brandColor,
                    onLogin = onLogin
                )
            }
        }

        // 2. Personal Promotions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Discount,
                        contentDescription = "Promotions",
                        tint = brandColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${provider.displayName} Promotions & Vouchers (${promotions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { isAddingPromo = !isAddingPromo },
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_promo_button_${provider.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isAddingPromo) Icons.Default.ExpandLess else Icons.Default.Add,
                        contentDescription = "Add promo",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isAddingPromo) "Cancel" else "Add Promo",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Add promo expandable card
        item {
            AnimatedVisibility(
                visible = isAddingPromo,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Add Custom ${provider.displayName} Voucher",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPromoCode,
                            onValueChange = { newPromoCode = it.uppercase() },
                            label = { Text("Promo Code (e.g. SAVE25, WEEKEND5)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_promo_code")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPromoTitle,
                            onValueChange = { newPromoTitle = it },
                            label = { Text("Description / Label") },
                            placeholder = { Text("e.g. Personal Weekend perk") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = discountPercentText,
                                onValueChange = { discountPercentText = it },
                                label = { Text("Discount %") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = maxCapText,
                                onValueChange = { maxCapText = it },
                                label = { Text("Max Cap ($currencySymbol)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category restriction chips
                        Text(
                            text = "Applicable Ride Tier:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All Tiers") }
                            )
                            RideCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.title) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (newPromoCode.isNotBlank()) {
                                    val pct = discountPercentText.toDoubleOrNull() ?: 0.0
                                    val flat = flatDiscountText.toDoubleOrNull() ?: 0.0
                                    val cap = maxCapText.toDoubleOrNull() ?: 0.0
                                    onAddPromotion(
                                        provider,
                                        newPromoCode,
                                        newPromoTitle,
                                        pct,
                                        flat,
                                        cap,
                                        selectedCategory
                                    )
                                    isAddingPromo = false
                                    newPromoCode = ""
                                    newPromoTitle = ""
                                }
                            },
                            enabled = newPromoCode.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_promo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply Voucher to Compare Rates", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Promotions List
        if (promotions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Discount,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No ${provider.displayName} promotions linked",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Log in or add promo codes to see discounted fares",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(promotions, key = { it.id }) { promo ->
                PromotionItemCard(
                    promotion = promo,
                    currencySymbol = currencySymbol,
                    brandColor = brandColor,
                    onToggle = { isActive -> onTogglePromotion(promo.id, isActive) },
                    onDelete = { onDeletePromotion(promo.id) }
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LoginFormCard(
    provider: RideProvider,
    brandColor: Color,
    onLogin: (name: String, email: String, isMember: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("Alex Rivera") }
    var email by remember {
        mutableStateOf(
            if (provider == RideProvider.UBER) "alex.rivera@example.com" else "+44 7700 900123"
        )
    }
    var isMember by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = brandColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sign In to ${provider.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sync your personal vouchers and member perks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account Holder Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_name")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (provider == RideProvider.UBER) "Email Address" else "Phone / Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_login_email")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMember = !isMember }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isMember,
                    onCheckedChange = { isMember = it },
                    colors = CheckboxDefaults.colors(checkedColor = brandColor),
                    modifier = Modifier.testTag("checkbox_membership")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (provider == RideProvider.UBER) "I have an active Uber One membership (5% off)" else "I have an active Bolt Plus subscription (10% off)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Automatically applies member discounts in comparisons",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onLogin(name, email, isMember) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_login_${provider.name.lowercase()}"),
                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Sign in to ${provider.displayName} & Sync Promos",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PromotionItemCard(
    promotion: UserPromotion,
    currencySymbol: String,
    brandColor: Color,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("promo_item_${promotion.code}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (promotion.isActive) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (promotion.isActive) brandColor.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (promotion.isActive) brandColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                        border = BorderStroke(
                            1.dp,
                            if (promotion.isActive) brandColor.copy(alpha = 0.5f) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = promotion.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (promotion.isActive) brandColor else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = promotion.formattedDiscountText(currencySymbol),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (promotion.isActive) Color(0xFF00796B) else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = promotion.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (promotion.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = promotion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = promotion.isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = brandColor
                    ),
                    modifier = Modifier.testTag("toggle_promo_${promotion.code}")
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove promotion",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
