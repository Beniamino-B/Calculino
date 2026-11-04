package com.calculino.app.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calculino.app.R
import com.calculino.app.data.ocr.OcrEngine
import com.calculino.app.data.ocr.OcrError
import com.calculino.app.ui.components.KawaiiBackdrop
import com.calculino.app.ui.science.ScienceToolHeader
import com.calculino.app.ui.theme.CalculinoSizes
import com.calculino.app.ui.theme.CalculinoSpacing
import com.calculino.app.ui.theme.LocalKawaiiPalette
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun CameraOcrRoute(
    ocrEngine: OcrEngine,
    onRecognized: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CameraOcrViewModel = viewModel(
        initializer = { CameraOcrViewModel(ocrEngine) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.recognizedFormula) {
        state.recognizedFormula?.let(onRecognized)
    }
    CameraOcrScreen(
        state = state,
        onPhotoCaptured = viewModel::recognize,
        onDismissError = viewModel::clearError,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun CameraOcrScreen(
    state: CameraOcrUiState,
    onPhotoCaptured: (String) -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalKawaiiPalette.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        KawaiiBackdrop()
        Column(modifier = Modifier.fillMaxSize()) {
            ScienceToolHeader(
                title = stringResource(R.string.scanner_title),
                subtitle = stringResource(R.string.scanner_subtitle),
                onBack = onBack,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(CalculinoSpacing.lg),
            ) {
                when {
                    !hasPermission -> PermissionContent(
                        onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    )
                    else -> CameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        enabled = !state.recognizing,
                        onPhotoCaptured = onPhotoCaptured,
                    )
                }
                if (state.recognizing) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = palette.surface)
                            Text(
                                text = stringResource(R.string.scanner_recognizing),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.surface,
                                modifier = Modifier.padding(top = CalculinoSpacing.md),
                            )
                        }
                    }
                }
            }
            state.error?.let { error ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(CalculinoSizes.hairline, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CalculinoSpacing.lg),
                ) {
                    Column(
                        modifier = Modifier.padding(CalculinoSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(CalculinoSpacing.xs),
                    ) {
                        Text(
                            text = stringResource(
                                when (error) {
                                    OcrError.EmptyText -> R.string.scanner_error_empty
                                    OcrError.RecognitionFailed -> R.string.scanner_error_failed
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Button(onClick = onDismissError) {
                            Text(stringResource(R.string.scanner_retry))
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(CalculinoSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.scanner_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(onRequest: () -> Unit) {
    val palette = LocalKawaiiPalette.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.scanner_permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = palette.title,
        )
        Text(
            text = stringResource(R.string.scanner_permission_text),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary,
            modifier = Modifier.padding(top = CalculinoSpacing.sm),
        )
        Button(
            onClick = onRequest,
            modifier = Modifier.padding(top = CalculinoSpacing.md),
        ) {
            Text(stringResource(R.string.scanner_permission_grant))
        }
    }
}

@Composable
private fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    enabled: Boolean,
    onPhotoCaptured: (String) -> Unit,
) {
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraUnavailable by remember { mutableStateOf(false) }
    var captureFailed by remember { mutableStateOf(false) }
    var retryToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(lifecycleOwner, retryToken) {
        cameraUnavailable = false
        try {
            val provider = context.awaitCameraProvider()
            val preview = Preview.Builder().build().also { preview ->
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
            )
            cameraProvider = provider
        } catch (_: Throwable) {
            cameraUnavailable = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )
        if (cameraUnavailable || captureFailed) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        if (cameraUnavailable) {
                            R.string.scanner_camera_unavailable
                        } else {
                            R.string.scanner_capture_failed
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(
                    onClick = {
                        captureFailed = false
                        retryToken++
                    },
                ) {
                    Text(stringResource(R.string.scanner_retry))
                }
            }
        }
        Surface(
            shape = CircleShape,
            color = LocalKawaiiPalette.current.keyEquals,
            border = BorderStroke(CalculinoSizes.hairline, LocalKawaiiPalette.current.border),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CalculinoSpacing.lg)
                .size(CalculinoSizes.heroIconContainer),
            onClick = {
                if (!enabled || cameraUnavailable || captureFailed) return@Surface
                val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
                val options = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    options,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onPhotoCaptured(file.absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            captureFailed = true
                        }
                    },
                )
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = stringResource(R.string.scanner_capture),
                    tint = LocalKawaiiPalette.current.onKeyEquals,
                    modifier = Modifier.size(CalculinoSizes.iconLarge),
                )
            }
        }
    }
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    continuation.resume(future.get())
                } catch (throwable: Throwable) {
                    continuation.resumeWithException(throwable)
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }
