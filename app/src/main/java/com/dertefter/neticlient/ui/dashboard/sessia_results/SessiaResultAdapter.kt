package com.dertefter.neticlient.ui.dashboard.sessia_results

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.ItemSessiaResultBinding
import com.dertefter.neticore.features.sessia_results.model.SessiaResultItem
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.pow


class SessiaResultAdapter(private var items: List<SessiaResultItemUi>) :
    RecyclerView.Adapter<SessiaResultAdapter.ViewHolder>() {

    data class SessiaResultItemUi(
        val item: SessiaResultItem,
        var isChecked: Boolean? = null
    )

    fun updateData(newItems: List<SessiaResultItem>) {
        this.items = newItems.map { SessiaResultItemUi(it) }
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemSessiaResultBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun interpolateColor(colorStart: Int, colorEnd: Int, value: Int): Int {
            val fraction = value.toDouble().pow(4) / 100f.pow(4)

            val startA = Color.alpha(colorStart)
            val startR = Color.red(colorStart)
            val startG = Color.green(colorStart)
            val startB = Color.blue(colorStart)

            val endA = Color.alpha(colorEnd)
            val endR = Color.red(colorEnd)
            val endG = Color.green(colorEnd)
            val endB = Color.blue(colorEnd)

            val a = (startA + ((endA - startA) * fraction)).toInt()
            val r = (startR + ((endR - startR) * fraction)).toInt()
            val g = (startG + ((endG - startG) * fraction)).toInt()
            val b = (startB + ((endB - startB) * fraction)).toInt()

            return Color.argb(a, r, g, b)
        }


        fun bind(_item: SessiaResultItemUi) {
            val item = _item.item

            var ectsPt1 = item.score_ects
            var ectsPt2 = if (ectsPt1.length > 1) ectsPt1[1].toString() else ""
            ectsPt1 = if (ectsPt2.isNotEmpty()) ectsPt1.replaceFirst(ectsPt2, "") else ectsPt1


            binding.title.text = item.title
            binding.titleShrink.text = item.title
            binding.value.text = "${itemView.context.getString(R.string.balli)}: ${item.score}"
            binding.ects.text = ectsPt1
            binding.ectsShrink.text = ectsPt1

            if (ectsPt2 == "+"){
                binding.ectsAdd.setImageResource(R.drawable.plus_icon)
                binding.ectsAddShrink.setImageResource(R.drawable.plus_icon)
            } else if (ectsPt2 == "-"){
                binding.ectsAdd.setImageResource(R.drawable.minus_icon)
                binding.ectsAddShrink.setImageResource(R.drawable.minus_icon)
            }else{
                ectsPt2 = ""
            }

            binding.ectsAdd.isGone = ectsPt2.isEmpty()
            binding.ectsAddShrink.isGone = ectsPt2.isEmpty()

            binding.valueFive.text = "${itemView.context.getString(R.string.five_balli)}: ${item.score_five}"
            binding.date.text =  "${itemView.context.getString(R.string.date)}: ${item.date}"
            var intScore = item.score?.toIntOrNull()

            binding.contentExpand.isGone = (_item.isChecked != true)
            binding.contentShrink.isGone = (_item.isChecked == true)

            binding.image.let {
                if (_item.isChecked != true) return@let
                val inter  = 	PathInterpolator(0.2f, 0f, 0f, 1f)
                ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "rotation", 80f, 0f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "scaleX", 0f, 1f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "scaleY", 0f, 1f).apply {
                    interpolator = inter
                }.start()
            }

            binding.imageShrink.let {
                if (_item.isChecked != false) return@let
                val inter  = 	PathInterpolator(0.2f, 0f, 0f, 1f)
                ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "rotation", -80f, 0f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "scaleX", 1.4f, 1f).apply {
                    interpolator = inter
                }.start()
                ObjectAnimator.ofFloat(it, "scaleY", 1.4f, 1f).apply {
                    interpolator = inter
                }.start()
            }


            if (item.score_five == "Зач") {
                if (item.score.isNullOrEmpty() || item.score!!.toInt() == 0) {
                    binding.value.text = "100"
                    intScore = 100
                    binding.ects.text = "A"
                    binding.ectsShrink.text = "А"
                }
            }

            if (intScore != null){
                val colorBgStart = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorTertiaryContainer)
                val colorBgEnd = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorPrimaryContainer)
                val colorTvStart = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnTertiaryContainer)
                val colorTVEnd = MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnPrimaryContainer)
                val colorTv = interpolateColor(colorTvStart, colorTVEnd, intScore)
                val colorBg = interpolateColor(colorBgStart, colorBgEnd, intScore)

                val colorAddTv = interpolateColor(colorTvStart, colorTVEnd, intScore/4)
                val colorAddBg = interpolateColor(colorBgStart, colorBgEnd, intScore/4)

                binding.ects.setTextColor(colorTv)
                binding.ectsShrink.setTextColor(colorTv)
                binding.image.imageTintList = ColorStateList.valueOf(colorBg)
                binding.imageShrink.imageTintList = ColorStateList.valueOf(colorBg)
                binding.ectsAdd.backgroundTintList = ColorStateList.valueOf(colorAddBg)
                binding.ectsAddShrink.backgroundTintList = ColorStateList.valueOf(colorAddBg)
                binding.ectsAdd.imageTintList = ColorStateList.valueOf(colorAddTv)
                binding.ectsAddShrink.imageTintList = ColorStateList.valueOf(colorAddTv)

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessiaResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            val newIsChecked = (items[position].isChecked == true)
            items[position].isChecked = !newIsChecked
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size
}

