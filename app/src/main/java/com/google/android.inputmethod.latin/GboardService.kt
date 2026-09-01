package com.google.android.inputmethod.latin

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import kotlin.random.Random

class GboardService : InputMethodService() {

    private var typingHandler: Handler? = Handler(Looper.getMainLooper())
    private var isTypingActive = false
    private var currentRunnable: Runnable? = null

    private lateinit var mainKeyboardView: View
    private lateinit var layoutLetters: View
    private lateinit var layoutSymbols: View

    override fun onCreateInputView(): View {
        mainKeyboardView = layoutInflater.inflate(R.layout.keyboard_main, null)
        
        setupViews()
        setupTypingEngine()
        
        return mainKeyboardView
    }

    private fun setupViews() {
        layoutLetters = mainKeyboardView.findViewById(R.id.layout_letters)
        layoutSymbols = mainKeyboardView.findViewById(R.id.layout_symbols)

        mainKeyboardView.findViewById<Button>(R.id.btn_switch_symbols)?.setOnClickListener {
            switchToSymbols()
        }

        mainKeyboardView.findViewById<Button>(R.id.btn_switch_letters)?.setOnClickListener {
            switchToLetters()
        }

        mainKeyboardView.findViewById<Button>(R.id.btn_backspace)?.setOnClickListener {
            stopAutoTyping()
            currentInputConnection?.deleteSurroundingText(1, 0)
        }

        mainKeyboardView.findViewById<Button>(R.id.btn_space)?.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }

        mainKeyboardView.findViewById<Button>(R.id.btn_enter)?.setOnClickListener {
            currentInputConnection?.sendKeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)
            )
        }

        mainKeyboardView.findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setupTypingEngine() {
        val btnAutoType = mainKeyboardView.findViewById<View>(R.id.btn_auto_type)
        btnAutoType?.setOnClickListener {
            val sampleText = "السلام عليكم ورحمة الله 123! هذه تجربة الكتابة السريعة."
            startHumanTypingProcess(sampleText)
        }
    }

    private fun startHumanTypingProcess(text: String) {
        stopAutoTyping()
        isTypingActive = true
        var index = 0
        val inputConnection = currentInputConnection ?: return

        currentRunnable = object : Runnable {
            override fun run() {
                if (!isTypingActive || index >= text.length) {
                    if (index >= text.length && isTypingActive) {
                        typingHandler?.postDelayed({ startFastDeletionProcess(text.length) }, 500)
                    }
                    return
                }

                val char = text[index]

                if (char.isDigit() || "!@#$%^&*()_+-=[]{}|;:'\",.<>?/؟".contains(char)) {
                    switchToSymbols()
                } else {
                    switchToLetters()
                }

                inputConnection.commitText(char.toString(), 1)
                index++

                val randomSpeed = Random.nextLong(70, 160)
                typingHandler?.postDelayed(this, randomSpeed)
            }
        }

        typingHandler?.post(currentRunnable!!)
    }

    private fun startFastDeletionProcess(length: Int) {
        var count = 0
        val inputConnection = currentInputConnection ?: return

        currentRunnable = object : Runnable {
            override fun run() {
                if (!isTypingActive || count >= length) {
                    stopAutoTyping()
                    return
                }

                inputConnection.deleteSurroundingText(1, 0)
                count++
                typingHandler?.postDelayed(this, 40)
            }
        }

        typingHandler?.post(currentRunnable!!)
    }

    private fun stopAutoTyping() {
        isTypingActive = false
        currentRunnable?.let { typingHandler?.removeCallbacks(it) }
        currentRunnable = null
    }

    private fun switchToSymbols() {
        layoutLetters.visibility = View.GONE
        layoutSymbols.visibility = View.VISIBLE
    }

    private fun switchToLetters() {
        layoutSymbols.visibility = View.GONE
        layoutLetters.visibility = View.VISIBLE
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        stopAutoTyping()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAutoTyping()
        typingHandler = null
    }
}
