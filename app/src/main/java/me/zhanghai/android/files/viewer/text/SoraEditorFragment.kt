package me.zhanghai.android.files.viewer.text

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import java8.nio.file.Files
import java8.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.SoraEditorFragmentBinding
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.theme.night.NightModeHelper
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.showToast
import java.io.IOException
import java.nio.charset.StandardCharsets

class SoraEditorFragment : Fragment(), ConfirmReloadDialogFragment.Listener,
    ConfirmCloseDialogFragment.Listener {
    private val args by args<Args>()
    private lateinit var argsFile: Path

    private lateinit var binding: SoraEditorFragmentBinding
    private lateinit var codeEditor: CodeEditor

    private var fileContents: String? = null
    private var errorMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val argsFile = args.intent.extraPath
        if (argsFile == null) {
            showToast(getString(R.string.activity_not_found))
            return finish()
        }

        this.argsFile = argsFile
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = SoraEditorFragmentBinding.inflate(inflater, container, false).also {
        binding = it
        codeEditor = it.codeEditor
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.lifecycleScope.launchWhenCreated {
            activity.setSupportActionBar(binding.toolbar)
            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        codeEditor.setEditorLanguage(JavaLanguage())
        codeEditor.colorScheme =
            if (NightModeHelper.isInNightMode(activity)) SchemeDarcula() else EditorColorScheme()


        updateTitle() //TODO: Call this method when text is edited (How to detect??)
        setupMenu()
        reload()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        codeEditor.colorScheme =
            if (NightModeHelper.isInNightMode(activity as AppCompatActivity)) SchemeDarcula() else EditorColorScheme()
        codeEditor.invalidate()
        //TODO: Update toolbar color scheme on dark/light mode toggle
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.sora_editor, menu)
                menu.findItem(R.id.action_word_warp).isChecked = codeEditor.isWordwrap
                menu.findItem(R.id.action_syntax_highlight).isChecked =
                    codeEditor.editorLanguage is JavaLanguage
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean =
                when (item.itemId) {
                    R.id.action_save -> {
                        if (fileContents != null && errorMessage == null)
                            save()
                        true
                    }
                    R.id.action_word_warp -> {
                        codeEditor.isWordwrap = !codeEditor.isWordwrap
                        item.isChecked = codeEditor.isWordwrap
                        true
                    }
                    R.id.action_syntax_highlight -> {
                        codeEditor.setEditorLanguage(
                            if (codeEditor.editorLanguage is JavaLanguage) null else JavaLanguage()
                        )
                        item.isChecked = codeEditor.editorLanguage is JavaLanguage
                        true
                    }
                    R.id.action_reload -> {
                        onReload()
                        true
                    }
                    else -> false
                }
        })
    }


    fun onFinish(): Boolean {
        if (textChanged()) {
            ConfirmCloseDialogFragment.show(this)
            return true
        }
        return false
    }

    override fun finish() {
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        codeEditor.release()
    }

    private fun updateTitle() {
        val fileName = argsFile.fileName
        requireActivity().title = getString(
            if (textChanged()) {
                R.string.text_editor_title_changed_format
            } else {
                R.string.text_editor_title_format
            }, fileName
        )
    }

    private fun onReload() {
        if (binding.progress.isVisible) return
        if (textChanged()) {
            ConfirmReloadDialogFragment.show(this)
        } else reload()
    }

    override fun reload() {

        binding.progress.visibility = View.VISIBLE
        binding.codeEditor.visibility = View.GONE
        binding.errorText.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            fileContents = readFile(argsFile)

            launch(Dispatchers.Main) {
                binding.progress.visibility = View.GONE
                if (fileContents == null && errorMessage != null) {
                    binding.errorText.text = errorMessage
                    binding.errorText.visibility = View.VISIBLE
                    codeEditor.visibility = View.GONE
                } else {
                    codeEditor.visibility = View.VISIBLE
                    codeEditor.setText(fileContents)
                }
            }
        }
    }

    private fun save() {
        val text = codeEditor.text.toString()
        val bytes = text.toByteArray(StandardCharsets.UTF_8)

        FileJobService.write(argsFile, bytes, requireContext()) { success ->
            if (success) {
                fileContents = text
                showToast(getString(R.string.text_editor_save_success))
            }
        }
    }

    private fun readFile(file: Path): String? {
        return try {
            errorMessage = null
            String(Files.readAllBytes(file))
        } catch (err: Throwable) {
            if (err !is OutOfMemoryError && err !is IOException) throw err
            errorMessage = err.localizedMessage
            null
        }
    }

    private fun textChanged() =
        errorMessage == null && fileContents != null && codeEditor.text.toString() != fileContents


    @Parcelize
    class Args(val intent: Intent) : ParcelableArgs

}
