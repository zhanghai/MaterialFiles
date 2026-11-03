package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem

class FolderCustomizationDialogFragment : DialogFragment() {

    private lateinit var file: FileItem
    
    // По умолчанию ставим стандартный синий цвет папки
    private var selectedColor: Int = Color.parseColor("#4285F4") 

    companion object {
        private const val ARG_FILE = "file"

        fun show(file: FileItem, fragment: FileListFragment) {
            val dialog = FolderCustomizationDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FILE, file)
                }
            }
            dialog.show(fragment.childFragmentManager, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        file = requireArguments().getParcelable(ARG_FILE)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_customize_folder, null)
        val emojiEditText = view.findViewById<EditText>(R.id.emojiEditText)

        // Загружаем текущие настройки
        val existing = FolderCustomizationStore.getCustomization(file.path.toString())
        if (existing != null) {
            emojiEditText.setText(existing.emoji)
            selectedColor = existing.color
        }

        // Карта связи ID квадратиков из XML с реальными цветами
        val colorMap = mapOf(
            R.id.colorRed to Color.parseColor("#F44336"),
            R.id.colorCyan to Color.parseColor("#00BCD4"),
            R.id.colorBlue to Color.parseColor("#2196F3"),
            R.id.colorGreen to Color.parseColor("#4CAF50"),
            R.id.colorMagenta to Color.parseColor("#E91E63"),
            R.id.colorYellow to Color.parseColor("#FFEB3B"),
            R.id.colorOrange to Color.parseColor("#FF9800"),
            R.id.colorGray to Color.parseColor("#9E9E9E")
        )

        // Вешаем обработчик нажатий на каждый квадратик
        for ((viewId, color) in colorMap) {
            view.findViewById<View>(viewId).setOnClickListener {
                selectedColor = color // Запоминаем цвет, на который нажал пользователь
                // TODO: Позже можно добавить белую рамку вокруг выбранного цвета для красоты
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Настройка папки ${file.name}")
            .setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                val emoji = emojiEditText.text.toString().trim()
                FolderCustomizationStore.saveCustomization(file.path.toString(), selectedColor, emoji)
                
                (parentFragment as? FileListFragment)?.requireActivity()?.recreate()
            }
            .setNeutralButton("Сбросить") { _, _ ->
                FolderCustomizationStore.removeCustomization(file.path.toString())
                (parentFragment as? FileListFragment)?.requireActivity()?.recreate()
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}
