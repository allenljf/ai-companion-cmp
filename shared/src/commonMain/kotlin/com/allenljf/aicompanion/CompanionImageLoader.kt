package com.allenljf.aicompanion

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

/**
 * 全域 ImageLoader（T18）：memory/disk cache 用 ImageLoader.Builder 平台預設（不手動調大小，
 * demo 圖片量小，預設值夠用）。network fetcher 用 coil-network-ktor3，
 * 不帶自訂 HttpClient/engine —— 沿用專案既有的 Ktor engine（androidMain=okhttp、iosMain=darwin），
 * 跟 CompanionApiClient 的 HttpClient() 同一套 engine 自動解析機制。
 */
fun companionImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory())
        }
        .crossfade(true)
        .build()
