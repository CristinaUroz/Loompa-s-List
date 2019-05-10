package com.example.loompaslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.filter_dialog.view.*

class MainActivity : AppCompatActivity(), OompaLoompaContract.View {

    //Server data
    private var string_list: String = ""
    private var presenter = Presenter()

    //Layout items
    private lateinit var listView: ListView
    private lateinit var filter_btn: ImageView
    private lateinit var change_order_btn: ImageView
    private lateinit var order_spinner: Spinner

    //Filter and order data
    private var order_reverse: Boolean = false
    private var selectedGender: Int = 0
    private var selectedProfession: Int = 0
    private var orderBy: Int = 0

    //Invariable spinners options
    private val genders = arrayOf("All", "F", "M")
    private val possible_orders = arrayOf("Id", "Last name", "Profession", "Gender")

    companion object {
        private const val TAG: String = "MainActivity"
        private const val STRING_LIST = "String_list"
        private const val SELECTED_GENDER = "Selected_gender"
        private const val SELECTED_PROFESSION = "Selected_profession"
        private const val ORDER_REVERSE = "Order_reverse"
        private const val ORDER_BY = "Order_by"
        private const val SCROLL_POSITION = "Scroll_position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get objects in layout
        listView = findViewById<ListView>(R.id.listView)
        filter_btn = findViewById(R.id.filter_btn)
        change_order_btn = findViewById(R.id.arrows_btn)
        order_spinner = findViewById(R.id.sort_by)

        //Get data when open the app
        if (savedInstanceState == null) {
            presenter = Presenter(this, this, listView)
        }

        //Create order spinner
        order_spinner.adapter =
            ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, possible_orders)
        order_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                orderBy = order_spinner.selectedItemId.toInt()
                setList()
            }
        }

        //Button to change order
        change_order_btn.setOnClickListener {
            when (order_reverse) {
                false -> change_order_btn.setImageResource(R.drawable.descendent_order)
                true -> change_order_btn.setImageResource(R.drawable.ascendent_order)
            }
            order_reverse = !order_reverse
            setList()
        }

        //Button filter
        filter_btn.setOnClickListener {
            //Create a dialog to choose filters
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.filter_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle(R.string.filter)

            //Spinners creation
            mDialogView.spinner_gender.adapter =
                ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, genders)
            mDialogView.spinner_profession.adapter = ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_list_item_1,
                presenter.getProfessionsList().distinct()
            )
            //Spinners actual position
            mDialogView.spinner_gender.setSelection(selectedGender)
            mDialogView.spinner_profession.setSelection(selectedProfession)

            val mAlertDialog = mBuilder.show()

            //Apply filters
            mDialogView.apply_btn.setOnClickListener {
                mAlertDialog.dismiss()
                //Save the data and show the filtered list
                selectedGender = mDialogView.spinner_gender.selectedItemPosition
                selectedProfession = mDialogView.spinner_profession.selectedItemPosition
                setList()
                //Change filter icon to full if the user is filtering the list
                if (selectedGender != 0 || selectedProfession != 0) {
                    filter_btn.setImageResource(R.drawable.filter_full)
                } else {
                    filter_btn.setImageResource(R.drawable.filter_icon)
                }
            }
            mDialogView.cancel_btn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (string_list == "") {
            string_list = presenter.string_list
        }
        outState?.putString(STRING_LIST, string_list)
        outState?.putInt(SELECTED_GENDER, selectedGender)
        outState?.putInt(SELECTED_PROFESSION, selectedProfession)
        outState?.putBoolean(ORDER_REVERSE, order_reverse)
        outState?.putInt(ORDER_BY, orderBy)
        outState?.putInt(SCROLL_POSITION, listView.firstVisiblePosition)
        //TODO: Scroll position no és correcte!
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        string_list = savedInstanceState?.getCharSequence(STRING_LIST) as String
        if (string_list == "") {
            presenter = Presenter(this, this, listView)
        } else {
            presenter = Presenter(this, listView, string_list)

            //TODO: Tractament nuls
            selectedGender = savedInstanceState.getInt(SELECTED_GENDER)
            selectedProfession = savedInstanceState.getInt(SELECTED_PROFESSION)
            order_reverse = savedInstanceState.getBoolean(ORDER_REVERSE)
            orderBy = savedInstanceState.getInt(ORDER_BY)
            listView.smoothScrollToPosition(savedInstanceState.getInt(SCROLL_POSITION))

            setList()

            //Change icons
            if (order_reverse) {
                change_order_btn.setImageResource(R.drawable.descendent_order)
            } else {
                change_order_btn.setImageResource(R.drawable.ascendent_order)
            }
            if (selectedGender != 0 || selectedProfession != 0) {
                filter_btn.setImageResource(R.drawable.filter_full)
            }
        }
    }

    override fun setList() {
        presenter.setFilteredlist(
            genders[selectedGender],
            presenter.getProfessionsList().distinct()[selectedProfession],
            order_spinner.selectedItem.toString(),
            order_reverse,
            this@MainActivity
        )
    }
}
