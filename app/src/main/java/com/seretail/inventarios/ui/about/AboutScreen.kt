package com.seretail.inventarios.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seretail.inventarios.R
import com.seretail.inventarios.ui.components.SERTopBar
import com.seretail.inventarios.ui.theme.DarkBackground
import com.seretail.inventarios.ui.theme.DarkSurface
import com.seretail.inventarios.ui.theme.SERBlue
import com.seretail.inventarios.ui.theme.TextMuted
import com.seretail.inventarios.ui.theme.TextPrimary
import com.seretail.inventarios.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            SERTopBar(
                title = "Acerca de",
                onBackClick = onBackClick,
            )
        },
        containerColor = DarkBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // App logo
            Image(
                painter = painterResource(R.drawable.logo_ser),
                contentDescription = "Logo SER",
                modifier = Modifier.height(120.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(20.dp))

            // App name
            Text(
                text = "SER Inventarios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(4.dp))

            // Version
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )

            Spacer(Modifier.height(32.dp))

            // Company info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Servicios Empresariales Retail",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "seretail.com.mx",
                        style = MaterialTheme.typography.bodySmall,
                        color = SERBlue,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Contacto: avillegas@seretail.com.mx",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // App info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Desarrollado con Kotlin + Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Base de datos: Room + SQLite",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sincronizacion: Retrofit + WorkManager",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Copyright
            Text(
                text = "\u00A9 2026 SER \u2014 Todos los derechos reservados",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
