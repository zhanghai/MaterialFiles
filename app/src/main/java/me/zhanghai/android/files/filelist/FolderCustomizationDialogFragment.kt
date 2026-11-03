package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem

class FolderCustomizationDialogFragment : DialogFragment() {

    private lateinit var file: FileItem

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
        val colorEditText = view.findViewById<EditText>(R.id.colorEditText)

        // Загружаем текущие настройки, если они уже есть
        val existing = FolderCustomizationStore.getCustomization(file.path.toString())
        if (existing != null) {
            emojiEditText.setText(existing.emoji)
            // Преобразуем Int цвет обратно в HEX строку (например, #FF5252)
            colorEditText.setText(String.format("#%06X", 0xFFFFFF and existing.color))
        } else {
            colorEditText.setText("#4285F4") // Синий по умолчанию
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Настройка папки ${file.name}")
            .setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                val emoji = emojiEditText.text.toString().trim()
                val colorHex = colorEditText.text.toString().trim()
                
                try {
                    val colorInt = Color.parseColor(colorHex)
                    // Сохраняем в нашу базу!
                    FolderCustomizationStore.saveCustomization(file.path.toString(), colorInt, emoji)
                    
                    // Обновляем список, чтобы изменения сразу отобразились
                    (parentFragment as? FileListFragment)?.let { fragment ->
                        // Здесь мы вызываем перезагрузку списка файлов
                        fragment.requireActivity().recreate() // Грубый, но надежный метод для теста
                    }
                } catch (e: Exception) {
                    // Если ввели неправильный цвет
                }
            }
            .setNeutralButton("Сбросить") { _, _ ->
                FolderCustomizationStore.removeCustomization(file.path.toString())
                (parentFragment as? FileListFragment)?.requireActivity()?.recreate()
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}
