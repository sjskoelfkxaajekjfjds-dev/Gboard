package com.mygboard.custom.app

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class GboardService : InputMethodService() {

    private val inputBuffer = StringBuilder()

    // قائمة الاختصارات السريعة (يمكنك التعديل عليها وإضافة ما تريد)
    private val shortcuts = mapOf(
        "سلا" to "السلام عليكم ورحمة الله وبركاته",
        "11" to "0500000000",
        "😂2" to "هههههههههههههههههه",
        "شك" to "شكراً جزيلاً"
    )

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1C1C1E.toInt())
            setPadding(8, 16, 8, 16)
        }

        // صف الحروف/المفاتيح الأول
        val row1 = createRow(listOf("س", "ل", "ا", "ش", "ك", "1", "😂"))
        // صف المفاتيح الخاص بالتحكم
        val rowControl = createControlRow()

        layout.addView(row1)
        layout.addView(rowControl)

        return layout
    }

    private fun createRow(keys: List<String>): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = keys.size.toFloat()
        }

        for (key in keys) {
            val btn = Button(this).apply {
                text = key
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundResource(R.drawable.btn_key_bg)
                val params = LinearLayout.LayoutParams(0, 140, 1f).apply {
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params

                setOnClickListener {
                    handleKeyPress(key)
                }
            }
            row.addView(btn)
        }
        return row
    }

    private fun createControlRow(): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnDelete = Button(this).apply {
            text = "حذف"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundResource(R.drawable.btn_key_bg)
            layoutParams = LinearLayout.LayoutParams(0, 140, 1f).apply { setMargins(4, 4, 4, 4) }
            setOnClickListener {
                if (inputBuffer.isNotEmpty()) inputBuffer.deleteCharAt(inputBuffer.length - 1)
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
        }

        val btnSpace = Button(this).apply {
            text = "مسافة"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundResource(R.drawable.btn_key_bg)
            layoutParams = LinearLayout.LayoutParams(0, 140, 2f).apply { setMargins(4, 4, 4, 4) }
            setOnClickListener {
                checkAndApplyShortcut()
                currentInputConnection?.commitText(" ", 1)
                inputBuffer.clear()
            }
        }

        row.addView(btnDelete)
        row.addView(btnSpace)
        return row
    }

    private fun handleKeyPress(key: String) {
        inputBuffer.append(key)
        currentInputConnection?.commitText(key, 1)

        // فحص الاختصارات المباشرة أثناء الكتابة
        val currentText = inputBuffer.toString()
        if (shortcuts.containsKey(currentText)) {
            val fullText = shortcuts[currentText]!!
            currentInputConnection?.deleteSurroundingText(currentText.length, 0)
            currentInputConnection?.commitText(fullText, 1)
            inputBuffer.clear()
        }
    }

    private fun checkAndApplyShortcut() {
        val currentText = inputBuffer.toString()
        if (shortcuts.containsKey(currentText)) {
            val fullText = shortcuts[currentText]!!
            currentInputConnection?.deleteSurroundingText(currentText.length, 0)
            currentInputConnection?.commitText(fullText, 1)
        }
    }
}
