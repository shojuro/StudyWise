# OpenAI API Setup Guide

## Prerequisites
- OpenAI account with API access
- API key from https://platform.openai.com/api-keys

## Setup Instructions

### Method 1: Using local.properties (Recommended for Development)

1. Open or create `local.properties` file in the project root directory
2. Add your API key:
   ```
   OPENAI_API_KEY=sk-your-actual-api-key-here
   ```
3. This file is already in `.gitignore` so it won't be committed

### Method 2: Using Environment Variable

1. Set the environment variable before building:
   ```bash
   export OPENAI_API_KEY="sk-your-actual-api-key-here"
   ./gradlew assembleDebug
   ```

### Method 3: For Production Builds

For production builds, consider using:
- Android Keystore for secure key storage
- Server-side proxy to protect the API key
- Environment-specific build configurations

## Verifying Setup

After adding your API key, rebuild the project:
```bash
./gradlew clean assembleDebug
```

## API Features Using OpenAI

The StudyWise app uses OpenAI API for:

1. **Object Identification Enhancement**: Provides educational context for identified objects
2. **Grade-Appropriate Sentences**: Generates example sentences for grades 2-12
3. **Socratic Lessons**: Creates interactive 5-minute lessons using the Socratic method
4. **Voice Transcription**: Converts student voice responses to text (Whisper API)
5. **Text-to-Speech**: Converts AI responses to natural speech

## Troubleshooting

### API Key Not Found
If you see "YOUR_API_KEY_HERE" in logs:
1. Ensure you've added the key to local.properties
2. Sync and rebuild the project
3. Check that BuildConfig.OPENAI_API_KEY is properly generated

### API Errors
- **401 Unauthorized**: Invalid API key
- **429 Rate Limit**: Too many requests, implement rate limiting
- **500 Server Error**: OpenAI service issue, implement retry logic

## Security Best Practices

1. **Never commit API keys** to version control
2. **Use different keys** for development and production
3. **Monitor usage** in OpenAI dashboard
4. **Implement rate limiting** to prevent abuse
5. **Consider a backend proxy** for production apps

## Cost Management

- ChatGPT-4 API usage is billed per token
- Audio transcription (Whisper) is billed per minute
- Text-to-speech is billed per character
- Set usage limits in OpenAI dashboard
- Monitor costs regularly

## Next Steps

1. Test the photo learning feature with your API key
2. Adjust temperature and max_tokens for optimal responses
3. Implement caching to reduce API calls
4. Add offline fallbacks for when API is unavailable