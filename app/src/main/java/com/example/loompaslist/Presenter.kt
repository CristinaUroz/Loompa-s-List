package com.example.loompaslist

import android.app.ProgressDialog
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.beust.klaxon.Klaxon
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.worker_dialog.view.*
import okhttp3.OkHttpClient
import okhttp3.Request

@Suppress("DEPRECATION")
class Presenter() : OompaLoompaContract.Presenter {

    var crew = mutableListOf<OompaLoompa>() //Formated data from the URL
    var string_list: String = "" //String of all the data
    var crew_temp: List<OompaLoompa> = crew //Sorted and filtered data
    var professionlist: MutableList<String> = arrayListOf("All") //List of different professions
    var listView: ListView? = null
    private val TAG: String = "Presenter"
    private val url_db: String = "https://www.mockaroo.com/aaafc040/download?count=10&key=aa8685c0"


    constructor (view: OompaLoompaContract.View, context: Context, listView2: ListView) : this() {
        getOompaLoompas(view, context).execute() //Get the data from URL
        listView = listView2
        //Set the clicklistener to the items to create the alert dialog with extended information of the user
        workerAlertDialogCreation(context)
    }

    constructor (context: Context, listView2: ListView, listString: String) : this() {
        listView = listView2
        workerAlertDialogCreation(context)

        //Format and display the data
        crew = Klaxon()
            .parseArray<OompaLoompa>(listString) as MutableList<OompaLoompa>
        string_list = listString
        createProfessionsList()
        crew_temp = crew
    }

    override fun workerAlertDialogCreation(context: Context) {
        listView!!.setOnItemClickListener { parent, view, position, id ->

            val mDialogView: View
            //Different layout depending on smartphone orientation
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mDialogView = LayoutInflater.from(context).inflate(R.layout.worker_dialog, null)
            } else {
                mDialogView = LayoutInflater.from(context).inflate(R.layout.worker_dialog_landscape, null)
            }
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)

            Picasso.with(context).load(crew_temp[position].image).into(mDialogView.worker_image)
            mDialogView.worker_id.text = crew_temp[position].id.toString()
            mDialogView.worker_name.text = crew_temp[position].last_name + ", " + crew_temp[position].first_name
            mDialogView.worker_profession.text = crew_temp[position].profession
            mDialogView.worker_gender.text = crew_temp[position].gender
            mDialogView.worker_email.text = crew_temp[position].email

            mBuilder.show()
        }
    }

    override fun createProfessionsList() {
        //Get a list of the professions of the workers of the actual list
        for (item in crew) {
            professionlist.add(item.profession)
        }
    }

    override fun getProfessionsList(): Array<String> {
        //Return the professions list
        return professionlist.toTypedArray()
    }

    override fun setFilteredlist(gender: String, profession: String, sort: String, order: Boolean, context: Context) {
        crew_temp = crew
        //Filter the list
        if (gender != "All") {
            crew_temp = crew_temp.filter { i -> i.gender == gender }
        }
        if (profession != "All") {
            crew_temp = crew_temp.filter { i -> i.profession == profession }
        }
        //Sort the list
        when (sort) {
            "Id" -> crew_temp = crew_temp.sortedWith(compareBy({ it.id }))
            "Last name" -> crew_temp = crew_temp.sortedWith(compareBy({ it.last_name }))
            "Profession" -> crew_temp = crew_temp.sortedWith(compareBy({ it.profession }))
            "Gender" -> crew_temp = crew_temp.sortedWith(compareBy({ it.gender }))
            else -> crew_temp = crew_temp.sortedWith(compareBy({ it.id }))
        }
        //Invers order if necessary
        if (order) {
            crew_temp = crew_temp.reversed()
        }
        //Set the list
        listView?.adapter = MyListAdapter(context, R.layout.list_item, crew_temp)
    }

    internal inner class getOompaLoompas(var view: OompaLoompaContract.View, val context: Context) :
        AsyncTask<Void, Void, String>() {

        lateinit var progressDialog: ProgressDialog
        private var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
            //Create progress dialog
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Downloading Oompa Loompas' information...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): String? {
            //If there is internet get data
            if (isNetworkAvailable()) {
                hasInternet = true
                val client = OkHttpClient()
                val request = Request.Builder().url(url_db).build()
                val response = client.newCall(request).execute()
                return response.body()?.string().toString()
            } else {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT)
                return ""
                //TODO: Search for problems if there is no internet connection
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            if (hasInternet) {
                //Form and set the data
                crew = Klaxon()
                    .parseArray<OompaLoompa>(result) as MutableList<OompaLoompa>
                Log.i(TAG, crew.size.toString())
                string_list = result
                createProfessionsList()
                listView?.adapter = MyListAdapter(context, R.layout.list_item, crew)
                crew_temp = crew

            } else {
                Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT)
            }
            progressDialog.dismiss()
        }

        private fun isNetworkAvailable(): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            return if (connectivityManager is ConnectivityManager) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected ?: false
            } else false
        }
    }
}