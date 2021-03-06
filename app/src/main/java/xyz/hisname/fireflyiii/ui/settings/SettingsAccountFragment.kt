package xyz.hisname.fireflyiii.ui.settings

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.preference.EditTextPreference
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_base.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.local.pref.AppPref
import xyz.hisname.fireflyiii.data.remote.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.auth.AuthViewModel
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError
import xyz.hisname.fireflyiii.util.extension.toastInfo
import xyz.hisname.fireflyiii.util.extension.toastSuccess
import xyz.hisname.fireflyiii.workers.RefreshTokenWorker
import java.util.*
import java.util.concurrent.TimeUnit

class SettingsAccountFragment: BaseSettings() {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(requireContext())) }
    private val authMethodPref by lazy { accManager.authMethod }
    private val authViewModel by lazy { getViewModel(AuthViewModel::class.java) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_account_settings)
        setAccountSection()
    }

    private fun setAccountSection(){
        val fireflyUrlPref = findPreference("fireflyUrl") as EditTextPreference
        fireflyUrlPref.apply {
            title = "Firefly URL"
            summary = AppPref(sharedPref).baseUrl
        }

        val accessTokenPref = findPreference("access_token") as EditTextPreference
        accessTokenPref.apply {
            title = "Access Token"
            summary = accManager.secretKey
        }
        val authMethod = findPreference("auth_method")

        if(Objects.equals(authMethodPref, "oauth")){
            authMethod.summary = "OAuth Authentication"
        } else {
            authMethod.summary = "Personal Access Authentication"
        }

        fireflyUrlPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            toastInfo("You should also change your access token", Toast.LENGTH_LONG)
            RetrofitBuilder.destroyInstance()
            true
        }
        accessTokenPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            RetrofitBuilder.destroyInstance()
            true
        }
        val refreshToken = findPreference("refresh_token")
        refreshToken.setOnPreferenceClickListener {
            toastInfo("Refreshing your token...")
            authViewModel.getRefreshToken().observe(this, Observer { success ->
                if(success){
                    toastSuccess("Token refresh success!")
                } else {
                    toastError("There was an error refreshing your token")
                }
            })
            true
        }
        val certBolean = findPreference("enable_cert_pinning")
        certBolean.setOnPreferenceChangeListener { _, _ ->
            RetrofitBuilder.destroyInstance()
            true
        }
        val certValue = findPreference("cert_value")
        certValue.setOnPreferenceChangeListener{ _, newValue ->
            try {
                Base64.decode(newValue.toString(), Base64.DEFAULT).toString(Charsets.UTF_8)
            }catch (exception: IllegalArgumentException){
                AlertDialog.Builder(requireContext())
                        .setTitle("Error parsing Certificate pin value")
                        .setMessage("Your certificate pin is not a valid base64 value. The app will continue" +
                                " to work but you should note that certificate pinning is now useless.")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
            }
            RetrofitBuilder.destroyInstance()
            true
        }
        val autoRefreshToken = findPreference("auto_refresh_token")
        val refreshTokenInterval = findPreference("refresh_token_interval")
        autoRefreshToken.setOnPreferenceChangeListener { _, newValue ->
            if(newValue == false){
                WorkManager.getInstance().cancelAllWorkByTag("refresh_worker")
            } else {
                AlertDialog.Builder(requireContext())
                        .setTitle("Warning!")
                        .setMessage("This feature may not work as expected on certain devices." +
                                "Currently, it ensures that your device is charging, connected to a " +
                                "network and the battery is not low before running.")
                        .setPositiveButton("OK") { _, _ -> }
                        .create()
                        .show()
            }
            true
        }
        refreshTokenInterval.setOnPreferenceChangeListener { _, newValue ->
            val workBuilder = PeriodicWorkRequest
                    .Builder(RefreshTokenWorker::class.java, newValue.toString().toLong(), TimeUnit.HOURS)
                    .addTag("refresh_worker")
                    .setConstraints(Constraints.Builder()
                            .setRequiresCharging(true)
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .setRequiresBatteryNotLow(true)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(workBuilder)
            true
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Account Settings"
    }

}