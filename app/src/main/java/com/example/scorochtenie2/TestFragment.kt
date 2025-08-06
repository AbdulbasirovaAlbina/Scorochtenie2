package com.example.scorochtenie2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import org.json.JSONObject

class TestFragment : Fragment() {

    companion object {
        private const val ARG_TEXT_INDEX = "textIndex"
        private const val ARG_TECHNIQUE_NAME = "techniqueName"
        private const val ARG_DURATION_PER_WORD = "durationPerWord"

        fun newInstance(textIndex: Int, techniqueName: String, durationPerWord: Long): TestFragment {
            return TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TEXT_INDEX, textIndex)
                    putString(ARG_TECHNIQUE_NAME, techniqueName)
                    putLong(ARG_DURATION_PER_WORD, durationPerWord)
                }
            }
        }
    }


    private var score = 0
    private var currentTextIndex = 0
    private var techniqueName: String = ""
    private var durationPerWord: Long = 400L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentTextIndex = arguments?.getInt(ARG_TEXT_INDEX, 0) ?: 0
        techniqueName = arguments?.getString(ARG_TECHNIQUE_NAME) ?: ""
        durationPerWord = arguments?.getLong(ARG_DURATION_PER_WORD) ?: 400L

        displayQuestion(0)

        view.findViewById<View>(R.id.btnSubmit).setOnClickListener {
            checkAnswer()
        }
    }

    private fun displayQuestion(index: Int) {
        val view = view ?: return
        
        val questions = when (techniqueName) {
            "Чтение по диагонали" -> TextResources.getDiagonalTexts().getOrNull(currentTextIndex)?.questionsAndAnswers
            else -> TextResources.getOtherTexts()[techniqueName]?.getOrNull(currentTextIndex)?.questionsAndAnswers
        }

        if (questions.isNullOrEmpty()) {
            Log.e("TestFragment", "No questions found for technique='$techniqueName', textIndex=$currentTextIndex")
            view.findViewById<View>(R.id.tv_question_header).visibility = View.GONE
            view.findViewById<TextView>(R.id.questionText).text = "Ошибка: вопросы для этой техники недоступны."
            view.findViewById<View>(R.id.radioGroup).visibility = View.GONE
            view.findViewById<View>(R.id.btnSubmit).visibility = View.GONE
            return
        }

        if (index < questions.size) {
            val questionPair = questions[index]
            view.findViewById<TextView>(R.id.questionText).text = questionPair.first
            view.findViewById<TextView>(R.id.questionText).tag = Pair(index, questionPair.second[0])

            view.findViewById<TextView>(R.id.tv_question_header).text = "Вопрос ${index + 1}"

            view.findViewById<RadioGroup>(R.id.radioGroup).removeAllViews()
            val options = questionPair.second.shuffled()
            options.forEach { option ->
                val radioButton = RadioButton(context).apply {
                    text = option
                    id = View.generateViewId()
                    textSize = 16f
                    setTextColor(context?.let { 
                        val typedValue = android.util.TypedValue()
                        it.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                        ContextCompat.getColor(it, typedValue.resourceId)
                    } ?: android.graphics.Color.WHITE)
                }
                view.findViewById<RadioGroup>(R.id.radioGroup).addView(radioButton)
            }
        } else {
            showResult()
        }
    }

    private fun checkAnswer() {
        val radioGroup = view?.findViewById<RadioGroup>(R.id.radioGroup)
        val selectedRadioButtonId = radioGroup?.checkedRadioButtonId ?: -1
        if (selectedRadioButtonId == -1) {
            Toast.makeText(context, "Выберите ответ", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = radioGroup?.findViewById<RadioButton>(selectedRadioButtonId)
        val userAnswer = selectedRadioButton?.text.toString().lowercase()
        val correctAnswer = (view?.findViewById<TextView>(R.id.questionText)?.tag as? Pair<*, *>)?.second as? String ?: ""
        if (userAnswer == correctAnswer.lowercase()) {
            score++
        }

        radioGroup?.clearCheck()
        displayQuestion(((view?.findViewById<TextView>(R.id.questionText)?.tag as? Pair<*, *>)?.first as? Int ?: 0) + 1)
    }

    private fun showResult() {
        val view = view ?: return
        val totalQuestions = when (techniqueName) {
            "Чтение по диагонали" -> TextResources.getDiagonalTexts().getOrNull(currentTextIndex)?.questionsAndAnswers?.size
            else -> TextResources.getOtherTexts()[techniqueName]?.getOrNull(currentTextIndex)?.questionsAndAnswers?.size
        } ?: 0

        view.findViewById<View>(R.id.tv_question_header).visibility = View.GONE
        view.findViewById<TextView>(R.id.questionText).text = "Тест завершён! Ваш результат: $score из $totalQuestions"
        view.findViewById<View>(R.id.radioGroup).visibility = View.GONE
        view.findViewById<View>(R.id.btnSubmit).visibility = View.GONE

        saveTestResult(techniqueName, totalQuestions)

        // Добавляем кнопку "Назад" для возврата в главное меню
        val backButton = view.findViewById<View>(R.id.btnBack)
        backButton?.visibility = View.VISIBLE
        backButton?.setOnClickListener {
            activity?.finish()
        }
    }

    private fun saveTestResult(techniqueName: String, totalQuestions: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val timestamp = System.currentTimeMillis()
        val key = "result_${techniqueName}_$timestamp"
        val resultJson = """
            {
                "techniqueName": "$techniqueName",
                "durationPerWord": $durationPerWord,
                "score": $score,
                "totalQuestions": $totalQuestions,
                "timestamp": $timestamp
            }
        """
        editor.putString(key, resultJson)
        editor.apply()
    }


} 