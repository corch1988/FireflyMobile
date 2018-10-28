package xyz.hisname.fireflyiii.ui.transaction

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_add_transaction.*
import kotlinx.android.synthetic.main.progress_overlay.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.repository.models.transaction.ErrorModel
import xyz.hisname.fireflyiii.repository.viewmodel.TransactionViewModel
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.ui.currency.CurrencyListFragment
import xyz.hisname.fireflyiii.util.DateTimeUtil
import xyz.hisname.fireflyiii.util.extension.*
import java.util.*
import kotlin.collections.ArrayList


class AddTransactionFragment: BaseFragment(), CurrencyListFragment.OnCompleteListener {

    private val transactionType: String by lazy { arguments?.getString("transactionType") ?: "" }
    private val model: TransactionViewModel by lazy { getViewModel(TransactionViewModel::class.java) }
    private val accountDatabase by lazy { AppDatabase.getInstance(requireActivity())?.accountDataDao() }
    private var accounts = ArrayList<String>()
    private var sourceAccounts = ArrayList<String>()
    private var destinationAccounts = ArrayList<String>()
    private val piggyBankDatabase by lazy { AppDatabase.getInstance(requireActivity())?.piggyDataDao() }
    private var piggyBank = ArrayList<String>()
    private val billDatabase by lazy { AppDatabase.getInstance(requireActivity())?.billDataDao() }
    private val bill = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_add_transaction, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        if(Objects.equals(transactionType, "transfers")){
            zipLiveData(accountDatabase?.getAssetAccount()!!, piggyBankDatabase?.getPiggy()!!)
                    .observe(this, Observer {
                        it.first.forEachIndexed { _, accountData ->
                            accounts.add(accountData.accountAttributes?.name!!)
                        }
                        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, accounts)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        sourceSpinner.isVisible = true
                        sourceAutoComplete.isGone = true
                        sourceSpinner.adapter = spinnerAdapter
                        destinationAutoComplete.isGone = true
                        destinationSpinner.isVisible = true
                        destinationSpinner.adapter = spinnerAdapter
                        it.second.forEachIndexed { _,piggyData ->
                            piggyBank.add(piggyData.piggyAttributes?.name!!)
                        }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, piggyBank)
                        piggyBankName.threshold = 1
                        piggyBankName.setAdapter(adapter)
                    })
        } else if(Objects.equals(transactionType, "income")){
            zipLiveData(accountDatabase?.getRevenueAccount()!!, accountDatabase?.getAssetAccount()!!)
                    .observe(this , Observer {
                        it.first.forEachIndexed { _, accountData ->
                            sourceAccounts.add(accountData.accountAttributes?.name!!)
                        }
                        it.second.forEachIndexed { _, accountData ->
                            destinationAccounts.add(accountData.accountAttributes?.name!!)
                        }
                        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, destinationAccounts)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        destinationSpinner.adapter = spinnerAdapter
                        destinationAutoComplete.isVisible = false
                        val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, sourceAccounts)
                        sourceAutoComplete.threshold = 1
                        sourceAutoComplete.setAdapter(autocompleteAdapter)
                        sourceSpinner.isVisible = false
                    })
        } else {
            zipLiveData(accountDatabase?.getAssetAccount()!!, accountDatabase?.getAllAccounts()!!)
                    .observe(this, Observer {
                        // Spinner for source account
                        it.first.forEachIndexed { _, accountData ->
                            sourceAccounts.add(accountData.accountAttributes?.name!!)
                        }
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sourceAccounts)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        sourceAutoComplete.isVisible = false
                        sourceSpinner.adapter = adapter
                        // This is used for auto complete for destination account
                        it.second.forEachIndexed { _, accountData ->
                            destinationAccounts.add(accountData.accountAttributes?.name!!)
                        }
                        val autocompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, destinationAccounts)
                        destinationAutoComplete.threshold = 1
                        destinationAutoComplete.setAdapter(autocompleteAdapter)
                        destinationSpinner.isVisible = false
                    })
            billDatabase?.getAllBill()?.observe(this, Observer {
                if(it.isNotEmpty()){
                    it.forEachIndexed { _,billData ->
                        bill.add(billData.billAttributes?.name!!)
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, bill)
                    billEditText.threshold = 1
                    billEditText.setAdapter(adapter)
                }
            })
        }
        setupWidgets()
    }

    private fun setupWidgets(){
        transactionDateEditText.setText(DateTimeUtil.getTodayDate())
        val calendar = Calendar.getInstance()
        val transactionDate = DatePickerDialog.OnDateSetListener {
            _, year, monthOfYear, dayOfMonth ->
            run {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                transactionDateEditText.setText(DateTimeUtil.getCalToString(calendar.timeInMillis.toString()))
            }
        }
        transactionDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), transactionDate, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .show()
        }
        if(Objects.equals(transactionType, "expense")){
            billEditText.isVisible = true
        } else if(Objects.equals(transactionType, "transfers")){
            piggyBankName.isVisible = true
        }
        descriptionEditText.isFocusable = true
        descriptionEditText.isFocusableInTouchMode = true
        descriptionEditText.requestFocus()
        currencyEditText.setOnClickListener{
            val currencyListFragment = CurrencyListFragment().apply {
                bundleOf("fireflyUrl" to baseUrl, "access_token" to accessToken)
            }
            currencyListFragment.show(requireFragmentManager(), "currencyList" )
            currencyListFragment.setCurrencyListener(this)

        }
    }

    // well...:(
    private fun convertString(pleaseLowerCase: Boolean): String{
        var convertedString = ""
        when {
            Objects.equals(transactionType, "expense") -> convertedString = if(pleaseLowerCase){
                "withdrawal"
            } else {
                "Withdrawal"
            }
            Objects.equals(transactionType, "income") -> convertedString = if(pleaseLowerCase) {
                "deposit"
            } else {
                "Deposit"
            }
            Objects.equals(transactionType, "transfers") -> convertedString = if(pleaseLowerCase) {
                "transfer"
            } else {
                "Transfer"
            }
        }
        return convertedString
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Add " + convertString(false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar?.title = "Add " + convertString(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.save_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if(item.itemId == R.id.menu_item_save){
            hideKeyboard()
            val billName: String? = if(billEditText.isBlank()){
                null
            } else {
                billEditText.getString()
            }
            val piggyBank: String? = if(piggyBankName.isBlank()){
                null
            } else {
                piggyBankName.getString()
            }
            var sourceAccount = ""
            var destinationAccount = ""
            if(Objects.equals("expense", transactionType)){
                sourceAccount = sourceSpinner.selectedItem.toString()
                destinationAccount = destinationAutoComplete.getString()
            } else if(Objects.equals("transfers", transactionType)){
                sourceAccount = sourceSpinner.selectedItem.toString()
                destinationAccount = destinationSpinner.selectedItem.toString()
            } else if(Objects.equals("income", transactionType)){
                sourceAccount = sourceAutoComplete.getString()
                destinationAccount = destinationSpinner.selectedItem.toString()
            }
            ProgressBar.animateView(progress_overlay, View.VISIBLE, 0.4f, 200)
                model.addTransaction(baseUrl, accessToken, convertString(true),
                        descriptionEditText.getString(), transactionDateEditText.getString(), piggyBank,
                        billName, transactionAmountEditText.getString(), sourceAccount,
                        destinationAccount,
                        currencyEditText.getString()).observe(this, Observer { transactionResponse ->
                    if (transactionResponse.getResponse() != null) {
                        toastSuccess("Transaction Added")
                        requireFragmentManager().popBackStack()
                    } else if(transactionResponse.getErrorMessage() != null){
                        ProgressBar.animateView(progress_overlay, View.GONE, 0f, 200)
                        val errorMessage = transactionResponse.getErrorMessage()
                        val gson = Gson().fromJson(errorMessage, ErrorModel::class.java)
                        when {
                            gson.errors.transactions_currency != null -> {
                                if(gson.errors.transactions_currency.contains("is required")){
                                    currencyEditText.error = "Currency Code Required"
                                } else {
                                    currencyEditText.error = "Invalid Currency Code"
                                }
                            }
                            gson.errors.bill_name != null -> billEditText.error = "Invalid Bill Name"
                            gson.errors.piggy_bank_name != null -> piggyBankName.error = "Invalid Piggy Bank Name"
                            gson.errors.transactions_destination_name != null -> destinationAutoComplete.error = "Invalid Destination Account"
                            gson.errors.transactions_source_name != null -> sourceAutoComplete.error = "Invalid Source Account"
                            gson.errors.transaction_destination_id != null -> toastError(gson.errors.transaction_destination_id[0])
                            else -> toastError("Error occurred while saving transaction", Toast.LENGTH_LONG)
                        }
                    } else if(transactionResponse.getError() != null){
                        if(transactionResponse.getError()!!.localizedMessage.startsWith("Unable to resolve host")){
                            if(Objects.equals("transfers", convertString(true))){
                                val transferBroadcast = Intent("firefly.hisname.ADD_TRANSFER")
                                val extras = bundleOf(
                                        "description" to descriptionEditText.getString(),
                                        "date" to transactionDateEditText.getString(),
                                        "amount" to transactionAmountEditText.getString(),
                                        "currency" to currencyEditText.getString(),
                                        "sourceName" to sourceAccount,
                                        "destinationName" to destinationAccount,
                                        "piggyBankName" to piggyBank
                                )
                                transferBroadcast.putExtras(extras)
                                requireActivity().sendBroadcast(transferBroadcast)
                                toastOffline(getString(R.string.data_added_when_user_online, "Transfer"))
                            } else if(Objects.equals("deposit", convertString(true))){
                                val transferBroadcast = Intent("firefly.hisname.ADD_DEPOSIT")
                                val extras = bundleOf(
                                        "description" to descriptionEditText.getString(),
                                        "date" to transactionDateEditText.getString(),
                                        "amount" to transactionAmountEditText.getString(),
                                        "currency" to currencyEditText.getString(),
                                        "destinationName" to destinationAccount
                                )
                                transferBroadcast.putExtras(extras)
                                requireActivity().sendBroadcast(transferBroadcast)
                                toastOffline(getString(R.string.data_added_when_user_online, "Deposit"))
                            } else if(Objects.equals("withdrawal", convertString(true))){
                                val withdrawalBroadcast = Intent("firefly.hisname.ADD_WITHDRAW")
                                val extras = bundleOf(
                                        "description" to descriptionEditText.getString(),
                                        "date" to transactionDateEditText.getString(),
                                        "amount" to transactionAmountEditText.getString(),
                                        "currency" to currencyEditText.getString(),
                                        "sourceName" to sourceAccount,
                                        "billName" to billName
                                )
                                withdrawalBroadcast.putExtras(extras)
                                requireActivity().sendBroadcast(withdrawalBroadcast)
                                toastOffline(getString(R.string.data_added_when_user_online, "Withdrawal"))
                            }
                        }
                    }
            })
        }
        return true
    }

    override fun onDestroyView() {
        val bundle = bundleOf("fireflyUrl" to baseUrl,
                "access_token" to accessToken, "transactionType" to transactionType)
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TransactionFragment().apply { arguments = bundle })
                .commit()
        super.onDestroyView()
    }

    override fun onCurrencyClickListener(currency: String) {
        currencyEditText.setText(currency)
    }

}