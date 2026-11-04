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
    
    // Ставим приятный синий цвет по умолчанию
    private var selectedColor: Int = Color.parseColor("#5486B8") 

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

        // Карта связи 16 ID квадратиков из XML с реальными цветами Material You
        val colorMap = mapOf(
            R.id.colorRed to Color.parseColor("#C25953"),
            R.id.colorCoral to Color.parseColor("#D97B66"),
            R.id.colorOrange to Color.parseColor("#D68A47"),
            R.id.colorGold to Color.parseColor("#C49B45"),
            R.id.colorOlive to Color.parseColor("#909B53"),
            R.id.colorSage to Color.parseColor("#6F9B73"),
            R.id.colorGreen to Color.parseColor("#50916E"),
            R.id.colorTeal to Color.parseColor("#409489"),
            R.id.colorCyan to Color.parseColor("#4F92A6"),
            R.id.colorBlue to Color.parseColor("#5486B8"),
            R.id.colorIndigo to Color.parseColor("#6A79BA"),
            R.id.colorPurple to Color.parseColor("#8B6EB8"),
            R.id.colorPlum to Color.parseColor("#A8679A"),
            R.id.colorPink to Color.parseColor("#C46487"),
            R.id.colorBrown to Color.parseColor("#8D7666"),
            R.id.colorSlate to Color.parseColor("#7A8994")
        )

        // Вешаем обработчик нажатий на каждый из 16 квадратиков
        for ((viewId, color) in colorMap) {
            view.findViewById<View>(viewId).setOnClickListener {
                selectedColor = color // Запоминаем цвет
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Настройка: ${file.name}")
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
