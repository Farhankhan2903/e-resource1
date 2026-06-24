package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class BotMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Now"
)

class ChatbotViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<BotMessage>>(emptyList())
    val messages: StateFlow<List<BotMessage>> = _messages.asStateFlow()

    private val _suggestedQuestions = MutableStateFlow<List<String>>(emptyList())
    val suggestedQuestions: StateFlow<List<String>> = _suggestedQuestions.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Welcome greet
        _messages.value = listOf(
            BotMessage(
                text = "👋 Hello there! I'm your E-Resource Smart Assistant. \n\nI can help you troubleshoot home appliances, find technicians, or explain how our tool rental pool works. How can I assist you today?",
                isUser = false
            )
        )
        _suggestedQuestions.value = listOf(
            "Fridge not cooling",
            "Washing machine leaks",
            "Laptop won't turn on",
            "How do I book a repair?",
            "How to rent tools?"
        )
    }

    fun selectQuestion(question: String) {
        processQuery(question)
    }

    fun submitCustomQuestion(questionText: String) {
        val q = questionText.trim()
        if (q.isEmpty()) return
        processQuery(q)
    }

    private fun processQuery(query: String) {
        viewModelScope.launch {
            // 1. Add user message
            val currentWithUser = _messages.value.toMutableList()
            currentWithUser.add(BotMessage(text = query, isUser = true))
            _messages.value = currentWithUser

            // 2. Add "typing" simulation delay
            _isTyping.value = true
            delay(1200)
            _isTyping.value = false

            // 3. Generate and add bot response
            val answer = getAnswerForQuestion(query)
            val currentWithBot = _messages.value.toMutableList()
            currentWithBot.add(BotMessage(text = answer, isUser = false))
            _messages.value = currentWithBot
        }
    }

    private fun getAnswerForQuestion(query: String): String {
        val text = query.lowercase()
        return when {
            text.contains("fridge") || text.contains("cool") || text.contains("refrigerator") -> {
                "❄️ **Refrigerator Issues:** This is usually caused by dusty condenser coils or a faulty door seal. \n\n**Try this:** Clean the coils behind the unit and ensure the door closes tightly. If it's still warm, it might be the compressor or thermostat—I recommend booking an 'Electrician' from our home screen!"
            }
            text.contains("wash") || text.contains("leak") || text.contains("vibrat") -> {
                "💧 **Washing Machine Help:** Leaks are often due to loose hoses or a worn-out door gasket. \n\n**Tip:** Make sure the machine is perfectly level on the floor to stop vibrations. If you see water pooling, it's best to have one of our verified technicians take a look before it gets worse."
            }
            text.contains("laptop") || text.contains("computer") || text.contains("won't turn on") || text.contains("boot") -> {
                "💻 **Computer Troubleshooting:** If it won't start, try a 'hard reset' by holding the power button for 30 seconds without the charger. \n\nIf that doesn't work, it could be a power adapter or internal hardware issue. You can find specialized 'Computer' repair experts right here in the app!"
            }
            text.contains("book") || text.contains("repair") || text.contains("how do i") -> {
                "📅 **Booking a Repair:** It's super easy! \n\n1. Go to the **Home** tab.\n2. Pick your appliance type.\n3. Choose a verified technician.\n4. Set your preferred time and describe the problem.\n\nOnce they accept, you can chat with them directly!"
            }
            text.contains("rent") || text.contains("tool") || text.contains("resource") -> {
                "🔧 **Tool Rentals:** Our 'Shared Resource Pool' is a great way for technicians to get high-end diagnostic tools without buying them. \n\nTechnicians can rent things like multimeters or soldering stations by the hour. All billing is handled automatically through the app when tools are returned."
            }
            text.contains("kyc") || text.contains("worker") || text.contains("apply") -> {
                "📝 **Becoming a Technician:** We're always looking for pros! \n\nHead to your profile and tap **'Apply as Worker'**. You'll need to provide your shop details and a valid ID for verification. Once an Admin approves you, you'll start getting job requests!"
            }
            text.contains("hello") || text.contains("hi") -> {
                "👋 Hi! I'm here to help. You can ask me about appliance repairs, how to book a technician, or how to rent tools from our pool. What's on your mind?"
            }
            else -> {
                "🤔 I'm not quite sure about that specific issue, but I'm learning! \n\nYou might want to try our **AI Fault Diagnosis** tool on the home screen for a more technical analysis, or reach out to a verified technician directly for expert advice."
            }
        }
    }
}
