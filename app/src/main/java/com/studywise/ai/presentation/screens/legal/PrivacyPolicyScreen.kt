package com.studywise.ai.presentation.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    onAccept: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    
    // Check if user has scrolled to bottom
    LaunchedEffect(scrollState.value) {
        if (!hasScrolledToBottom && scrollState.value >= scrollState.maxValue - 100) {
            hasScrolledToBottom = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (onAccept != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Decline")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            enabled = hasScrolledToBottom
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "STUDYWISE PRIVACY POLICY",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Effective Date: ${java.time.LocalDate.now()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Table of Contents
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Table of Contents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    privacyTableOfContents.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Privacy content sections
            privacyContent.forEach { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                section.content.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            
            // Contact Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Privacy Questions",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Email: privacy@studywise.ai",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Phone: 1-800-STUDYWISE",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Data Protection Officer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Email: dpo@studywise.ai",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Children's Privacy (COPPA)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Email: privacy-kids@studywise.ai",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (onAccept == null) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private val privacyTableOfContents = listOf(
    "1. Introduction",
    "2. Information We Collect",
    "3. How We Use Your Information",
    "4. Information Sharing and Disclosure",
    "5. Data Storage and Security",
    "6. Children's Privacy (COPPA)",
    "7. Your Rights and Choices",
    "8. Cookies and Tracking Technologies",
    "9. International Data Transfers",
    "10. Data Retention",
    "11. Changes to This Policy",
    "12. Contact Information"
)

private data class PrivacySection(
    val title: String,
    val content: List<String>
)

private val privacyContent = listOf(
    PrivacySection(
        title = "1. Introduction",
        content = listOf(
            "StudyWise AI, Inc. (\"StudyWise,\" \"we,\" \"us,\" or \"our\") is committed to protecting the privacy of our users. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use the StudyWise mobile application and related services (the \"Service\").",
            "This Policy applies to all users of our Service, including students, parents, teachers, and adult learners. We have special provisions for protecting children's privacy in compliance with applicable laws."
        )
    ),
    PrivacySection(
        title = "2. Information We Collect",
        content = listOf(
            "2.1 Information You Provide",
            "",
            "Account Information:",
            "• Full name",
            "• Email address",
            "• Password (encrypted)",
            "• User role (Student, Parent, Teacher, Adult)",
            "• Grade level (for students)",
            "• Date of birth",
            "• Profile picture (optional)",
            "",
            "Educational Information:",
            "• School name (optional)",
            "• Subject preferences",
            "• Learning goals",
            "• Parent-child relationships (for linked accounts)",
            "",
            "User Content:",
            "• Text entries for learning sessions",
            "• Photos of educational materials",
            "• Document uploads",
            "• Voice recordings",
            "• Responses to educational questions",
            "",
            "2.2 Information Collected Automatically",
            "",
            "Usage Data:",
            "• Learning session history",
            "• Time spent on activities",
            "• Feature usage patterns",
            "• Progress and achievement data",
            "• App performance metrics",
            "",
            "Device Information:",
            "• Device type and model",
            "• Operating system version",
            "• App version",
            "• Unique device identifiers",
            "• IP address",
            "• Time zone",
            "",
            "Analytics Data:",
            "• Crash reports",
            "• Error logs",
            "• Performance metrics",
            "• User interaction patterns",
            "",
            "2.3 Information from Third Parties",
            "• Authentication providers (if using social login)",
            "• School systems (for institutional accounts)",
            "• Payment processors (for premium features)"
        )
    ),
    PrivacySection(
        title = "3. How We Use Your Information",
        content = listOf(
            "3.1 Providing the Service",
            "• Create and manage user accounts",
            "• Generate personalized educational content",
            "• Track learning progress and achievements",
            "• Enable parent and teacher monitoring features",
            "• Provide offline functionality",
            "",
            "3.2 Improving the Service",
            "• Analyze usage patterns to enhance features",
            "• Develop new educational content",
            "• Optimize AI algorithms",
            "• Fix bugs and improve performance",
            "",
            "3.3 Communication",
            "• Send account-related notifications",
            "• Provide learning progress updates",
            "• Respond to support requests",
            "• Send optional educational tips (with consent)",
            "",
            "3.4 Safety and Security",
            "• Detect and prevent fraud",
            "• Monitor for inappropriate content",
            "• Ensure age-appropriate experiences",
            "• Comply with legal obligations",
            "",
            "3.5 AI Processing",
            "We use artificial intelligence to:",
            "• Generate grade-appropriate questions",
            "• Analyze uploaded content",
            "• Provide personalized learning recommendations",
            "• Create adaptive learning paths"
        )
    ),
    PrivacySection(
        title = "4. Information Sharing and Disclosure",
        content = listOf(
            "4.1 We Do Not Sell Personal Information",
            "We never sell, rent, or trade personal information to third parties for marketing purposes.",
            "",
            "4.2 Authorized Sharing",
            "We may share information with:",
            "",
            "Parents: Access to their children's learning data and progress",
            "",
            "Teachers: Access to their students' educational progress (with appropriate consent)",
            "",
            "Service Providers: Third parties who help us operate the Service, including:",
            "• Cloud storage providers (AWS)",
            "• Analytics services",
            "• Customer support tools",
            "• Payment processors",
            "",
            "4.3 Legal Requirements",
            "We may disclose information if required by:",
            "• Court orders or subpoenas",
            "• Law enforcement requests",
            "• Legal proceedings",
            "• To protect rights and safety",
            "",
            "4.4 Business Transfers",
            "In the event of a merger, acquisition, or sale of assets, user information may be transferred with appropriate notice.",
            "",
            "4.5 Aggregated Data",
            "We may share anonymized, aggregated data that cannot identify individuals for research or business purposes."
        )
    ),
    PrivacySection(
        title = "5. Data Storage and Security",
        content = listOf(
            "5.1 Security Measures",
            "We implement industry-standard security measures including:",
            "• Encryption of data in transit and at rest (AES-256)",
            "• Secure password hashing",
            "• Regular security audits",
            "• Access controls and authentication",
            "• Secure cloud infrastructure",
            "",
            "5.2 Data Centers",
            "Data is stored in secure data centers located in the United States with appropriate physical and technical safeguards.",
            "",
            "5.3 Incident Response",
            "We maintain incident response procedures and will notify affected users of any data breaches as required by law."
        )
    ),
    PrivacySection(
        title = "6. Children's Privacy (COPPA)",
        content = listOf(
            "6.1 Commitment to Children's Privacy",
            "We comply with the Children's Online Privacy Protection Act (COPPA) and are committed to protecting children's privacy.",
            "",
            "6.2 Parental Consent",
            "• We require verifiable parental consent before collecting personal information from children under 13",
            "• Parents can create and manage accounts for their children",
            "• Parents have full access to their children's information",
            "",
            "6.3 Information from Children",
            "We limit data collection from children to:",
            "• Educational interactions",
            "• Progress tracking",
            "• Safety monitoring",
            "• Information necessary for the Service",
            "",
            "6.4 Parental Rights",
            "Parents can:",
            "• Review their child's personal information",
            "• Request deletion of their child's data",
            "• Refuse further collection or use",
            "• Update their child's information",
            "",
            "6.5 Contact for Children's Privacy",
            "For questions about children's privacy, contact: privacy-kids@studywise.ai"
        )
    ),
    PrivacySection(
        title = "7. Your Rights and Choices",
        content = listOf(
            "7.1 Access and Portability",
            "You can access your personal information through your account settings or request a copy of your data.",
            "",
            "7.2 Update and Correction",
            "You can update your account information at any time through the app or by contacting support.",
            "",
            "7.3 Deletion",
            "You can request deletion of your account and personal information, subject to legal retention requirements.",
            "",
            "7.4 Marketing Communications",
            "You can opt-out of marketing emails through:",
            "• Unsubscribe links in emails",
            "• Account notification settings",
            "• Contacting support",
            "",
            "7.5 California Privacy Rights",
            "California residents have additional rights under CCPA, including:",
            "• Right to know what information is collected",
            "• Right to delete personal information",
            "• Right to opt-out of data sales (we do not sell data)",
            "• Right to non-discrimination",
            "",
            "7.6 European Privacy Rights",
            "If GDPR applies, you have rights including:",
            "• Right to access",
            "• Right to rectification",
            "• Right to erasure",
            "• Right to data portability",
            "• Right to object to processing",
            "• Right to withdraw consent"
        )
    ),
    PrivacySection(
        title = "8. Cookies and Tracking Technologies",
        content = listOf(
            "8.1 Mobile App Analytics",
            "We use analytics SDKs to understand app usage, including:",
            "• Session duration",
            "• Feature usage",
            "• Crash reporting",
            "• Performance monitoring",
            "",
            "8.2 Advertising",
            "We do not display third-party advertisements or use advertising tracking in our app.",
            "",
            "8.3 Do Not Track",
            "The app does not respond to Do Not Track signals as it is not applicable to mobile applications."
        )
    ),
    PrivacySection(
        title = "9. International Data Transfers",
        content = listOf(
            "9.1 Data Location",
            "Data is primarily stored and processed in the United States.",
            "",
            "9.2 International Users",
            "If you use the Service from outside the United States, you consent to transfer of your data to the United States.",
            "",
            "9.3 Data Protection",
            "We implement appropriate safeguards for international data transfers, including standard contractual clauses where required."
        )
    ),
    PrivacySection(
        title = "10. Data Retention",
        content = listOf(
            "10.1 Retention Periods",
            "• Active account data: Retained while account is active",
            "• Inactive accounts: Deleted after 2 years of inactivity",
            "• Educational progress: Retained for educational continuity",
            "• Support records: 3 years",
            "• Legal compliance data: As required by law",
            "",
            "10.2 Deletion",
            "Upon account deletion, we remove personal information within 30 days, except where retention is required for legal or legitimate business purposes.",
            "",
            "10.3 Anonymization",
            "Some data may be anonymized rather than deleted for educational research purposes."
        )
    ),
    PrivacySection(
        title = "11. Changes to This Policy",
        content = listOf(
            "11.1 Policy Updates",
            "We may update this Privacy Policy to reflect changes in:",
            "• Legal requirements",
            "• Our practices",
            "• Service features",
            "• Technology",
            "",
            "11.2 Notification",
            "We will notify you of material changes through:",
            "• In-app notifications",
            "• Email notifications",
            "• Prominent notice before changes take effect",
            "",
            "11.3 Consent",
            "Continued use of the Service after changes indicates acceptance of the updated Policy."
        )
    ),
    PrivacySection(
        title = "12. Your Privacy Matters",
        content = listOf(
            "At StudyWise, we believe that protecting your privacy is fundamental to building trust. We are committed to transparency about our data practices and giving you control over your information. Thank you for trusting us with your educational journey.",
            "",
            "Last Updated: ${java.time.LocalDate.now()}",
            "Version: 1.0"
        )
    )
)