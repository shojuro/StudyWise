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
fun TermsOfServiceScreen(
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
                title = { Text("Terms of Service") },
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
                text = "STUDYWISE TERMS OF SERVICE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Last Updated: ${java.time.LocalDate.now()}",
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
                    tableOfContents.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Terms content sections
            termsContent.forEach { section ->
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
                        text = "StudyWise AI, Inc.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Email: legal@studywise.ai",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Phone: 1-800-STUDYWISE",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Support: support@studywise.ai",
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

private val tableOfContents = listOf(
    "1. Introduction and Acceptance",
    "2. Definitions",
    "3. Eligibility and Account Registration",
    "4. License Grant and Restrictions",
    "5. User Content and Conduct",
    "6. Intellectual Property Rights",
    "7. Privacy and Data Protection",
    "8. Payment Terms",
    "9. Third-Party Services",
    "10. Disclaimers and Limitations of Liability",
    "11. Indemnification",
    "12. Termination",
    "13. Modifications to Terms",
    "14. Dispute Resolution",
    "15. General Provisions",
    "16. Contact Information"
)

private data class TermsSection(
    val title: String,
    val content: List<String>
)

private val termsContent = listOf(
    TermsSection(
        title = "1. Introduction and Acceptance",
        content = listOf(
            "These Terms of Service (\"Terms\") constitute a legally binding agreement between you (\"User,\" \"you,\" or \"your\") and StudyWise AI, Inc. (\"StudyWise,\" \"we,\" \"us,\" or \"our\") regarding your use of the StudyWise mobile application and related services (collectively, the \"Service\").",
            "By downloading, installing, accessing, or using the Service, you acknowledge that you have read, understood, and agree to be bound by these Terms and our Privacy Policy. If you do not agree to these Terms, you must not use the Service."
        )
    ),
    TermsSection(
        title = "2. Definitions",
        content = listOf(
            "• \"Account\" means a registered user account on the Service",
            "• \"Content\" means any text, images, audio, video, or other materials",
            "• \"Educational Content\" means learning materials, questions, and responses generated by our AI system",
            "• \"Student User\" means a user registered with a student account",
            "• \"Parent User\" means a parent or legal guardian of a Student User",
            "• \"Teacher User\" means an educator using the Service for classroom purposes",
            "• \"Adult User\" means an adult learner using the Service independently",
            "• \"User Content\" means any Content uploaded, submitted, or created by users"
        )
    ),
    TermsSection(
        title = "3. Eligibility and Account Registration",
        content = listOf(
            "3.1 Age Requirements",
            "• Users under 13 years of age must have parental consent to use the Service",
            "• Parents must create and manage accounts for children under 13",
            "• Users between 13-17 must have parental permission",
            "• By registering, you confirm you meet these age requirements",
            "",
            "3.2 Account Types",
            "The Service offers four account types:",
            "• Student Accounts: For K-12 learners",
            "• Parent Accounts: For monitoring and managing child accounts",
            "• Teacher Accounts: For educational professionals",
            "• Adult Accounts: For independent adult learners",
            "",
            "3.3 Account Security",
            "• You are responsible for maintaining the confidentiality of your account credentials",
            "• You must notify us immediately of any unauthorized account use",
            "• You are responsible for all activities under your account",
            "• We reserve the right to suspend accounts that violate these Terms",
            "",
            "3.4 Accurate Information",
            "You agree to provide accurate, current, and complete information during registration and to update such information to maintain its accuracy."
        )
    ),
    TermsSection(
        title = "4. License Grant and Restrictions",
        content = listOf(
            "4.1 License to Use Service",
            "Subject to your compliance with these Terms, StudyWise grants you a limited, non-exclusive, non-transferable, revocable license to:",
            "• Download and install the app on authorized devices",
            "• Access and use the Service for personal, non-commercial educational purposes",
            "• Access Educational Content appropriate for your grade level or role",
            "",
            "4.2 License Restrictions",
            "You may not:",
            "• Modify, reverse engineer, or create derivative works of the Service",
            "• Use automated systems or software to extract data (\"scraping\")",
            "• Resell, redistribute, or sublicense the Service",
            "• Use the Service for any illegal or unauthorized purpose",
            "• Attempt to gain unauthorized access to any portion of the Service",
            "• Remove or alter any proprietary notices",
            "",
            "4.3 Educational Use",
            "Teachers may use the Service for classroom instruction provided they comply with their school's policies and these Terms."
        )
    ),
    TermsSection(
        title = "5. User Content and Conduct",
        content = listOf(
            "5.1 Your Content",
            "You retain ownership of User Content you submit. By uploading User Content, you grant StudyWise a worldwide, non-exclusive, royalty-free license to use, process, and display such content solely to provide the Service.",
            "",
            "5.2 Content Guidelines",
            "User Content must not:",
            "• Violate any laws or regulations",
            "• Infringe intellectual property rights",
            "• Contain harmful, offensive, or inappropriate material",
            "• Include personal information of others without consent",
            "• Contain malware or malicious code",
            "• Misrepresent your identity or affiliation",
            "",
            "5.3 AI Processing",
            "You acknowledge that User Content may be processed by AI to generate Educational Content. This processing is performed in accordance with our Privacy Policy.",
            "",
            "5.4 Monitoring",
            "We reserve the right to monitor User Content for safety and compliance, particularly for Student Users."
        )
    ),
    TermsSection(
        title = "6. Intellectual Property Rights",
        content = listOf(
            "6.1 StudyWise Property",
            "The Service, including all Educational Content, software, designs, and trademarks, is owned by StudyWise and protected by intellectual property laws.",
            "",
            "6.2 Educational Content License",
            "Educational Content generated by our AI is licensed to you for personal educational use only. You may not redistribute or commercialize this content.",
            "",
            "6.3 Feedback",
            "Any feedback, suggestions, or ideas you provide become the property of StudyWise without compensation to you.",
            "",
            "6.4 Copyright Claims",
            "If you believe content infringes your copyright, please contact us with:",
            "• Identification of the copyrighted work",
            "• Identification of the infringing material",
            "• Your contact information",
            "• A statement of good faith belief",
            "• A statement of accuracy under penalty of perjury"
        )
    ),
    TermsSection(
        title = "7. Privacy and Data Protection",
        content = listOf(
            "7.1 Privacy Policy",
            "Your use of the Service is subject to our Privacy Policy, which is incorporated into these Terms by reference.",
            "",
            "7.2 COPPA Compliance",
            "We comply with the Children's Online Privacy Protection Act (COPPA). We obtain verifiable parental consent before collecting personal information from children under 13.",
            "",
            "7.3 Educational Records",
            "For school users, we comply with FERPA regarding educational records. Schools are responsible for obtaining necessary consents."
        )
    ),
    TermsSection(
        title = "8. Payment Terms",
        content = listOf(
            "8.1 Free and Premium Features",
            "The Service offers both free and premium features. Premium subscriptions may be available for enhanced functionality.",
            "",
            "8.2 Subscription Terms",
            "• Prices are subject to change with notice",
            "• Subscriptions auto-renew unless cancelled",
            "• Refunds are subject to our refund policy",
            "• Taxes may apply based on your location",
            "",
            "8.3 Payment Processing",
            "Payments are processed through third-party providers. You agree to their terms when making purchases."
        )
    ),
    TermsSection(
        title = "9. Third-Party Services",
        content = listOf(
            "9.1 Third-Party Integration",
            "The Service may integrate with third-party services. Your use of such services is subject to their terms.",
            "",
            "9.2 Third-Party Content",
            "We are not responsible for third-party content accessed through the Service.",
            "",
            "9.3 Links",
            "The Service may contain links to third-party websites. We are not responsible for their content or practices."
        )
    ),
    TermsSection(
        title = "10. Disclaimers and Limitations of Liability",
        content = listOf(
            "10.1 Service \"As Is\"",
            "THE SERVICE IS PROVIDED \"AS IS\" AND \"AS AVAILABLE\" WITHOUT WARRANTIES OF ANY KIND, EXPRESS OR IMPLIED.",
            "",
            "10.2 Educational Disclaimer",
            "The Service is an educational supplement and should not replace professional instruction or evaluation.",
            "",
            "10.3 Limitation of Liability",
            "TO THE MAXIMUM EXTENT PERMITTED BY LAW, STUDYWISE SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES.",
            "",
            "10.4 Liability Cap",
            "OUR TOTAL LIABILITY SHALL NOT EXCEED THE AMOUNT YOU PAID US IN THE TWELVE MONTHS BEFORE THE CLAIM."
        )
    ),
    TermsSection(
        title = "11. Indemnification",
        content = listOf(
            "You agree to indemnify and hold harmless StudyWise from any claims, damages, or expenses arising from:",
            "• Your violation of these Terms",
            "• Your User Content",
            "• Your use of the Service",
            "• Your violation of any rights of another party"
        )
    ),
    TermsSection(
        title = "12. Termination",
        content = listOf(
            "12.1 Termination by You",
            "You may terminate your account at any time through account settings or by contacting support.",
            "",
            "12.2 Termination by StudyWise",
            "We may suspend or terminate your account for:",
            "• Violation of these Terms",
            "• Extended inactivity",
            "• Request by authorized school officials (for school accounts)",
            "• Legal requirements",
            "",
            "12.3 Effect of Termination",
            "Upon termination:",
            "• Your license to use the Service ends",
            "• You must cease all use of the Service",
            "• We may delete your account data per our data retention policy"
        )
    ),
    TermsSection(
        title = "13. Modifications to Terms",
        content = listOf(
            "We may modify these Terms at any time. We will notify you of material changes via:",
            "• In-app notifications",
            "• Email to your registered address",
            "• Prominent notice on our website",
            "",
            "Continued use after changes constitutes acceptance of modified Terms."
        )
    ),
    TermsSection(
        title = "14. Dispute Resolution",
        content = listOf(
            "14.1 Informal Resolution",
            "Before filing a claim, you agree to try to resolve disputes informally by contacting us.",
            "",
            "14.2 Arbitration",
            "Any disputes not resolved informally shall be resolved through binding arbitration under AAA rules, except you may pursue claims in small claims court.",
            "",
            "14.3 Class Action Waiver",
            "You waive any right to participate in class actions against StudyWise.",
            "",
            "14.4 Governing Law",
            "These Terms are governed by the laws of Delaware, USA, without regard to conflict of law principles."
        )
    ),
    TermsSection(
        title = "15. General Provisions",
        content = listOf(
            "15.1 Entire Agreement",
            "These Terms and the Privacy Policy constitute the entire agreement between you and StudyWise.",
            "",
            "15.2 Severability",
            "If any provision is found unenforceable, the remaining provisions continue in effect.",
            "",
            "15.3 No Waiver",
            "Our failure to enforce any right or provision is not a waiver of that right or provision.",
            "",
            "15.4 Assignment",
            "We may assign these Terms. You may not assign them without our written consent.",
            "",
            "15.5 International Use",
            "The Service is controlled from the United States. We make no representations about appropriateness for use in other locations."
        )
    )
)