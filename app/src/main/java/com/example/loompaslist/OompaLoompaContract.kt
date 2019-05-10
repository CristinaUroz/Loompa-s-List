package com.example.loompaslist

import android.content.Context

interface OompaLoompaContract {
    interface View{

        fun setList()

    }


    interface Presenter{

        fun workerAlertDialogCreation(context: Context)

        fun createProfessionsList()

        fun getProfessionsList(): Array<String>

        fun setFilteredlist(gender:String, profession:String, sort: String, order: Boolean, context: Context)

    }
}