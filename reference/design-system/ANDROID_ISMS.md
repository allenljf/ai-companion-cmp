# Android-only API 使用點清單（31 處）

> 這些是移植到 CMP 時需要 expect/actual 或替代方案的地方。
> 好消息：核心邏輯（ViewModel 1610 行）只有 2 處；PlanScreens/TripListScreen 完全沒有。

## 逐檔分佈

```
8 處  CompanionShareActions.kt
4 處  AiCompanionScreens.kt
3 處  ShareImageV2AssetResolver.kt
3 處  PosterHistoryStorage.kt
3 處  AiCompanionTripScreens.kt
2 處  BitmapExt.kt
2 處  AiCompanionViewModel.kt
2 處  AiCompanionActivity.kt
1 處  AiCompanionStates.kt
0 處  SixZonePosterSpec.kt
0 處  SixZonePosterComposer.kt
0 處  PosterFallbackAssets.kt
0 處  CompanionPosterFonts.kt
0 處  AiCompanionTripListScreen.kt
0 處  AiCompanionPlanScreens.kt
0 處  AiCompanionPhase2Previews.kt
0 處  AiCompanionAnnotationModule.kt
0
0
0
0
0
0
0
0
```

## 使用到的 Android API

```
   6 import android.graphics.Bitmap
   6 import android.content.Context
   3 import android.content.Intent
   2 import android.net.Uri
   2 import android.graphics.drawable.BitmapDrawable
   1 import android.widget.Toast
   1 import android.provider.MediaStore
   1 import android.os.Build
   1 import android.graphics.Color
   1 import android.graphics.BitmapFactory
   1 import android.content.pm.PackageManager
   1 import android.content.ActivityNotFoundException
   1 import android.app.DownloadManager
   1 import android.app.Activity
```
