package com.example.loompaslist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class MyListAdapter(var mCtx: Context, var resource: Int, var items: List<OompaLoompa>) :
    ArrayAdapter<OompaLoompa>(mCtx, resource, items) {
    //TODO: Thumbnail image no s'utilitza!

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resource, null)

        val image: ImageView = view.findViewById(R.id.image)
        val row1: TextView = view.findViewById(R.id.row1)
        val row2: TextView = view.findViewById(R.id.row2)

        var mItems: OompaLoompa = items[position]

        Picasso.with(mCtx).load(mItems.image).into(image)
        row1.text = mItems.id.toString() + ". " + mItems.last_name + ", " + mItems.first_name
        row2.text = mItems.profession + " (" + mItems.gender + ")" + "  - " + mItems.email

        return view
    }
}