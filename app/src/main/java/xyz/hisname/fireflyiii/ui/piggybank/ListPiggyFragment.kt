package xyz.hisname.fireflyiii.ui.piggybank

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.base_swipe_layout.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.repository.models.piggy.PiggyData
import xyz.hisname.fireflyiii.repository.piggybank.PiggyViewModel
import xyz.hisname.fireflyiii.ui.base.BaseFragment
import xyz.hisname.fireflyiii.util.extension.create
import xyz.hisname.fireflyiii.util.extension.getViewModel
import xyz.hisname.fireflyiii.util.extension.toastError

class ListPiggyFragment: BaseFragment() {

    private var dataAdapter = ArrayList<PiggyData>()
    private val piggyViewModel by lazy { getViewModel(PiggyViewModel::class.java) }
    private val fab by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.globalFAB) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.create(R.layout.fragment_base_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayView()
        pullToRefresh()
        initFab()
    }

    private fun displayView(){
        runLayoutAnimation(recycler_view)
        piggyViewModel.getAllPiggyBanks().observe(this, Observer {
            if(it.isNotEmpty()) {
                listText.isVisible = false
                listImage.isVisible = false
                recycler_view.isVisible = true
                recycler_view.adapter = PiggyRecyclerAdapter(it) { data: PiggyData -> itemClicked(data) }
            } else {
                listText.text = resources.getString(R.string.no_piggy_bank)
                listText.isVisible = true
                listImage.isVisible = true
                listImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_piggy_bank))
                recycler_view.isVisible = false
            }
        })
        piggyViewModel.isLoading.observe(this, Observer {
            swipeContainer.isRefreshing = it == true
        })

        piggyViewModel.apiResponse.observe(this, Observer {
            toastError(it)
        })
    }

    private fun itemClicked(piggyData: PiggyData){
        val bundle = bundleOf("piggyId" to piggyData.piggyId)
        requireFragmentManager().commit {
            replace(R.id.fragment_container, PiggyDetailFragment().apply { arguments = bundle })
            addToBackStack(null)
        }
    }

    private fun pullToRefresh(){
        swipeContainer.setOnRefreshListener {
            dataAdapter.clear()
            displayView()
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
    }

    private fun initFab(){
        fab.apply {
            isVisible = true
            translationY = (6 * 56).toFloat()
            animate().translationY(0.toFloat())
                    .setInterpolator(OvershootInterpolator(1.toFloat()))
                    .setStartDelay(300)
                    .setDuration(400)
                    .start()
            setOnClickListener{
                fab.isClickable = false
                val addPiggy = AddPiggyDialog()
                addPiggy.arguments = bundleOf("revealX" to fab.width / 2, "revealY" to fab.height / 2)
                addPiggy.show(requireFragmentManager().beginTransaction(), "add_piggy_dialog")
                fab.isClickable = true
            }
        }
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy > 0 && fab.isShown){
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        activity?.activity_toolbar?.title = resources.getString(R.string.piggy_bank)
    }

    override fun onResume() {
        super.onResume()
        activity?.activity_toolbar?.title = resources.getString(R.string.piggy_bank)
    }

    override fun onDetach() {
        super.onDetach()
        fab.isGone = true
    }
}