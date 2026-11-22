package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BackendService(private val userPreferences: UserPreferences) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun submitPayment(
        planTitle: String,
        amountBdt: Int,
        email: String,
        senderNumber: String,
        trxId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!userPreferences.isBackendConfigured()) {
            return@withContext Result.failure(
                Exception("Payment service is temporarily unavailable. Please try again later.")
            )
        }

        val targetUrl = userPreferences.appsScriptUrl

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "payment_submission")
                put("plan", planTitle)
                put("amountBdt", amountBdt)
                put("email", email)
                put("senderNumber", senderNumber)
                put("trxId", trxId)
                put("timestamp", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(targetUrl)
                .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    try {
                        val resJson = JSONObject(body)
                        if (resJson.optString("status") == "error") {
                            Result.failure(Exception(resJson.optString("message", "Unable to submit payment request.")))
                        } else {
                            Result.success(resJson.optString("message", "Payment submitted for verification."))
                        }
                    } catch (_: Exception) {
                        Result.success("Payment submitted for verification.")
                    }
                } else {
                    Result.failure(Exception("Unable to reach payment service (HTTP ${response.code}). Please try again later."))
                }
            }
        } catch (_: Exception) {
            Result.failure(Exception("Network error while submitting payment. Please check your connection and try again."))
        }
    }

    suspend fun verifyAndRedeemSecretUpgrade(email: String, secretCode: String): Result<String> = withContext(Dispatchers.IO) {
        if (!userPreferences.isBackendConfigured()) {
            return@withContext Result.failure(
                Exception("Redeem verification service is temporarily unavailable. Please try again later.")
            )
        }

        val targetUrl = userPreferences.appsScriptUrl

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "secret_upgrade")
                put("email", email)
                put("secretCode", secretCode)
                put("status", "Active Lifetime User")
                put("timestamp", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(targetUrl)
                .post(jsonPayload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    try {
                        val resJson = JSONObject(body)
                        if (resJson.optString("status") == "success") {
                            Result.success(resJson.optString("message", "Lifetime Pro activated!"))
                        } else {
                            Result.failure(
                                Exception(resJson.optString("message", "Invalid email or access code."))
                            )
                        }
                    } catch (_: Exception) {
                        Result.failure(Exception("Unexpected verification response. Please try again."))
                    }
                } else {
                    Result.failure(Exception("Unable to verify credentials (HTTP ${response.code})."))
                }
            }
        } catch (_: Exception) {
            Result.failure(Exception("Network error while verifying credentials. Please check your connection."))
        }
    }

    companion object {
        const val GOOGLE_APPS_SCRIPT_SAMPLE_CODE = """
// ==============================================================
// PENGUIN PLUS - SECURE GOOGLE APPS SCRIPT BACKEND
// Spreadsheet: Penguin Plus Payments
// Sheet Name:   Submissions
// ==============================================================

function doPost(e) {
  var lock = LockService.getScriptLock();
  try {
    // 10 second lock to prevent race conditions & duplicate entries
    lock.waitLock(10000);
    
    if (!e || !e.postData || !e.postData.contents) {
      return createJsonResponse({
        status: "error",
        message: "Bad Request: No payload received."
      }, 400);
    }
    
    var data = JSON.parse(e.postData.contents);
    var action = String(data.action || "").trim();
    
    if (action !== "payment_submission" && action !== "secret_upgrade") {
      return createJsonResponse({
        status: "error",
        message: "Invalid action specified."
      }, 400);
    }
    
    var email = String(data.email || "").trim().toLowerCase();
    if (!email || email.indexOf("@") === -1) {
      return createJsonResponse({
        status: "error",
        message: "A valid email address is required."
      }, 400);
    }
    
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var sheet = ss.getActiveSheet();
    
    // Auto-create required headers on row 1 if sheet is empty
    if (sheet.getLastRow() === 0) {
      sheet.appendRow([
        "Timestamp",
        "Action",
        "Plan",
        "Email",
        "Sender Number",
        "Transaction ID",
        "Amount in BDT",
        "Payment Verification Status"
      ]);
      sheet.getRange(1, 1, 1, 8).setFontWeight("bold").setBackground("#4A1525").setFontColor("#FFFFFF");
    }
    
    var now = new Date();
    
    if (action === "secret_upgrade") {
      // Server-side verification using Apps Script ScriptProperties (never hardcoded or shipped in APK)
      var scriptProps = PropertiesService.getScriptProperties();
      var configuredSecret = String(scriptProps.getProperty("SECRET_REDEEM_CODE") || "").trim();
      var configuredEmail = String(scriptProps.getProperty("SECRET_REDEEM_EMAIL") || "").trim().toLowerCase();
      var providedSecret = String(data.secretCode || "").trim();

      if (!configuredSecret || providedSecret !== configuredSecret || (configuredEmail && email !== configuredEmail)) {
        return createJsonResponse({
          status: "error",
          message: "Invalid email or access code."
        });
      }

      sheet.appendRow([
        now,
        "secret_upgrade",
        "Lifetime Unlimited (Verified)",
        email,
        "OWNER_REDEEM",
        "VERIFIED_REDEEM",
        0,
        "VERIFIED_SECRET"
      ]);
      
      return createJsonResponse({
        status: "success",
        message: "Secret upgrade recorded successfully in Google Sheet."
      });
    }
    
    // Standard bKash Payment Submission
    var plan = String(data.plan || "Monthly Plan").trim();
    var senderNumber = String(data.senderNumber || "").trim();
    var trxId = String(data.trxId || "").trim().toUpperCase();
    var amountBdt = Number(data.amountBdt) || 0;
    
    if (!senderNumber || senderNumber.length < 11) {
      return createJsonResponse({
        status: "error",
        message: "Valid 11-digit bKash sender number is required."
      }, 400);
    }
    
    if (!trxId || trxId.length < 6) {
      return createJsonResponse({
        status: "error",
        message: "Valid Transaction ID (TrxID) is required."
      }, 400);
    }
    
    if (amountBdt <= 0) {
      return createJsonResponse({
        status: "error",
        message: "Invalid payment amount specified."
      }, 400);
    }
    
    // Duplicate Transaction ID prevention check
    var lastRow = sheet.getLastRow();
    if (lastRow > 1) {
      var existingTrxIds = sheet.getRange(2, 6, lastRow - 1, 1).getValues();
      for (var i = 0; i < existingTrxIds.length; i++) {
        if (String(existingTrxIds[i][0]).toUpperCase() === trxId) {
          return createJsonResponse({
            status: "error",
            message: "Duplicate Transaction ID: This TrxID has already been submitted."
          }, 400);
        }
      }
    }
    
    // Append submission with PENDING_VERIFICATION status
    // Client cannot forge or force instant 'VERIFIED' status
    sheet.appendRow([
      now,
      "payment_submission",
      plan,
      email,
      senderNumber,
      trxId,
      amountBdt,
      "PENDING_VERIFICATION"
    ]);
    
    return createJsonResponse({
      status: "success",
      message: "Payment submission recorded! Status: PENDING_VERIFICATION.",
      trxId: trxId
    });
    
  } catch (err) {
    return createJsonResponse({
      status: "error",
      message: "Server error processing payment record."
    }, 500);
  } finally {
    lock.releaseLock();
  }
}

function createJsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
"""
    }
}
