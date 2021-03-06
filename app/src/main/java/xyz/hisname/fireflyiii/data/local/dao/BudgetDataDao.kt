package xyz.hisname.fireflyiii.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import xyz.hisname.fireflyiii.repository.models.budget.BudgetData

@Dao
abstract class BudgetDataDao: BaseDao<BudgetData> {

    @Query("SELECT * FROM budget")
    abstract fun getAllBudget(): LiveData<MutableList<BudgetData>>

    @Query("SELECT * FROM budget WHERE (start_date =:startDate AND end_date =:endDate) AND " +
            "currency_code =:currencyCode")
    abstract fun getConstraintBudgetWithCurrency(startDate: String, endDate: String,
                                                 currencyCode: String): MutableList<BudgetData>

}