package com.example.scorochtenie2

import android.content.Context
import com.example.scorochtenie2.R
import org.xmlpull.v1.XmlPullParser

data class TextData(
    val text: String,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

data class DiagonalTextData(
    val text: String,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

object TextResources {
    private var diagonalTexts: List<DiagonalTextData> = emptyList()
    private var otherTexts: Map<String, List<TextData>> = emptyMap()

    fun initialize(context: Context) {
        try {
            val parser = context.resources.getXml(R.xml.texts)
            val diagonalList = mutableListOf<DiagonalTextData>()
            val otherMap = mutableMapOf<String, MutableList<TextData>>()

            var currentTechnique: String? = null
            var currentText: StringBuilder? = null
            var currentQuestions: MutableList<Pair<String, List<String>>>? = null
            var currentQuestionText: StringBuilder? = null
            var currentAnswers: MutableList<String>? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "technique" -> {
                                currentTechnique = parser.getAttributeValue(null, "name")
                                otherMap[currentTechnique] = mutableListOf()
                            }
                            "text" -> {
                                currentText = StringBuilder()
                                currentQuestions = mutableListOf()
                            }
                            "content" -> {
                                currentText?.append(parser.nextText().trim())
                            }
                            "questions" -> {
                                currentQuestions = mutableListOf()
                            }
                            "question" -> {
                                currentQuestionText = StringBuilder(parser.getAttributeValue(null, "text"))
                                currentAnswers = mutableListOf()
                            }
                            "answer" -> {
                                val answer = parser.nextText().trim()
                                currentAnswers?.add(answer)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "text" -> {
                                if (currentTechnique != null && currentText != null && currentQuestions != null) {
                                    when (currentTechnique) {
                                        "Чтение по диагонали" -> {
                                            diagonalList.add(
                                                DiagonalTextData(
                                                    text = currentText.toString(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                        }
                                        else -> {
                                            otherMap[currentTechnique]?.add(
                                                TextData(
                                                    text = currentText.toString(),
                                                    questionsAndAnswers = currentQuestions.toList()
                                                )
                                            )
                                        }
                                    }
                                }
                                currentText = null
                                currentQuestions = null
                            }
                            "question" -> {
                                if (currentQuestionText != null && currentAnswers != null) {
                                    currentQuestions?.add(
                                        currentQuestionText.toString() to currentAnswers.toList()
                                    )
                                }
                                currentQuestionText = null
                                currentAnswers = null
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            diagonalTexts = diagonalList
            otherTexts = otherMap
        } catch (e: Exception) {
            e.printStackTrace()
            diagonalTexts = emptyList()
            otherTexts = emptyMap()
        }
    }

    fun getDiagonalTexts(): List<DiagonalTextData> = diagonalTexts
    fun getOtherTexts(): Map<String, List<TextData>> = otherTexts
}