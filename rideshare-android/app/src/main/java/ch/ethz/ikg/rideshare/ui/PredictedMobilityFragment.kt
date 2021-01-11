package ch.ethz.ikg.rideshare.ui

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.ethz.ikg.rideshare.MainActivity
import ch.ethz.ikg.rideshare.R
import kotlinx.android.synthetic.main.mobility_element.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PredictedMobilityFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PredictedMobilityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PredictedMobilityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    private var mainActivity: MainActivity? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_predicted_mobility, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity

        val toolbar: Toolbar = mainActivity!!.findViewById(R.id.toolbar)
        toolbar.title = "Your Predicted Mobility"

        viewManager = LinearLayoutManager(activity)
        viewAdapter = PredictedMobilityElementsAdapter(arrayOf("14:00", "18:30"))

        recyclerView = view.findViewById<RecyclerView>(R.id.pred_mobility_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context, LinearLayoutManager.VERTICAL
        )

        val ATTRS = intArrayOf(android.R.attr.listDivider)
        val a = recyclerView.context.obtainStyledAttributes(ATTRS)
        val divider = a.getDrawable(0)
        val inset = resources.getDimensionPixelSize(R.dimen.divider_margin)
        val insetDivider = InsetDrawable(divider, inset, 0, inset, 0)
        a.recycle()

        dividerItemDecoration.setDrawable(insetDivider)
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PredictedMobilityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PredictedMobilityFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

class PredictedMobilityElementsAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<PredictedMobilityElementsAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val firstLine = view.first_line
        val firstLineShade = view.first_line_shade
        val secondLine = view.second_line
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): PredictedMobilityElementsAdapter.MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mobility_element, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.firstLine.text = myDataset[position]
        holder.firstLineShade.text = "(13:53 - 14:11)"
        holder.secondLine.text = "2.3 km by Car"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}
