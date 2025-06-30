package com.studywise.ai.presentation.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit
) {
    var expandedSection by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Contact Support Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.HeadsetMic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Need Help?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Our support team is here to help",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { /* TODO: Open email */ }) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email Us")
                        }
                        OutlinedButton(onClick = { /* TODO: Open chat */ }) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Chat")
                        }
                    }
                }
            }
            
            // FAQ Section
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            FAQItem(
                question = "Getting Started",
                answer = "Welcome to StudyWise! Here's how to get started:\n\n" +
                        "1. Create your account (Student, Parent, Teacher, or Adult)\n" +
                        "2. Complete your profile with grade level\n" +
                        "3. Choose a subject to study\n" +
                        "4. Input your study material (text, photo, document, or voice)\n" +
                        "5. Answer AI-generated questions to learn!\n\n" +
                        "The app uses the Socratic method to help you learn through guided questioning.",
                isExpanded = expandedSection == "getting_started",
                onToggle = { expandedSection = if (expandedSection == "getting_started") null else "getting_started" }
            )
            
            FAQItem(
                question = "How does the AI questioning work?",
                answer = "StudyWise uses advanced AI to:\n\n" +
                        "• Analyze your uploaded content\n" +
                        "• Generate grade-appropriate questions\n" +
                        "• Guide you to discover answers yourself\n" +
                        "• Provide hints when needed\n" +
                        "• Track your progress and adapt difficulty\n\n" +
                        "Questions are designed to promote critical thinking rather than rote memorization.",
                isExpanded = expandedSection == "ai_questioning",
                onToggle = { expandedSection = if (expandedSection == "ai_questioning") null else "ai_questioning" }
            )
            
            FAQItem(
                question = "Input Methods Explained",
                answer = "You can input study material in 4 ways:\n\n" +
                        "📝 Text: Type or paste text directly\n" +
                        "📷 Photo: Take a photo of book pages\n" +
                        "📄 Document: Upload PDFs or documents\n" +
                        "🎤 Voice: Read text aloud\n\n" +
                        "Tips:\n" +
                        "• Ensure good lighting for photos\n" +
                        "• Speak clearly for voice input\n" +
                        "• Minimum 50 characters required",
                isExpanded = expandedSection == "input_methods",
                onToggle = { expandedSection = if (expandedSection == "input_methods") null else "input_methods" }
            )
            
            FAQItem(
                question = "Progress and Achievements",
                answer = "Track your learning journey:\n\n" +
                        "• Daily streaks for consistent practice\n" +
                        "• Points for correct answers\n" +
                        "• Skill mastery percentages\n" +
                        "• Achievement badges\n" +
                        "• Weekly progress reports\n\n" +
                        "Parents and teachers can view student progress in their dashboards.",
                isExpanded = expandedSection == "progress",
                onToggle = { expandedSection = if (expandedSection == "progress") null else "progress" }
            )
            
            FAQItem(
                question = "Privacy and Safety",
                answer = "We take privacy seriously:\n\n" +
                        "• COPPA compliant for users under 13\n" +
                        "• Encrypted data storage\n" +
                        "• No ads or data selling\n" +
                        "• Parental controls available\n" +
                        "• Content filtering for safety\n\n" +
                        "See our Privacy Policy for full details.",
                isExpanded = expandedSection == "privacy",
                onToggle = { expandedSection = if (expandedSection == "privacy") null else "privacy" }
            )
            
            FAQItem(
                question = "Troubleshooting Common Issues",
                answer = "Common solutions:\n\n" +
                        "📷 Photo not clear?\n" +
                        "→ Ensure good lighting and steady hands\n\n" +
                        "🎤 Voice not working?\n" +
                        "→ Check microphone permissions\n\n" +
                        "📱 App crashing?\n" +
                        "→ Update to latest version\n\n" +
                        "🔄 Progress not syncing?\n" +
                        "→ Check internet connection\n\n" +
                        "For other issues, contact support.",
                isExpanded = expandedSection == "troubleshooting",
                onToggle = { expandedSection = if (expandedSection == "troubleshooting") null else "troubleshooting" }
            )
            
            // Quick Links
            Text(
                text = "Quick Links",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            
            QuickLinkItem(
                icon = Icons.Default.Book,
                title = "User Guide",
                description = "Comprehensive guide to all features"
            )
            
            QuickLinkItem(
                icon = Icons.Default.VideoLibrary,
                title = "Video Tutorials",
                description = "Watch how-to videos"
            )
            
            QuickLinkItem(
                icon = Icons.Default.Forum,
                title = "Community Forum",
                description = "Connect with other users"
            )
            
            QuickLinkItem(
                icon = Icons.Default.BugReport,
                title = "Report a Bug",
                description = "Help us improve the app"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FAQItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onToggle
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickLinkItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { /* TODO: Implement navigation */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}