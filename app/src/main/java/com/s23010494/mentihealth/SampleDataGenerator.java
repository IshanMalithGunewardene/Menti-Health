package com.s23010494.mentihealth;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SampleDataGenerator {
    private static final String SAMPLE_EMAIL = "exampleuser123@gmail.com";
    private static final String SAMPLE_NAME = "Alex Johnson";
    
    // Sample journal entries with different moods
    private static final String[] HAPPY_ENTRIES = {
        "Had an amazing day! Got promoted at work and celebrated with friends. Feeling incredibly grateful for this opportunity.",
        "Beautiful sunny day! Went for a hike and felt so connected with nature. The fresh air really cleared my mind.",
        "Family dinner was wonderful tonight. Laughed so much with my siblings. These moments are precious.",
        "Finished reading a great book today. It inspired me to pursue new goals. Feeling motivated and optimistic!",
        "Great workout session this morning! Endorphins are flowing and I feel energized for the day ahead.",
        "Had coffee with an old friend. It's amazing how some friendships pick up right where they left off.",
        "Completed a challenging project at work. The sense of accomplishment is incredible!"
    };
    
    private static final String[] CALM_ENTRIES = {
        "Peaceful evening at home. Sometimes the best therapy is just quiet time with a good cup of tea.",
        "Meditation session went well today. Found inner peace and clarity. Feeling centered and balanced.",
        "Spent time in the garden today. There's something therapeutic about nurturing plants and watching them grow.",
        "Quiet day reading by the window. The rain outside made it feel cozy and serene.",
        "Had a nice, slow morning. No rush, no stress. Sometimes we need these gentle moments.",
        "Evening yoga session helped me unwind. Mind and body feel aligned and peaceful.",
        "Organized my living space today. A clean environment brings such mental clarity."
    };
    
    private static final String[] EXCITED_ENTRIES = {
        "Can't contain my excitement! Planning a trip to Europe next month. Adventure awaits!",
        "Just got tickets to see my favorite band! The anticipation is killing me in the best way.",
        "Starting a new hobby - photography! Can't wait to capture the world through my lens.",
        "My best friend is getting married and I'm the maid of honor! So much joy and celebration ahead.",
        "Got accepted into the course I've been wanting to take. Learning new skills here I come!",
        "Planning a surprise party for my mom's birthday. The excitement of making her happy is overwhelming!",
        "New job opportunity came up that's perfect for my career goals. Fingers crossed!"
    };
    
    private static final String[] SAD_ENTRIES = {
        "Missing my grandmother today. Some days the grief hits harder than others.",
        "Feeling a bit lonely lately. Sometimes it's hard when friends are busy with their own lives.",
        "Rainy day matching my mood. Sometimes we need to sit with sadness and let it pass.",
        "Had to say goodbye to my childhood pet today. The house feels so quiet without them.",
        "Feeling overwhelmed with work stress. Need to find better ways to cope with pressure.",
        "Arguments with family are weighing on my heart. Hoping we can resolve things soon.",
        "Disappointed about missing out on an opportunity. Rejection is never easy to handle."
    };
    
    private static final String[] ANXIOUS_ENTRIES = {
        "Big presentation at work tomorrow. My mind is racing with all the what-ifs and scenarios.",
        "Medical appointment coming up and I'm worried about the results. Trying to stay positive.",
        "First day at new job next week. Excitement mixed with nervous energy about the unknown.",
        "Travel anxiety kicking in before my flight. Flying always makes me feel unsettled.",
        "Waiting for important news and the uncertainty is making me restless. Need to practice patience.",
        "Social event tonight and I'm feeling nervous about meeting new people. Pushing myself out of comfort zone.",
        "Financial worries keeping me up at night. Need to make a better budget plan."
    };
    
    private DBHelper dbHelper;
    private Random random;
    
    public SampleDataGenerator(Context context) {
        dbHelper = new DBHelper(context);
        random = new Random();
    }
    
    /**
     * Generate and insert sample mood data for the past month
     */
    public boolean generateSampleMoodData() {
        try {
            // First, ensure the user exists
            if (!dbHelper.userExists(SAMPLE_EMAIL)) {
                // Create the sample user
                if (!dbHelper.addUser(SAMPLE_EMAIL, "password123")) {
                    return false;
                }
                // Add user name
                if (!dbHelper.updateName(SAMPLE_EMAIL, SAMPLE_NAME)) {
                    return false;
                }
            }
            
            // Generate mood data for the past 30 days
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            for (int i = 29; i >= 0; i--) {
                // Set date to i days ago
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, -i);
                String date = dateFormat.format(calendar.getTime());
                
                // Generate realistic mood pattern (some variation but generally positive trend)
                String mood = generateRealisticMood(i);
                String entry = getRandomEntry(mood);
                
                // Add some randomness - not every day has an entry (70% chance)
                if (random.nextFloat() < 0.7f) {
                    // Insert the journal entry
                    long result = dbHelper.insertJournalEntry(SAMPLE_EMAIL, mood, entry, date);
                    
                    if (result == -1) {
                        System.out.println("Failed to insert entry for date: " + date);
                    } else {
                        System.out.println("Added " + mood + " entry for " + date);
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate a realistic mood based on day position (some patterns)
     */
    private String generateRealisticMood(int daysAgo) {
        // Create some realistic patterns
        float randomFactor = random.nextFloat();
        
        // Weekend effect (days 0,1,7,8,14,15,21,22,28,29 are weekends approximately)
        boolean isWeekend = (daysAgo % 7 == 0) || (daysAgo % 7 == 1);
        
        // Monday blues effect
        boolean isMonday = (daysAgo % 7 == 2);
        
        // Mid-week stress
        boolean isMidWeek = (daysAgo % 7 == 4) || (daysAgo % 7 == 5);
        
        if (isWeekend) {
            // Weekends tend to be happier
            if (randomFactor < 0.4f) return "Happy";
            else if (randomFactor < 0.7f) return "Excited";
            else if (randomFactor < 0.9f) return "Calm";
            else return "Anxious";
        } else if (isMonday) {
            // Mondays can be tough
            if (randomFactor < 0.3f) return "Anxious";
            else if (randomFactor < 0.5f) return "Sad";
            else if (randomFactor < 0.8f) return "Calm";
            else return "Happy";
        } else if (isMidWeek) {
            // Mid-week stress
            if (randomFactor < 0.35f) return "Anxious";
            else if (randomFactor < 0.6f) return "Calm";
            else if (randomFactor < 0.8f) return "Happy";
            else return "Excited";
        } else {
            // Regular weekdays
            if (randomFactor < 0.35f) return "Happy";
            else if (randomFactor < 0.6f) return "Calm";
            else if (randomFactor < 0.8f) return "Excited";
            else if (randomFactor < 0.9f) return "Anxious";
            else return "Sad";
        }
    }
    
    /**
     * Get a random journal entry for the given mood
     */
    private String getRandomEntry(String mood) {
        String[] entries;
        
        switch (mood.toLowerCase()) {
            case "happy":
                entries = HAPPY_ENTRIES;
                break;
            case "calm":
                entries = CALM_ENTRIES;
                break;
            case "excited":
                entries = EXCITED_ENTRIES;
                break;
            case "sad":
                entries = SAD_ENTRIES;
                break;
            case "anxious":
                entries = ANXIOUS_ENTRIES;
                break;
            default:
                entries = CALM_ENTRIES; // Default fallback
        }
        
        return entries[random.nextInt(entries.length)];
    }
    
    /**
     * Clear all existing data for the sample user
     */
    public boolean clearSampleUserData() {
        return dbHelper.deleteAllJournalEntries(SAMPLE_EMAIL);
    }
    
    /**
     * Check if sample user exists
     */
    public boolean sampleUserExists() {
        return dbHelper.userExists(SAMPLE_EMAIL);
    }
    
    /**
     * Get sample user email
     */
    public String getSampleEmail() {
        return SAMPLE_EMAIL;
    }
    
    /**
     * Get sample user name
     */
    public String getSampleName() {
        return SAMPLE_NAME;
    }
}
