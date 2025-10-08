package com.example.scorochtenie2

import android.content.Context
import org.xmlpull.v1.XmlPullParser

data class TextData(
    val text: String,
    val questionsAndAnswers: List<Pair<String, List<String>>>
)

object TextResources {
    private var texts: Map<String, List<TextData>> = emptyMap()
    private var demoText: TextData? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) {
            return
        }
        
        try {
            val textsMap = mutableMapOf<String, MutableList<TextData>>()

            val techniqueFiles = mapOf(
                "Чтение по диагонали" to R.xml.diagonal_reading,
                "Чтение блоками" to R.xml.block_reading,
                "Предложения наоборот" to R.xml.reverse_sentences,
                "Слова наоборот" to R.xml.reverse_words,
                "Метод указки" to R.xml.pointer_method,
                "Частично скрытые строки" to R.xml.partially_hidden_lines,
                "Зашумленный текст" to R.xml.noisy_text
            )

            for ((techniqueName, xmlResourceId) in techniqueFiles) {
                try {
                    val parser = context.resources.getXml(xmlResourceId)
                    loadTechniqueFromParser(parser, techniqueName, textsMap)
                } catch (e: Exception) {
                }
            }

            try {
                val demoParser = context.resources.getXml(R.xml.demo_text)
                demoText = loadDemoText(demoParser)
            } catch (e: Exception) {
            }

            texts = textsMap

            
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
            texts = emptyMap()
            isInitialized = false
        }
    }

    private fun loadTechniqueFromParser(
        parser: XmlPullParser,
        techniqueName: String,
        textsMap: MutableMap<String, MutableList<TextData>>
    ) {
        var currentText: StringBuilder? = null
        var currentQuestions: MutableList<Pair<String, List<String>>>? = null
        var currentQuestionText: StringBuilder? = null
        var currentAnswers: MutableList<String>? = null

        if (!textsMap.containsKey(techniqueName)) {
            textsMap[techniqueName] = mutableListOf()
        }

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
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
                            if (currentText != null && currentQuestions != null) {
                                textsMap[techniqueName]?.add(
                                    TextData(
                                        text = currentText.toString(),
                                        questionsAndAnswers = currentQuestions.toList()
                                    )
                                )
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
    }

    private fun loadDemoText(parser: XmlPullParser): TextData? {
        var currentText: StringBuilder? = null
        var currentQuestions: MutableList<Pair<String, List<String>>>? = null
        var currentQuestionText: StringBuilder? = null
        var currentAnswers: MutableList<String>? = null

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
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
                            if (currentText != null && currentQuestions != null) {
                                return TextData(
                                    text = currentText.toString(),
                                    questionsAndAnswers = currentQuestions.toList()
                                )
                            }
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
        return null
    }

    fun getTexts(): Map<String, List<TextData>> = texts

    fun getDemoTextForTechnique(techniqueName: String): String {
        return demoText?.text ?: "Демонстрационный текст недоступен"
    }
}