package com.syncplus

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class IconSpinnerAdapter(
    private val context: Context,
    private val icons: TypedArray,
    private val iconNames: Array<String>
) : ArrayAdapter<String>(context, R.layout.icon_spinner_item, iconNames) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.icon_spinner_item, parent, false)

        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        val iconNameTextView = view.findViewById<TextView>(R.id.iconNameTextView)

        // Get the resource ID of the icon at the specified position
        val iconResourceId = icons.getResourceId(position, R.drawable.ic_default_icon)

        // Set the icon and icon name in the views
        iconImageView.setImageResource(iconResourceId)
        iconNameTextView.text = iconNames[position]

        return view
    }
}
