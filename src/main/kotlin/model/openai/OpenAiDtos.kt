package io.github.krisalord.model.openai

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiChoiceMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiChoiceMessage
)

@Serializable
data class OpenAiResponse(
    val choices: List<OpenAiChoice>
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>
)