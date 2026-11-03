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

        // Новые спокойные пастельные (Material) цвета, приятные для светлой и темной темы
        val colorMap = mapOf(
            R.id.colorRed to Color.parseColor("#E57373"),     // Приглушенный красный
            R.id.colorCyan to Color.parseColor("#4DD0E1"),    // Спокойный бирюзовый
            R.id.colorBlue to Color.parseColor("#64B5F6"),    // Мягкий синий
            R.id.colorGreen to Color.parseColor("#81C784"),   // Пастельный зеленый
            R.id.colorMagenta to Color.parseColor("#F06292"), // Мягкий розовый
            R.id.colorYellow to Color.parseColor("#FFD54F"),  // Теплый желтый (не режущий глаз)
            R.id.colorOrange to Color.parseColor("#FFB74D"),  // Приглушенный оранжевый
            R.id.colorGray to Color.parseColor("#90A4AE")     // Сине-серый
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
