package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BackendService
import com.example.data.DownloadRepository
import com.example.data.ToastEvent
import com.example.data.UserPreferences
import com.example.model.DownloadHistoryItem
import com.example.model.DownloadItem
import com.example.model.InstagramParsedMedia
import com.example.model.PlatformType
import com.example.model.SubscriptionPlan
import com.example.model.SubscriptionState
import com.example.model.VideoResolution
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    HOME("Home"),
    PROGRESS("Progress"),
    DOWNLOADS("Downloads")
}

data class ActiveToast(
    val message: String,
    val isError: Boolean = false,
    val isDone: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val userPreferences = UserPreferences(application)
    val backendService = BackendService(userPreferences)
    val repository = DownloadRepository(application, viewModelScope)

    // Navigation & Tabs
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Home Section Platform Selection
    private val _selectedPlatform = MutableStateFlow<PlatformType?>(null)
    val selectedPlatform: StateFlow<PlatformType?> = _selectedPlatform.asStateFlow()

    private val _selectedSubCategory = MutableStateFlow("All")
    val selectedSubCategory: StateFlow<String> = _selectedSubCategory.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    // Video URL Parsing State (drives custom Animatable circular progress feedback)
    private val _isParsingUrl = MutableStateFlow(false)
    val isParsingUrl: StateFlow<Boolean> = _isParsingUrl.asStateFlow()

    // Instagram Story/Highlight Parsing State
    private val _isParsingStories = MutableStateFlow(false)
    val isParsingStories: StateFlow<Boolean> = _isParsingStories.asStateFlow()

    private val _parsedStories = MutableStateFlow<List<InstagramParsedMedia>>(emptyList())
    val parsedStories: StateFlow<List<InstagramParsedMedia>> = _parsedStories.asStateFlow()

    private val _parsedProfileInfo = MutableStateFlow<String?>(null)
    val parsedProfileInfo: StateFlow<String?> = _parsedProfileInfo.asStateFlow()

    // Modals
    private val _showQualityModal = MutableStateFlow(false)
    val showQualityModal: StateFlow<Boolean> = _showQualityModal.asStateFlow()

    private val _showSubscriptionModal = MutableStateFlow(false)
    val showSubscriptionModal: StateFlow<Boolean> = _showSubscriptionModal.asStateFlow()

    private val _showSecretUpgradeDialog = MutableStateFlow(false)
    val showSecretUpgradeDialog: StateFlow<Boolean> = _showSecretUpgradeDialog.asStateFlow()

    private val _previewItem = MutableStateFlow<DownloadItem?>(null)
    val previewItem: StateFlow<DownloadItem?> = _previewItem.asStateFlow()

    // Toast UI state
    private val _activeToast = MutableStateFlow<ActiveToast?>(null)
    val activeToast: StateFlow<ActiveToast?> = _activeToast.asStateFlow()

    // Subscription & Payment state
    val subscriptionState: StateFlow<SubscriptionState> = userPreferences.subscriptionState

    private val _isSubmittingPayment = MutableStateFlow(false)
    val isSubmittingPayment: StateFlow<Boolean> = _isSubmittingPayment.asStateFlow()

    private val _paymentFeedbackMessage = MutableStateFlow<String?>(null)
    val paymentFeedbackMessage: StateFlow<String?> = _paymentFeedbackMessage.asStateFlow()

    private val _isVerifyingSecret = MutableStateFlow(false)
    val isVerifyingSecret: StateFlow<Boolean> = _isVerifyingSecret.asStateFlow()

    private val _secretUpgradeError = MutableStateFlow<String?>(null)
    val secretUpgradeError: StateFlow<String?> = _secretUpgradeError.asStateFlow()

    private var toastJob: Job? = null
    private var parseUrlJob: Job? = null

    init {
        // Collect repository toast events
        viewModelScope.launch {
            repository.toastEvents.collectLatest { event ->
                showToast(
                    message = event.message,
                    durationMs = event.durationMs,
                    isError = event is ToastEvent.Sorry,
                    isDone = event is ToastEvent.Done
                )
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectPlatform(platform: PlatformType?) {
        _selectedPlatform.value = platform
        if (platform != null) {
            _selectedSubCategory.value = platform.subCategories.firstOrNull() ?: "All"
            // Reset story parsed state when switching platforms
            _parsedStories.value = emptyList()
            _parsedProfileInfo.value = null
        }
    }

    fun selectSubCategory(sub: String) {
        _selectedSubCategory.value = sub
        // If switched away from Story/Highlight, clear parsed list
        if (sub != "Story" && sub != "Highlights") {
            _parsedStories.value = emptyList()
            _parsedProfileInfo.value = null
        }
    }

    fun onUrlChanged(newUrl: String) {
        _urlInput.value = newUrl
        parseUrlJob?.cancel()

        val trimmed = newUrl.trim()
        if (trimmed.isBlank()) {
            _isParsingUrl.value = false
            return
        }

        // Auto detect platform if on Home and link is pasted
        if (_selectedPlatform.value == null) {
            val detected = PlatformType.detectPlatform(trimmed)
            _selectedPlatform.value = detected
            if (trimmed.contains("shorts", ignoreCase = true)) {
                _selectedSubCategory.value = "Shorts"
            } else if (trimmed.contains("stories", ignoreCase = true) || trimmed.contains("/s/")) {
                _selectedSubCategory.value = "Story"
            } else {
                _selectedSubCategory.value = detected.subCategories.firstOrNull() ?: "All"
            }
        }

        val looksLikeVideoUrl = trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length >= 8)

        val isStorySubCat = _selectedPlatform.value == PlatformType.INSTAGRAM &&
            (_selectedSubCategory.value == "Story" || _selectedSubCategory.value == "Highlights")

        if (looksLikeVideoUrl && !isStorySubCat) {
            parseUrlJob = viewModelScope.launch {
                _isParsingUrl.value = true
                delay(680)
                _isParsingUrl.value = false
            }
        } else {
            _isParsingUrl.value = false
        }
    }

    fun clearUrl() {
        parseUrlJob?.cancel()
        _isParsingUrl.value = false
        _urlInput.value = ""
        _parsedStories.value = emptyList()
        _parsedProfileInfo.value = null
    }

    // Check if the current URL or context is a YouTube Short
    fun isYouTubeShort(): Boolean {
        val url = _urlInput.value.trim().lowercase()
        return (_selectedPlatform.value == PlatformType.YOUTUBE || url.contains("youtube") || url.contains("youtu.be")) &&
                (url.contains("/shorts/") || url.contains("shorts") || _selectedSubCategory.value == "Shorts")
    }

    // Direct YouTube Short Download without Quality Selection Modal
    fun downloadYouTubeShortDirect() {
        if (_urlInput.value.isBlank()) {
            showToast("Paste a YouTube Shorts link first bro!", 2500, isError = true)
            return
        }

        val isPro = subscriptionState.value.isPro
        // For YouTube Shorts, present highest available resolution (1080p / 4K if Pro, 1080p Full HD highest free/pro)
        val bestResolution = if (isPro) VideoResolution.RES_4K else VideoResolution.RES_1080P

        repository.startDownload(
            platform = PlatformType.YOUTUBE,
            url = _urlInput.value.trim(),
            subCategory = "Shorts",
            resolution = bestResolution,
            title = "YouTube Short (HD Max)"
        )

        showToast("⚡ Downloading Short in Max Quality!", 2500)
        _currentTab.value = AppTab.PROGRESS
    }

    // Instagram Story & Highlights Parser
    fun parseInstagramStoriesOrHighlights(input: String, isHighlight: Boolean) {
        if (input.isBlank()) {
            showToast("Please enter an Instagram username or link bro!", 2500, isError = true)
            return
        }

        viewModelScope.launch {
            _isParsingStories.value = true
            _parsedStories.value = emptyList()
            _parsedProfileInfo.value = null

            // Clean username/link representation
            val cleanInput = input.trim()
                .replace("https://www.instagram.com/", "")
                .replace("https://instagram.com/", "")
                .replace("stories/", "")
                .replace("@", "")
                .trimEnd('/')

            val targetName = if (cleanInput.contains("/")) cleanInput.split("/").first() else cleanInput

            // Realistic parsing delay (simulating GraphQL / profile stories lookup)
            delay(1200)

            _isParsingStories.value = false
            _parsedProfileInfo.value = "@$targetName • Active Story Feed"

            // Generate parsed media items
            if (isHighlight) {
                _parsedStories.value = listOf(
                    InstagramParsedMedia("hl_1", "Highlight #1 (Tokyo Travel)", "Highlight Video", "Permanent", "0:28", "16 MB", 0xFFE1306C, true),
                    InstagramParsedMedia("hl_2", "Highlight #2 (Cherry Blossoms)", "Highlight Photo", "Permanent", "Photo", "4.2 MB", 0xFF833AB4, true),
                    InstagramParsedMedia("hl_3", "Highlight #3 (Street Food 4K)", "Highlight Video", "Permanent", "0:45", "32 MB", 0xFFF56040, true),
                    InstagramParsedMedia("hl_4", "Highlight #4 (Night Walk)", "Highlight Video", "Permanent", "0:30", "19 MB", 0xFF405DE6, true)
                )
            } else {
                _parsedStories.value = listOf(
                    InstagramParsedMedia("st_1", "Story Slide 1", "Video Story", "1 hour ago", "0:15", "9.4 MB", 0xFFE1306C, true),
                    InstagramParsedMedia("st_2", "Story Slide 2 (HD)", "Video Story", "3 hours ago", "0:15", "11.2 MB", 0xFF833AB4, true),
                    InstagramParsedMedia("st_3", "Story Slide 3 (Photo)", "Photo Story", "6 hours ago", "Photo", "3.8 MB", 0xFFF77737, true),
                    InstagramParsedMedia("st_4", "Story Slide 4", "Video Story", "9 hours ago", "0:14", "8.9 MB", 0xFFC13584, true),
                    InstagramParsedMedia("st_5", "Story Slide 5 (Live)", "Video Story", "14 hours ago", "0:15", "10.5 MB", 0xFF405DE6, true)
                )
            }

            showToast("Found ${_parsedStories.value.size} items available for download!", 2500, isDone = true)
        }
    }

    fun toggleStorySelection(id: String) {
        _parsedStories.value = _parsedStories.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun selectAllStories(select: Boolean) {
        _parsedStories.value = _parsedStories.value.map { it.copy(isSelected = select) }
    }

    fun downloadSelectedStories() {
        val selected = _parsedStories.value.filter { it.isSelected }
        if (selected.isEmpty()) {
            showToast("Please select at least one story/highlight bro!", 2500, isError = true)
            return
        }

        val isPro = subscriptionState.value.isPro
        val res = if (isPro) VideoResolution.RES_1080P else VideoResolution.RES_720P

        selected.forEach { media ->
            repository.startDownload(
                platform = PlatformType.INSTAGRAM,
                url = _urlInput.value,
                subCategory = _selectedSubCategory.value,
                resolution = res,
                title = "${_parsedProfileInfo.value?.split(" • ")?.firstOrNull() ?: "@User"} - ${media.title}"
            )
        }

        showToast("Started ${selected.size} download(s)!", 2500)
        _currentTab.value = AppTab.PROGRESS
    }

    fun openQualitySelector() {
        if (_urlInput.value.isBlank()) {
            showToast("Please paste a link first bro!", 2500, isError = true)
            return
        }
        _showQualityModal.value = true
    }

    fun closeQualitySelector() {
        _showQualityModal.value = false
    }

    fun openSubscriptionModal() {
        _showSubscriptionModal.value = true
    }

    fun closeSubscriptionModal() {
        _showSubscriptionModal.value = false
        _paymentFeedbackMessage.value = null
    }

    fun openSecretUpgradeDialog() {
        _secretUpgradeError.value = null
        _showSecretUpgradeDialog.value = true
    }

    fun closeSecretUpgradeDialog() {
        _showSecretUpgradeDialog.value = false
        _secretUpgradeError.value = null
        _isVerifyingSecret.value = false
    }

    fun clearSecretUpgradeError() {
        _secretUpgradeError.value = null
    }

    fun setPreviewItem(item: DownloadItem?) {
        _previewItem.value = item
    }

    fun triggerQuickDownload() {
        if (_urlInput.value.isBlank()) {
            showToast("Paste a link first for magic!", 2500, isError = true)
            return
        }

        // Check if it is a YouTube Short
        if (isYouTubeShort()) {
            downloadYouTubeShortDirect()
            return
        }

        val platform = _selectedPlatform.value ?: PlatformType.detectPlatform(_urlInput.value)
        val isPro = subscriptionState.value.isPro
        val bestResolution = if (isPro) VideoResolution.RES_4K else VideoResolution.RES_720P

        repository.startDownload(
            platform = platform,
            url = _urlInput.value.trim(),
            subCategory = _selectedSubCategory.value,
            resolution = bestResolution
        )

        closeQualitySelector()
        _currentTab.value = AppTab.PROGRESS
    }

    fun onResolutionSelected(resolution: VideoResolution) {
        val isPro = subscriptionState.value.isPro
        if (resolution.isProRequired && !isPro) {
            closeQualitySelector()
            openSubscriptionModal()
            return
        }

        val platform = _selectedPlatform.value ?: PlatformType.detectPlatform(_urlInput.value)
        repository.startDownload(
            platform = platform,
            url = _urlInput.value.trim(),
            subCategory = _selectedSubCategory.value,
            resolution = resolution
        )

        closeQualitySelector()
        _currentTab.value = AppTab.PROGRESS
    }

    fun submitBkashPayment(plan: SubscriptionPlan, email: String, senderNumber: String, trxId: String) {
        if (email.isBlank() || !email.contains("@")) {
            _paymentFeedbackMessage.value = "Please enter a valid email address."
            return
        }
        if (senderNumber.isBlank() || senderNumber.length < 11) {
            _paymentFeedbackMessage.value = "Please enter valid 11-digit bKash sender number."
            return
        }
        if (trxId.isBlank() || trxId.length < 6) {
            _paymentFeedbackMessage.value = "Please enter a valid Transaction ID (TrxID)."
            return
        }

        viewModelScope.launch {
            _isSubmittingPayment.value = true
            _paymentFeedbackMessage.value = null

            val result = backendService.submitPayment(
                planTitle = plan.title,
                amountBdt = plan.priceBdt,
                email = email.trim(),
                senderNumber = senderNumber.trim(),
                trxId = trxId.trim()
            )

            _isSubmittingPayment.value = false
            result.onSuccess { msg ->
                _paymentFeedbackMessage.value = "Payment submitted! Status: PENDING VERIFICATION.\nOur admin will verify TrxID ($trxId) and activate your subscription."
                showToast("Payment submitted for verification!", 3500, isDone = true)
                delay(3000)
                closeSubscriptionModal()
            }.onFailure { err ->
                _paymentFeedbackMessage.value = "${err.message ?: "Submission failed"}. Please verify TrxID."
            }
        }
    }

    fun verifySecretUpgrade(email: String, code: String) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedCode = code.trim()

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            _secretUpgradeError.value = "Please enter a valid authorized email."
            return
        }
        if (trimmedCode.isBlank()) {
            _secretUpgradeError.value = "Please enter your access code."
            return
        }

        viewModelScope.launch {
            _isVerifyingSecret.value = true
            _secretUpgradeError.value = null

            val result = backendService.verifyAndRedeemSecretUpgrade(trimmedEmail, trimmedCode)

            _isVerifyingSecret.value = false
            result.onSuccess {
                userPreferences.activateLifetimePro(email = trimmedEmail, isSecret = true)
                closeSecretUpgradeDialog()
                closeSubscriptionModal()
                showToast("Lifetime Pro Activated! Enjoy 4K & 1080p!", 3500, isDone = true)
            }.onFailure { err ->
                val msg = err.message ?: "Invalid credentials or redeem is unavailable."
                _secretUpgradeError.value = msg
                showToast(msg, 2500, isError = true)
            }
        }
    }

    fun updateAppsScriptUrl(newUrl: String) {
        userPreferences.appsScriptUrl = newUrl.trim()
        showToast("Apps Script Web App URL updated!", 2500, isDone = true)
    }

    private fun showToast(message: String, durationMs: Long = 3000, isError: Boolean = false, isDone: Boolean = false) {
        toastJob?.cancel()
        _activeToast.value = ActiveToast(message, isError, isDone)
        toastJob = viewModelScope.launch {
            delay(durationMs)
            _activeToast.value = null
        }
    }
}
