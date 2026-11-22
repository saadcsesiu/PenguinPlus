package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FloatingLiquidDock
import com.example.ui.components.InAppToastBanner
import com.example.ui.components.MediaPreviewModal
import com.example.ui.components.QualitySelectorModal
import com.example.ui.components.SecretUpgradeDialog
import com.example.ui.components.SubscriptionModal
import com.example.ui.components.TopHeader
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PenguinPlusApp()
            }
        }
    }
}

@Composable
fun PenguinPlusApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeDownloads by viewModel.repository.activeDownloads.collectAsState()
    val completedDownloads by viewModel.repository.completedDownloads.collectAsState()
    val downloadHistory by viewModel.repository.downloadHistory.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()

    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()

    val isParsingUrl by viewModel.isParsingUrl.collectAsState()
    val isParsingStories by viewModel.isParsingStories.collectAsState()
    val parsedStories by viewModel.parsedStories.collectAsState()
    val parsedProfileInfo by viewModel.parsedProfileInfo.collectAsState()

    val showQualityModal by viewModel.showQualityModal.collectAsState()
    val showSubscriptionModal by viewModel.showSubscriptionModal.collectAsState()
    val showSecretUpgradeDialog by viewModel.showSecretUpgradeDialog.collectAsState()
    val previewItem by viewModel.previewItem.collectAsState()

    val activeToast by viewModel.activeToast.collectAsState()
    val isSubmittingPayment by viewModel.isSubmittingPayment.collectAsState()
    val paymentFeedbackMessage by viewModel.paymentFeedbackMessage.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CreamBackground,
        topBar = {
            TopHeader(
                subscriptionState = subscriptionState,
                onProBadgeClick = { viewModel.openSubscriptionModal() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CreamBackground)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        selectedPlatform = selectedPlatform,
                        selectedSubCategory = selectedSubCategory,
                        urlInput = urlInput,
                        isPro = subscriptionState.isPro,
                        isYouTubeShort = viewModel.isYouTubeShort(),
                        isParsingUrl = isParsingUrl,
                        isParsingStories = isParsingStories,
                        parsedStories = parsedStories,
                        parsedProfileInfo = parsedProfileInfo,
                        onPlatformSelected = { viewModel.selectPlatform(it) },
                        onSubCategorySelected = { viewModel.selectSubCategory(it) },
                        onUrlChanged = { viewModel.onUrlChanged(it) },
                        onClearUrl = { viewModel.clearUrl() },
                        onOpenQualityModal = { viewModel.openQualitySelector() },
                        onQuickDownload = { viewModel.triggerQuickDownload() },
                        onDownloadShortDirect = { viewModel.downloadYouTubeShortDirect() },
                        onParseStories = { input, isHighlight ->
                            viewModel.parseInstagramStoriesOrHighlights(input, isHighlight)
                        },
                        onToggleStorySelection = { viewModel.toggleStorySelection(it) },
                        onSelectAllStories = { viewModel.selectAllStories(it) },
                        onDownloadSelectedStories = { viewModel.downloadSelectedStories() }
                    )
                }

                AppTab.PROGRESS -> {
                    ProgressScreen(
                        activeDownloads = activeDownloads,
                        onPause = { viewModel.repository.pauseDownload(it) },
                        onResume = { viewModel.repository.resumeDownload(it) },
                        onCancel = { viewModel.repository.cancelDownload(it) },
                        onGoToHome = { viewModel.selectTab(AppTab.HOME) }
                    )
                }

                AppTab.DOWNLOADS -> {
                    DownloadsScreen(
                        completedDownloads = completedDownloads,
                        downloadHistory = downloadHistory,
                        onItemClick = { viewModel.setPreviewItem(it) },
                        onDeleteCompleted = { viewModel.repository.deleteCompleted(it) },
                        onDeleteHistoryItem = { viewModel.repository.deleteHistoryItem(it) },
                        onClearAllHistory = { viewModel.repository.clearAllHistory() },
                        onRedownload = { viewModel.repository.redownloadHistoryItem(it) },
                        onGoToHome = { viewModel.selectTab(AppTab.HOME) }
                    )
                }
            }

            // In-App Toast Banner ("Downloading bro", "Done", "Sorry bro")
            InAppToastBanner(
                toast = activeToast,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Floating Liquid Dock at Bottom (Home, Progress, Downloads)
            FloatingLiquidDock(
                currentTab = currentTab,
                activeDownloadsCount = activeDownloads.size,
                completedDownloadsCount = completedDownloads.size,
                onTabSelected = { viewModel.selectTab(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Modal: Quality Selector
    QualitySelectorModal(
        isOpen = showQualityModal,
        isPro = subscriptionState.isPro,
        onDismiss = { viewModel.closeQualitySelector() },
        onResolutionSelected = { viewModel.onResolutionSelected(it) },
        onQuickDownload = { viewModel.triggerQuickDownload() }
    )

    // Modal: Pro Subscription & bKash Payment
    SubscriptionModal(
        isOpen = showSubscriptionModal,
        isSubmitting = isSubmittingPayment,
        feedbackMessage = paymentFeedbackMessage,
        onDismiss = { viewModel.closeSubscriptionModal() },
        onSubmitPayment = { plan, email, phone, trxId ->
            viewModel.submitBkashPayment(plan, email, phone, trxId)
        },
        onOpenSecretUpgrade = {
            viewModel.openSecretUpgradeDialog()
        }
    )

    // Modal: Secret Free Upgrade
    SecretUpgradeDialog(
        isOpen = showSecretUpgradeDialog,
        onDismiss = { viewModel.closeSecretUpgradeDialog() },
        onVerify = { email, pass ->
            viewModel.verifySecretUpgrade(email, pass)
        }
    )

    // Modal: Media Preview & Player
    MediaPreviewModal(
        item = previewItem,
        onDismiss = { viewModel.setPreviewItem(null) },
        onDelete = { viewModel.repository.deleteCompleted(it) }
    )
}
