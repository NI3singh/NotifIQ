### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\.git\COMMIT_EDITMSG
*Saved at: 5/15/2026, 2:24:53 PM*

**[ADDED]**
```
60    
61    
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\.git\COMMIT_EDITMSG
*Saved at: 5/15/2026, 2:24:51 PM*

**[ADDED]**
```
3     Copy out/splash.mp4 → app/src/main/res/raw/splash.mp4
```
**[ADDED]**
```
5     Create SplashActivity.kt:
6     
7     @AndroidEntryPoint
8     class SplashActivity : AppCompatActivity() {
9     
10        override fun onCreate(savedInstanceState: Bundle?) {
11            super.onCreate(savedInstanceState)
12            enableEdgeToEdge()
13            setContentView(R.layout.activity_splash)
14    
15            val videoView = findViewById<VideoView>(R.id.splashVideo)
16            val uri = Uri.parse("android.resource://$packageName/${R.raw.splash}")
17            videoView.setVideoURI(uri)
18            videoView.setOnPreparedListener { it.isLooping = false }
19            videoView.setOnCompletionListener {
20                startActivity(Intent(this, MainActivity::class.java))
21                finish()
22            }
23            videoView.start()
24        }
25    }
26    Create res/layout/activity_splash.xml:
27    <?xml version="1.0" encoding="utf-8"?>
28    <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
29        android:layout_width="match_parent"
30        android:layout_height="match_parent"
31        android:background="#09090D">
32    
33        <VideoView
34            android:id="@+id/splashVideo"
35            android:layout_width="match_parent"
36            android:layout_height="match_parent"
37            android:layout_gravity="center" />
38    
39    </FrameLayout>
40    In AndroidManifest.xml, make SplashActivity the launcher and remove LAUNCHER from MainActivity:
41    <activity
42        android:name=".SplashActivity"
43        android:exported="true"
44        android:theme="@style/Theme.NotifIQ"
45        android:screenOrientation="portrait">
46        <intent-filter>
47            <action android:name="android.intent.action.MAIN" />
48            <category android:name="android.intent.category.LAUNCHER" />
49        </intent-filter>
50    </activity>
51    
52    <activity
53        android:name=".MainActivity"
54        android:exported="false"   <!-- changed from true -->
55        android:theme="@style/Theme.NotifIQ">
56        <!-- no intent-filter here -->
57    </activity>
58    Add to app/build.gradle.kts:
59    implementation("androidx.appcompat:appcompat:1.7.0")
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\.git\COMMIT_EDITMSG
*Saved at: 5/15/2026, 2:24:44 PM*

**[ADDED]**
```
2     
3     
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\.git\COMMIT_EDITMSG
*Saved at: 5/15/2026, 2:24:43 PM*

**[REMOVED]**
```
(from line ~1)


```
**[ADDED]**
```
1     added animation video following this : 
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\build.gradle.kts
*Saved at: 5/15/2026, 1:46:13 PM*

**[ADDED]**
```
62        implementation("androidx.appcompat:appcompat:1.7.0")
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:27 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"    <!--changed from true to false for security hardening -->

```
**[ADDED]**
```
38                android:exported="false"    
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:24 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"    changed from true to false for security hardening -->

```
**[ADDED]**
```
38                android:exported="false"    <!--changed from true to false for security hardening -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:22 PM*

**[REMOVED]**
```
(from line ~38)
             android:exported="false"    changed from true to false for security hardening -->

```
**[ADDED]**
```
38                android:exported="false"    changed from true to false for security hardening -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:20 PM*

**[REMOVED]**
```
(from line ~38)
            <!-- android:exported="false"    changed from true to false for security hardening -->

```
**[ADDED]**
```
38                 android:exported="false"    changed from true to false for security hardening -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:12 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"    changed from true to false for security hardening

```
**[ADDED]**
```
38                <!-- android:exported="false"    changed from true to false for security hardening -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:09 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"    changed from true 

```
**[ADDED]**
```
38                android:exported="false"    changed from true to false for security hardening
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:05 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"    changed from true -->

```
**[ADDED]**
```
38                android:exported="false"    changed from true 
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:45:02 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"   <!-- changed from true -->

```
**[ADDED]**
```
38                android:exported="false"    changed from true -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:44:58 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"   

```
**[ADDED]**
```
38                android:exported="false"   <!-- changed from true -->
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:44:51 PM*

**[REMOVED]**
```
(from line ~38)
            android:exported="false"   <!-- changed from true -->

```
**[ADDED]**
```
38                android:exported="false"   
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:44:39 PM*

**[REMOVED]**
```
(from line ~26)
        android:name=".SplashActivity"
        android:exported="true"
        android:theme="@style/Theme.NotifIQ"
        android:screenOrientation="portrait">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

```
**[ADDED]**
```
26                android:name=".SplashActivity"
27                android:exported="true"
28                android:theme="@style/Theme.NotifIQ"
29                android:screenOrientation="portrait">
30                <intent-filter>
31                    <action android:name="android.intent.action.MAIN" />
32                    <category android:name="android.intent.category.LAUNCHER" />
33                </intent-filter>
34            </activity>
```
**[REMOVED]**
```
(from line ~36)
    <activity
        android:name=".MainActivity"
        android:exported="false"   <!-- changed from true -->
        android:theme="@style/Theme.NotifIQ">
        <!-- no intent-filter here -->
    </activity>

```
**[ADDED]**
```
36            <activity
37                android:name=".MainActivity"
38                android:exported="false"   <!-- changed from true -->
39                android:theme="@style/Theme.NotifIQ">
40                <!-- no intent-filter here -->
41            </activity>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:44:36 PM*

**[REMOVED]**
```
(from line ~26)
    android:name=".SplashActivity"
    android:exported="true"
    android:theme="@style/Theme.NotifIQ"
    android:screenOrientation="portrait">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

```
**[ADDED]**
```
26            android:name=".SplashActivity"
27            android:exported="true"
28            android:theme="@style/Theme.NotifIQ"
29            android:screenOrientation="portrait">
30            <intent-filter>
31                <action android:name="android.intent.action.MAIN" />
32                <category android:name="android.intent.category.LAUNCHER" />
33            </intent-filter>
34        </activity>
```
**[REMOVED]**
```
(from line ~36)
<activity
    android:name=".MainActivity"
    android:exported="false"   <!-- changed from true -->
    android:theme="@style/Theme.NotifIQ">
    <!-- no intent-filter here -->
</activity>

```
**[ADDED]**
```
36        <activity
37            android:name=".MainActivity"
38            android:exported="false"   <!-- changed from true -->
39            android:theme="@style/Theme.NotifIQ">
40            <!-- no intent-filter here -->
41        </activity>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\AndroidManifest.xml
*Saved at: 5/15/2026, 1:44:32 PM*

**[REMOVED]**
```
(from line ~26)
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.NotifIQ">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

```
**[ADDED]**
```
26        android:name=".SplashActivity"
27        android:exported="true"
28        android:theme="@style/Theme.NotifIQ"
29        android:screenOrientation="portrait">
30        <intent-filter>
31            <action android:name="android.intent.action.MAIN" />
32            <category android:name="android.intent.category.LAUNCHER" />
33        </intent-filter>
34    </activity>
```
**[ADDED]**
```
36    <activity
37        android:name=".MainActivity"
38        android:exported="false"   <!-- changed from true -->
39        android:theme="@style/Theme.NotifIQ">
40        <!-- no intent-filter here -->
41    </activity>
42    
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\layout\activity_splash.xml
*Saved at: 5/15/2026, 1:43:21 PM*

**[ADDED]**
```
1     <?xml version="1.0" encoding="utf-8"?>
2     <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
3         android:layout_width="match_parent"
4         android:layout_height="match_parent"
5         android:background="#09090D">
6     
7         <VideoView
8             android:id="@+id/splashVideo"
9             android:layout_width="match_parent"
10            android:layout_height="match_parent"
11            android:layout_gravity="center" />
12    
13    </FrameLayout>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\layout\activity_splash.xml
*Saved at: 5/15/2026, 1:42:36 PM*

**[REMOVED]**
```
(from line ~1)
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

</LinearLayout>
```

---

### 📄 c:\Users\ELaunch\AppData\Roaming\Code\User\settings.json
*Saved at: 5/14/2026, 3:11:55 PM*

**[REMOVED]**
```
(from line ~128)
    "chatgpt.composerEnterBehavior": "enter"

```
**[ADDED]**
```
128       "chatgpt.composerEnterBehavior": "cmdIfMultiline"
```

---

### 📄 c:\Users\ELaunch\AppData\Roaming\Code\User\settings.json
*Saved at: 5/14/2026, 3:11:52 PM*

**[REMOVED]**
```
(from line ~128)
    "chatgpt.composerEnterBehavior": "cmdIfMultiline"

```
**[ADDED]**
```
128       "chatgpt.composerEnterBehavior": "enter"
```

---

### 📄 c:\Users\ELaunch\AppData\Roaming\Code\User\settings.json
*Saved at: 5/14/2026, 3:11:51 PM*

**[REMOVED]**
```
(from line ~128)
    "chatgpt.composerEnterBehavior": "enter"

```
**[ADDED]**
```
128       "chatgpt.composerEnterBehavior": "cmdIfMultiline"
```

---

### 📄 c:\Users\ELaunch\AppData\Roaming\Code\User\settings.json
*Saved at: 5/14/2026, 3:11:50 PM*

**[REMOVED]**
```
(from line ~128)
    "chatgpt.composerEnterBehavior": "cmdIfMultiline"

```
**[ADDED]**
```
128       "chatgpt.composerEnterBehavior": "enter"
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\mipmap-anydpi-v26\ic_launcher.xml
*Saved at: 5/14/2026, 3:10:42 PM*

**[REMOVED]**
```
(from line ~2)
  <background android:drawable="@color/ic_launcher_background"/>

```
**[ADDED]**
```
2       <background android:drawable="@drawable/ic_launcher_background"/>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\mipmap-anydpi-v26\ic_launcher.xml
*Saved at: 5/14/2026, 3:10:40 PM*

**[REMOVED]**
```
(from line ~2)
  <background android:drawable="@drawable/ic_launcher_background"/>

```
**[ADDED]**
```
2       <background android:drawable="@color/ic_launcher_background"/>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\mipmap-anydpi-v26\ic_launcher.xml
*Saved at: 5/14/2026, 2:44:19 PM*

**[REMOVED]**
```
(from line ~2)
  <background android:drawable="@color/ic_launcher_background"/>

```
**[ADDED]**
```
2       <background android:drawable="@drawable/ic_launcher_background"/>
```

---

### 📄 c:\Users\ELaunch\OneDrive\nitin-p\NotifIQ\app\src\main\res\mipmap-anydpi-v26\ic_launcher_round.xml
*Saved at: 5/14/2026, 2:44:10 PM*

**[REMOVED]**
```
(from line ~2)
  <background android:drawable=""/>
  <foreground android:drawable="@mipmap/ic@color/ic_launcher_background_launcher_foreground"/>

```
**[ADDED]**
```
2       <background android:drawable="@drawable/ic_launcher_background"/>
3       <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
```

---

