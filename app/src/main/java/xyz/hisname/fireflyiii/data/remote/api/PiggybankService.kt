package xyz.hisname.fireflyiii.data.remote.api

import retrofit2.Call
import retrofit2.http.*
import xyz.hisname.fireflyiii.Constants.Companion.PIGGY_BANK_API_ENDPOINT
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyModel
import xyz.hisname.fireflyiii.repository.models.piggy.PiggySuccessModel

// Link to relevant doc: https://firefly-iii.readthedocs.io/en/latest/api/piggy_banks.html
interface PiggybankService {

    @GET(PIGGY_BANK_API_ENDPOINT)
    fun getPiggyBanks(): Call<PiggyModel>

    @DELETE("$PIGGY_BANK_API_ENDPOINT/{id}")
    fun deletePiggyBankById(@Path("id") id: Long): Call<PiggyModel>

    @GET("$PIGGY_BANK_API_ENDPOINT/{id}")
    fun getPiggyBankById(@Path("id") id: String): Call<PiggyData>

    @FormUrlEncoded
    @POST(PIGGY_BANK_API_ENDPOINT)
    fun createNewPiggyBank(@Field("name") name: String, @Field("account_id") accountId: String,
                           @Field("target_amount") targetAmount: String,
                           @Field("current_amount") currentAmount: String?,
                           @Field("start_date") startDate: String?, @Field("target_date") targetDate: String?,
                           @Field("notes") notes: String?): Call<PiggySuccessModel>
    @FormUrlEncoded
    @PUT("$PIGGY_BANK_API_ENDPOINT/{piggyId}")
    fun updatePiggyBank(@Path("piggyId") piggyId: Long,
                        @Field("name") name: String, @Field("account_id") accountId: String,
                        @Field("target_amount") targetAmount: String,
                        @Field("current_amount") currentAmount: String?,
                        @Field("start_date") startDate: String?, @Field("target_date") targetDate: String?,
                        @Field("notes") notes: String?): Call<PiggySuccessModel>
}