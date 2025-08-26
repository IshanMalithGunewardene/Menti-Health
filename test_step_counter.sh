#!/bin/bash

# Step Counter Testing Script for MentiHealth App
# Usage: ./test_step_counter.sh

ADB_PATH="/home/ishan/Android/Sdk/platform-tools/adb"

echo "🚀 MentiHealth Step Counter Testing Script"
echo "=========================================="

# Check if device is connected
echo "📱 Checking connected devices..."
$ADB_PATH devices

echo ""
echo "🏗️  Building and installing app..."
cd /home/ishan/AndroidStudioProjects/MentiHealth
./gradlew assembleDebug && ./gradlew installDebug

echo ""
echo "📲 Launching MentiHealth app..."
$ADB_PATH shell monkey -p com.s23010494.mentihealth 1

echo ""
echo "🧪 Testing Methods Available:"
echo ""

echo "Method 1: Debug Buttons (Most Reliable)"
echo "  → Use the +100, +500, Reset buttons in your app"
echo ""

echo "Method 2: ADB Sensor Simulation"
echo "  → Try these commands one by one:"

echo ""
echo "# Test 1: Basic step counter broadcast"
echo "$ADB_PATH shell am broadcast -a com.android.server.sensorservice.test.action.TEST_SENSOR --ei sensor_type 19 --ef sensor_value 100"

echo ""
echo "# Test 2: Alternative step counter broadcast"  
echo "$ADB_PATH shell am broadcast -a android.intent.action.STEP_COUNTER --ei steps 500"

echo ""
echo "# Test 3: Fitness step broadcast"
echo "$ADB_PATH shell am broadcast -a com.google.android.gms.fitness.STEP_COUNTER --ei steps 1000"

echo ""
echo "# Test 4: Generic sensor simulation"
echo "$ADB_PATH shell am broadcast -a android.hardware.Sensor.TYPE_STEP_COUNTER --ei value 1500"

echo ""
echo "Method 3: Accelerometer Simulation (Advanced)"
echo "  → Simulate walking motion through accelerometer"

# Function to simulate walking
simulate_walking() {
    echo "🚶 Simulating walking motion..."
    for i in {1..10}; do
        # Simulate up-down motion typical of walking
        $ADB_PATH shell am broadcast -a android.hardware.Sensor.TYPE_ACCELEROMETER --ef x 0.5 --ef y 9.8 --ef z 0.2
        sleep 0.5
        $ADB_PATH shell am broadcast -a android.hardware.Sensor.TYPE_ACCELEROMETER --ef x -0.3 --ef y 10.2 --ef z 0.1
        sleep 0.5
    done
}

echo ""
echo "🎯 Quick Test Commands:"
echo "Run these manually to test step detection:"

echo ""
echo "# Send 100 steps"
echo "$ADB_PATH shell am broadcast -a com.android.server.sensorservice.test.action.TEST_SENSOR --ei sensor_type 19 --ef sensor_value 100"

echo ""
echo "# Send 500 steps" 
echo "$ADB_PATH shell am broadcast -a com.android.server.sensorservice.test.action.TEST_SENSOR --ei sensor_type 19 --ef sensor_value 500"

echo ""
echo "# Send 1000 steps"
echo "$ADB_PATH shell am broadcast -a com.android.server.sensorservice.test.action.TEST_SENSOR --ei sensor_type 19 --ef sensor_value 1000"

echo ""
echo "📊 Monitor sensor activity:"
echo "$ADB_PATH shell dumpsys sensorservice | grep -i step"

echo ""
echo "🔍 Check app logs:"
echo "$ADB_PATH logcat | grep MentiHealth"

echo ""
echo "✅ Testing complete! Use the debug buttons in your app for the most reliable testing."
