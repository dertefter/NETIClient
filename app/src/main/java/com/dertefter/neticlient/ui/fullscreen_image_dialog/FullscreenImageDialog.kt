package com.dertefter.neticlient.ui.fullscreen_image_dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.graphics.Bitmap
import androidx.fragment.app.DialogFragment
import com.dertefter.neticlient.R
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import com.dertefter.neticlient.ui.settings.SettingsViewModel

class FullscreenImageDialog : DialogFragment() {

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.ImageDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fullscreen_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<PhotoView>(R.id.imageView)
        val closeButton = view.findViewById<Button>(R.id.close)
        val imageUrl = arguments?.getString("image_url")
        val bitmap: Bitmap? = arguments?.getParcelable("bitmap")

        closeButton.setOnClickListener { dismiss() }

        imageUrl?.let {
            Picasso.get()
                .load(it)
                .fit()
                .centerInside()
                .into(imageView)
        }

        if (bitmap != null){
            imageView.setImageBitmap(bitmap)
        }
    }
}
