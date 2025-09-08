package com.dertefter.neticlient.ui.dashboard.share_score_bottom_sheet

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.databinding.FragmentShareScoreBinding
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ShareScoreSheetFragment : BottomSheetDialogFragment() {


    companion object {
        const val TAG = "ShareScoreSheetFragment"
    }

    private var _binding: FragmentShareScoreBinding? = null
    private val binding get() = _binding!!

    lateinit var _dialog: BottomSheetDialog

    private val shareScoreViewModel: ShareScoreViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        _dialog = dialog
        return dialog
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareScoreBinding.inflate(inflater, container, false)
        return binding.root
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shareScoreViewModel.updateShareScore()

        binding.replaceLinkButton.setOnClickListener {
            shareScoreViewModel.replaceLink()
        }

        binding.loadFail.buttonRetry.setOnClickListener {
            shareScoreViewModel.updateShareScore()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                shareScoreViewModel.uiStateFlow.collect { uiState ->


                    when (uiState.responseType){
                        ResponseType.SUCCESS -> {
                            binding.skeleton.isGone = true
                            binding.loadFail.root.isGone = true
                            binding.success.isGone = false
                            val link = (uiState.data as String?)
                            if (!link.isNullOrEmpty()){
                                binding.linkField.editText?.setText(link)
                                setQr(link)

                                binding.copyButton.setOnClickListener {
                                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Поделиться успеваемостью", link)
                                    clipboard.setPrimaryClip(clip)
                                }

                            }
                        }
                        ResponseType.ERROR -> {
                            binding.skeleton.isGone = true
                            binding.loadFail.root.isGone = false
                            binding.success.isGone = true
                        }
                        ResponseType.LOADING -> {
                            binding.skeleton.isGone = false
                            binding.loadFail.root.isGone = true
                            binding.success.isGone = true
                        }
                    }

                }
            }
        }


    }

    fun setQr(link: String){
        val data = QrData.Url(link)
        val options = QrVectorOptions.Builder()
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape
                        .RoundCorners(.15f),
                    ball = QrVectorBallShape
                        .RoundCorners(.15f),
                    frame = QrVectorFrameShape
                        .RoundCorners(.15f),
                )
            )
            .setColors(
                QrVectorColors(
                    dark = QrVectorColor
                        .Solid(MaterialColors.getColor(binding.qrCodeView, com.google.android.material.R.attr.colorSecondary)),
                    ball = QrVectorColor.Solid(
                        MaterialColors.getColor(binding.qrCodeView, com.google.android.material.R.attr.colorSecondary)
                    ),
                    frame = QrVectorColor.Solid(
                        MaterialColors.getColor(binding.qrCodeView, com.google.android.material.R.attr.colorSecondary)
                    ),
                )
            )
            .build()
        val drawable : Drawable = QrCodeDrawable(data, options)
        binding.qrCodeView.setImageDrawable(drawable)
    }

}