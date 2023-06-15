package com.thuypham.ptithcm.editvideo.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.databinding.DropdownItemBinding

class DropdownAdapter(
    private val context: Context,
    private val items: ArrayList<String>,
    private var selectedPos: Int = 0,
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, items) {
    override fun getCount() = items.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DropdownItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        bindView(binding, position)
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = DropdownItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        bindView(binding, position)
        return binding.root
    }

    @SuppressLint("NewApi")
    private fun bindView(binding: DropdownItemBinding, position: Int) {
        binding.tvName.text = getItem(position)
        binding.tvName.isSelected = true
        binding.tvName.setTextColor(if (selectedPos == position) context.getColor(R.color.purple_200) else context.getColor(R.color.black))
    }

    override fun getItem(position: Int): String? = items.getOrNull(position)

    fun updatePosition(position: Int){
        selectedPos = position
    }
}