package xyz.hisname.fireflyiii.repository.models.error

import com.google.gson.annotations.SerializedName

data class Errors(
        val name: List<String>?,
        val account_id: List<String>?,
        val current_amount: List<String>?,
        val targetDate: List<String>?,
        val currency_code: List<String>?,
        val amount_min: List<String>?,
        val repeat_freq: List<String>?,
        @SerializedName("transactions.0.destination_name")
        val transactions_destination_name: List<String>?,
        @SerializedName("transactions.0.destination_id")
        val transaction_destination_id: List<String>?,
        @SerializedName("transactions.0.currency_code")
        val transactions_currency: List<String>?,
        @SerializedName("transactions.0.source_name")
        val transactions_source_name: List<String>?,
        val bill_name: List<String>?,
        val piggy_bank_name: List<String>?,
        val account_number: List<String>?,
        val interest: List<String>?,
        @SerializedName("liability_start_date")
        val liabilityStartDate: List<String>?,
        @SerializedName("transactions.0.amount")
        val transaction_amount: List<String>?,
        val description: List<String>?,
        val date: List<String>?,
        val skip: List<String>?,
        val code: List<String>?,
        val symbol: List<String>?,
        @SerializedName("decimal_places")
        val decimalPlaces: List<String>?,
        val tag: List<String>?,
        val latitude: List<String>?,
        val longitude: List<String>?,
        @SerializedName("zoom_level")
        val zoomLevel: List<String>?,
        val iban: List<String>?
)