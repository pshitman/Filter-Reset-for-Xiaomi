<div align="center">

# 🌬️ Filter Reset for Xiaomi Air Purifier

**Reset the filter-life counter on your Xiaomi Air Purifier filter — just tap your phone on the filter.**

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![NFC](https://img.shields.io/badge/requires-NFC-blue)
![License](https://img.shields.io/badge/license-MIT-green)

[English](#english) · [فارسی](#فارسی)

</div>

---

## English

Xiaomi Air Purifier filters store their remaining life on a small NFC tag. When the counter hits 0%, the purifier stops working, even though the filter can often be cleaned and reused. This app resets that counter on **your own filter** with one tap, and shows a live log of everything it reads and sends.

### ✅ Tested devices

| Device | Status |
|---|---|
| Xiaomi Smart Air Purifier 4 | ✅ Tested |
| Xiaomi Smart Air Purifier 4 Pro | ✅ Tested |
| Any other model | ❓ Not tested, use at your own risk |

### How to use

1. Install the APK (allow "install from unknown sources" if Android asks).
2. Turn **NFC** on in your phone settings.
3. Open **Filter Reset**.
4. Hold the back of your phone against the filter's tag (usually a small label on the filter frame). Keep it still until the log shows **✅ ریست شد / reset done**.
5. Put the filter back in the purifier.

### What it does

1. Reads the tag's UID.
2. Computes the 4-byte tag password from the UID (SHA-1 based).
3. Authenticates (`PWD_AUTH`, `1B <pwd>`) and writes zeros to page 8 (`A2 08 00 00 00 00`), the same command as the manual reset code `1B<pwd>,3008,A20800000000`.
4. Re-reads page 8 to verify the write.

Every step appears in the on-screen log with timestamps and raw hex (`TX` / `RX`). Use **Copy log** to share it when reporting an issue.

### Build from source

```bash
# JDK 17 + Android SDK (platform 34) + Gradle 8.9
gradle assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

Or fork the repo and run the **Build APK** GitHub Action, then download the artifact.

### ⚠️ Disclaimer

- Unofficial project, not affiliated with Xiaomi.
- It writes to the NFC tag. Use it only on filters you own. You are responsible for the result.
- It only extends the *counter*. A dirty or worn-out filter still filters badly, so clean or replace it when needed.
- No warranty (see [LICENSE](LICENSE)).

### Credits

The password algorithm and tag command come from the research and code in
[logikal/xiaomi-air-purifier-filter-reset](https://github.com/logikal/xiaomi-air-purifier-filter-reset)
(MIT), based on
[Flamingo-tech/xiaomi-air-purifier-reverse-engineering](https://github.com/Flamingo-tech/xiaomi-air-purifier-reverse-engineering)
and the [Hackaday write-up](https://hackaday.com/2024/01/26/hacking-a-xiaomi-air-purifiers-filter-drm-to-extend-its-lifespan/).
This project is an Android port of that logic.

---

## فارسی

فیلتر دستگاه‌های تصفیه‌هوای شیائومی میزان عمر باقی‌مانده‌اش را روی یک تگ NFC کوچک ذخیره می‌کند. وقتی عدد به ۰٪ برسد دستگاه کار نمی‌کند، حتی اگر فیلتر را بشود شست و دوباره استفاده کرد. این برنامه با یک بار نزدیک کردن گوشی، شمارنده‌ی **فیلتر خودتان** را ریست می‌کند و همه‌ی چیزهایی را که می‌خواند و می‌فرستد در یک لاگ زنده نشان می‌دهد.

### ✅ دستگاه‌های تست‌شده

| دستگاه | وضعیت |
|---|---|
| Xiaomi Smart Air Purifier 4 | ✅ تست شده |
| Xiaomi Smart Air Purifier 4 Pro | ✅ تست شده |
| مدل‌های دیگر | ❓ تست نشده، با مسئولیت خودتان |

### روش استفاده

۱. فایل APK را نصب کنید (در صورت پرسش، نصب از منبع ناشناس را مجاز کنید).
۲. **NFC** را در تنظیمات گوشی روشن کنید.
۳. برنامه‌ی **Filter Reset** را باز کنید.
۴. پشت گوشی را روی تگ فیلتر بگذارید و ثابت نگه دارید تا در لاگ **✅ ریست شد** بیاید.
۵. فیلتر را سر جایش برگردانید.

### ⚠️ هشدار

- پروژه‌ی غیررسمی است و ارتباطی با شیائومی ندارد.
- روی تگ NFC می‌نویسد؛ فقط روی فیلتر خودتان استفاده کنید و مسئولیتش با شماست.
- فقط شمارنده را بازنشانی می‌کند. فیلتر کثیف یا فرسوده همچنان بد تصفیه می‌کند، پس در صورت نیاز آن را بشویید یا عوض کنید.
- هیچ گارانتی‌ای ندارد.

---

<div align="center">MIT License · Made with ❤️ for right-to-repair</div>
