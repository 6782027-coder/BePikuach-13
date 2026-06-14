# בפיקוח — הוראות התקנה והפעלה

## דרישות מקדימות
- Android Studio Hedgehog או חדש יותר
- JDK 17
- מכשיר אנדרואיד 8.0+ (API 26)
- USB Debugging מופעל על המכשיר

---

## שלב 1 — פתיחת הפרויקט
1. פתח Android Studio
2. `File → Open` → בחר את תיקיית `BePikuach`
3. המתן לסיום הסנכרון (Gradle Sync)

---

## שלב 2 — הרצה ראשונה
1. חבר את המכשיר למחשב
2. לחץ `Run → Run 'app'`
3. בחר את המכשיר שלך
4. לאחר ההתקנה, בחר `בפיקוח` כ-Launcher ברירת המחדל

---

## שלב 3 — הגדרת Device Owner (נדרש לנעילה קבועה + FRP)

**חשוב:** לפני הרצת הפקודה, ודא:
- אין חשבון Google מחובר למכשיר
- אין Device Admin אחר פעיל

**הרץ מהמחשב:**
```bash
adb shell dpm set-device-owner com.bepikuach/.admin.DeviceAdminReceiver
```

תגובה מצופה: `Success: Device owner set to package com.bepikuach`

לאחר מכן הפעל מחדש את המכשיר.

---

## שלב 4 — הגדרת סיסמה ואפליקציות מאושרות
1. בדף הבית של בפיקוח, לחץ **⚙ הגדרות מנהל**
2. הכנס את הסיסמה הברירת המחדל: **1234**
3. שנה את הסיסמה מיד!
4. סמן את האפליקציות המאושרות
5. לחץ **שמור**

---

## שלב 5 — הגדרת שירות נגישות (חסימה חזקה)
1. הגדרות מכשיר → נגישות → שירותי נגישות
2. מצא **"שירות חסימת בפיקוח"**
3. הפעל אותו

---

## שלב 6 — הרשאת שימוש (לחסימת אפליקציות)
1. הגדרות → פרטיות → שימוש בנתוני אפליקציה
2. מצא **בפיקוח** → אפשר גישה

---

## נעילה קבועה (FRP)
לאחר הגדרת Device Owner:
- היכנס להגדרות מנהל → לחץ **נעילה קבועה**
- **אזהרה: פעולה בלתי הפיכה!**

---

## סיסמה ברירת מחדל
```
1234
```
**שנה אותה מיד!**

---

## מבנה הפרויקט
```
BePikuach/
├── app/src/main/
│   ├── java/com/bepikuach/
│   │   ├── activities/
│   │   │   ├── HomeActivity.java       ← דף הבית הראשי
│   │   │   ├── AdminActivity.java      ← פאנל ניהול
│   │   │   ├── PasswordActivity.java   ← שאלת סיסמה
│   │   │   └── BlockedActivity.java    ← מסך חסימה
│   │   ├── admin/
│   │   │   └── DeviceAdminReceiver.java
│   │   ├── receivers/
│   │   │   └── BootReceiver.java       ← עלייה ללא פרצה
│   │   ├── services/
│   │   │   ├── AppMonitorService.java  ← ניטור רציף
│   │   │   └── BlockerAccessibilityService.java
│   │   └── utils/
│   │       ├── PrefManager.java
│   │       ├── AppInfo.java
│   │       ├── HomeAppsAdapter.java
│   │       └── AdminAppsAdapter.java
│   └── res/
│       ├── layout/                     ← כל מסכי הממשק
│       ├── xml/                        ← הגדרות Device Admin
│       └── values/                     ← צבעים, מחרוזות, תצוגה
```
