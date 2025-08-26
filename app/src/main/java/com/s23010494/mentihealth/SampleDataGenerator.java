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
    
    // Sample journal entries with different moods (updated to match mood tracker values)
    private static final String[] EXCELLENT_ENTRIES = {
        "Had an amazing day! Got promoted at work and celebrated with friends. Feeling incredibly grateful for this opportunity.",
        "Beautiful sunny day! Went for a hike and felt so connected with nature. The fresh air really cleared my mind.",
        "Family dinner was wonderful tonight. Laughed so much with my siblings. These moments are precious.",
        "Finished reading a great book today. It inspired me to pursue new goals. Feeling motivated and optimistic!",
        "Great workout session this morning! Endorphins are flowing and I feel energized for the day ahead.",
        "Had coffee with an old friend. It's amazing how some friendships pick up right where they left off.",
        "Completed a challenging project at work. The sense of accomplishment is incredible!"
    };
    
    private static final String[] GOOD_ENTRIES = {
        "Today was a good day overall. Managed to get through my to-do list and still had time to relax.",
        "Had a productive day at work. Everything went smoothly and I felt competent handling all tasks.",
        "Nice weather today! Enjoyed a short walk during lunch break which improved my mood.",
        "Caught up with an old friend over the phone. It was nice to hear their voice and share stories.",
        "Cooked a delicious meal tonight. There's something satisfying about creating good food.",
        "Made progress on my personal project today. Small steps forward still count as success.",
        "Had a good night's sleep and woke up feeling refreshed. It really sets a positive tone for the day."
    };
    
    private static final String[] MEH_ENTRIES = {
        "Peaceful evening at home. Sometimes the best therapy is just quiet time with a good cup of tea.",
        "Meditation session went well today. Found inner peace and clarity. Feeling centered and balanced.",
        "Spent time in the garden today. There's something therapeutic about nurturing plants and watching them grow.",
        "Quiet day reading by the window. The rain outside made it feel cozy and serene.",
        "Had a nice, slow morning. No rush, no stress. Sometimes we need these gentle moments.",
        "Evening yoga session helped me unwind. Mind and body feel aligned and peaceful.",
        "Organized my living space today. A clean environment brings such mental clarity."
    };
    
    private static final String[] NOT_GREAT_ENTRIES = {
        "Missing my grandmother today. Some days the grief hits harder than others.",
        "Feeling a bit lonely lately. Sometimes it's hard when friends are busy with their own lives.",
        "Rainy day matching my mood. Sometimes we need to sit with sadness and let it pass.",
        "Had to say goodbye to my childhood pet today. The house feels so quiet without them.",
        "Feeling overwhelmed with work stress. Need to find better ways to cope with pressure.",
        "Arguments with family are weighing on my heart. Hoping we can resolve things soon.",
        "Disappointed about missing out on an opportunity. Rejection is never easy to handle."
    };
    
    private static final String[] TERRIBLE_ENTRIES = {
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
     * @param userEmail The email of the user to generate data for
     */
    public boolean generateSampleMoodData(String userEmail) {
        try {
            // Check if the user exists
            if (!dbHelper.userExists(userEmail)) {
                return false; // User doesn't exist, can't generate data
            }
            
            // Generate mood data for the past 30 days
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            for (int i = 29; i >= 0; i--) {
                // Set date to i days ago
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, -i);
                String date = dateFormat.format(calendar.getTime());
                
                // Some days have no entries (10% chance), some have 1-2 entries max
                float entryChance = random.nextFloat();
                
                if (entryChance < 0.1f) {
                    // No entry for this day (10% chance - reduced for more data points)
                    continue;
                } else if (entryChance < 0.8f) {
                    // Single entry (70% chance) - use varied mood pattern
                    String mood = generateVariedMood(i, 0);
                    String entry = getRandomEntry(mood);
                    String image = getMoodImage(mood);
                    
                    long result = dbHelper.insertJournalEntryWithImage(userEmail, mood, entry, date, image);
                    if (result != -1) {
                        System.out.println("Added " + mood + " entry for " + date + " (day " + i + " ago)");
                    }
                } else {
                    // Two entries (20% chance) - each with varied mood pattern
                    for (int j = 0; j < 2; j++) {
                        String mood = generateVariedMood(i, j);
                        String entry = getRandomEntry(mood);
                        String image = getMoodImage(mood);
                        
                        long result = dbHelper.insertJournalEntryWithImage(userEmail, mood, entry, date, image);
                        if (result != -1) {
                            System.out.println("Added " + mood + " entry #" + (j+1) + " for " + date + " (day " + i + " ago)");
                        }
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
     * Generate and insert sample mood data for the sample user (for backwards compatibility)
     */
    public boolean generateSampleMoodData() {
        return generateSampleMoodData(SAMPLE_EMAIL);
    }
    
    /**
     * Generate varied mood patterns with smooth wave-like curves for testing
     */
    private String generateVariedMood(int daysAgo, int entryNum) {
        String[] allMoods = {"Excellent!", "Good!", "Meh", "Not great", "Terrible"};
        
        // Create smooth wave patterns with multiple overlapping sine waves
        double dayPhase = (29 - daysAgo) / 29.0; // Normalized from 0 to 1 (oldest to newest)
        
        // Primary wave - creates main up/down pattern over the 30 days
        double primaryWave = Math.sin(dayPhase * Math.PI * 1.5) * 0.5; // 1.5 cycles for smoother curves
        
        // Secondary wave - adds smaller variations
        double secondaryWave = Math.sin(dayPhase * Math.PI * 4) * 0.15; // Four cycles for subtle detail
        
        // Weekly pattern - slight boost on weekends
        int dayOfWeek = daysAgo % 7;
        double weeklyPattern = 0;
        if (dayOfWeek == 0 || dayOfWeek == 1) { // Weekend
            weeklyPattern = 0.15;
        } else if (dayOfWeek == 2) { // Monday
            weeklyPattern = -0.1;
        }
        
        // Combine waves to create overall mood trend
        double moodTrend = primaryWave + secondaryWave + weeklyPattern;
        
        // Add small random variation for multiple entries per day
        if (entryNum > 0) {
            moodTrend += (random.nextGaussian() * 0.05); // Reduced randomness for smoother curves
        }
        
        // Convert trend to mood selection
        // Map moodTrend (-0.7 to 0.7 range) to mood probabilities
        double[] moodWeights = new double[5];
        
        if (moodTrend > 0.3) {
            // Very positive trend - favor Excellent and Good
            moodWeights[0] = 0.5; // Excellent!
            moodWeights[1] = 0.35; // Good!
            moodWeights[2] = 0.1; // Meh
            moodWeights[3] = 0.05; // Not great
            moodWeights[4] = 0.0; // Terrible
        } else if (moodTrend > 0.1) {
            // Positive trend - favor Good and Excellent
            moodWeights[0] = 0.35; // Excellent!
            moodWeights[1] = 0.45; // Good!
            moodWeights[2] = 0.15; // Meh
            moodWeights[3] = 0.05; // Not great
            moodWeights[4] = 0.0; // Terrible
        } else if (moodTrend > -0.1) {
            // Neutral trend - favor Meh with some Good
            moodWeights[0] = 0.15; // Excellent!
            moodWeights[1] = 0.3; // Good!
            moodWeights[2] = 0.4; // Meh
            moodWeights[3] = 0.1; // Not great
            moodWeights[4] = 0.05; // Terrible
        } else if (moodTrend > -0.3) {
            // Negative trend - favor Not great and Meh
            moodWeights[0] = 0.05; // Excellent!
            moodWeights[1] = 0.15; // Good!
            moodWeights[2] = 0.35; // Meh
            moodWeights[3] = 0.35; // Not great
            moodWeights[4] = 0.1; // Terrible
        } else {
            // Very negative trend - favor Terrible and Not great
            moodWeights[0] = 0.0; // Excellent!
            moodWeights[1] = 0.05; // Good!
            moodWeights[2] = 0.2; // Meh
            moodWeights[3] = 0.45; // Not great
            moodWeights[4] = 0.3; // Terrible
        }
        
        // Select mood based on weights
        double randomValue = random.nextDouble();
        double cumulative = 0;
        
        for (int i = 0; i < moodWeights.length; i++) {
            cumulative += moodWeights[i];
            if (randomValue <= cumulative) {
                return allMoods[i];
            }
        }
        
        // Fallback
        return "Meh";
    }
    
    /**
     * Generate a realistic mood based on day position (some patterns)
     * Updated to use calendar-compatible mood values
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
            if (randomFactor < 0.4f) return "Excellent!";
            else if (randomFactor < 0.7f) return "Good!";
            else if (randomFactor < 0.9f) return "Meh";
            else return "Not great";
        } else if (isMonday) {
            // Mondays can be tough
            if (randomFactor < 0.3f) return "Not great";
            else if (randomFactor < 0.5f) return "Terrible";
            else if (randomFactor < 0.8f) return "Meh";
            else return "Good!";
        } else if (isMidWeek) {
            // Mid-week stress
            if (randomFactor < 0.35f) return "Not great";
            else if (randomFactor < 0.6f) return "Meh";
            else if (randomFactor < 0.8f) return "Good!";
            else return "Excellent!";
        } else {
            // Regular weekdays
            if (randomFactor < 0.35f) return "Good!";
            else if (randomFactor < 0.6f) return "Meh";
            else if (randomFactor < 0.8f) return "Excellent!";
            else if (randomFactor < 0.9f) return "Not great";
            else return "Terrible";
        }
    }
    
    /**
     * Get a random journal entry for the given mood
     */
    private String getRandomEntry(String mood) {
        String[] entries;
        
        switch (mood) {
            case "Excellent!":
                entries = EXCELLENT_ENTRIES;
                break;
            case "Good!":
                entries = GOOD_ENTRIES;
                break;
            case "Meh":
                entries = MEH_ENTRIES;
                break;
            case "Not great":
                entries = NOT_GREAT_ENTRIES;
                break;
            case "Terrible":
                entries = TERRIBLE_ENTRIES;
                break;
            default:
                entries = MEH_ENTRIES; // Default fallback
        }
        
        return entries[random.nextInt(entries.length)];
    }
    
    /**
     * Get a completely random mood with equal probability for each mood type
     */
    private String getRandomMood() {
        String[] allMoods = {"Excellent!", "Good!", "Meh", "Not great", "Terrible"};
        return allMoods[random.nextInt(allMoods.length)];
    }
    
    /**
     * Get mood-specific image drawable name for the given mood
     */
    private String getMoodImage(String mood) {
        switch (mood) {
            case "Excellent!":
                return "yay"; // yay.png
            case "Good!":
                return "nice"; // nice.png
            case "Meh":
                return "meh"; // meh.png
            case "Not great":
                return "eh"; // eh.png
            case "Terrible":
                return "ugh"; // ugh.png
            default:
                return "meh"; // Default fallback
        }
    }
    
    /**
     * Clear all existing data for a specific user
     * @param userEmail The email of the user whose data should be cleared
     */
    public boolean clearUserData(String userEmail) {
        return dbHelper.deleteAllJournalEntries(userEmail);
    }
    
    /**
     * Clear all existing data for the sample user (for backwards compatibility)
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
