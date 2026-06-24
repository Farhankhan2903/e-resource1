@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit
) {
    val markers by viewModel.markers.collectAsState()

    // Build the dynamic Leaflet HTML string using remember to prevent flashing/reloading on recomposition
    val htmlContent = remember(markers) {
        val markersJson = markers.joinToString(",") { marker ->
            """
            {
                name: "${marker.name}",
                type: "${marker.type}",
                addr: "${marker.address}",
                lat: ${marker.lat},
                lng: ${marker.lng}
            }
            """
        }

        """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Leaflet OpenStreetMap</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
            <style>
                html, body {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #f4f6f9;
                }
                #map {
                    width: 100%;
                    height: 100%;
                }
                .leaflet-popup-content-wrapper {
                    border-radius: 12px;
                    padding: 4px;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                // Coordinates for Bangalore/Tech hub as default focus center
                var map = L.map('map', {
                    zoomControl: false
                }).setView([12.9716, 77.5946], 13);
                
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OSM</a>'
                }).addTo(map);

                // Add zoom control to bottom right instead of top left
                L.control.zoom({ position: 'bottomright' }).addTo(map);

                var markersList = [$markersJson];

                markersList.forEach(function(mk) {
                    var popupText = "<div style='font-family: sans-serif;'><b style='color: #1E88E5;'>" + mk.name + "</b><br/>" + 
                                   "<span style='font-size: 11px; color: #666;'>" + mk.type + "</span><br/>" + 
                                   "<small>" + mk.addr + "</small></div>";
                    L.marker([mk.lat, mk.lng])
                        .addTo(map)
                        .bindPopup(popupText);
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Technician OpenStreetMap",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("leaflet_osm_webview"),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                        }
                        webViewClient = WebViewClient()
                        tag = htmlContent
                        loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    val lastLoaded = webView.tag as? String
                    if (lastLoaded != htmlContent) {
                        webView.tag = htmlContent
                        webView.loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                    }
                }
            )
        }
    }
}
