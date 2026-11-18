package com.example.filterreset;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements NfcAdapter.ReaderCallback {
    private NfcAdapter nfc;
    private TextView logView;
    private final StringBuilder logText = new StringBuilder();
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        Button clear = new Button(this);
        clear.setText("پاک کردن لاگ");
        clear.setOnClickListener(v -> { logText.setLength(0); logView.setText(""); });
        Button copy = new Button(this);
        copy.setText("کپی لاگ");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("log", logText.toString()));
            Toast.makeText(this, "کپی شد", Toast.LENGTH_SHORT).show();
        });
        bar.addView(clear, new LinearLayout.LayoutParams(0, -2, 1));
        bar.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(bar);

        logView = new TextView(this);
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setTextSize(13);
        logView.setTextIsSelectable(true);
        logView.setVerticalScrollBarEnabled(true);
        logView.setMovementMethod(new ScrollingMovementMethod());
        root.addView(logView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc == null) log("این گوشی NFC ندارد.");
        else if (!nfc.isEnabled()) log("NFC خاموش است. از تنظیمات روشنش کن.");
        else log("آماده. گوشی را به تگ فیلتر نزدیک کن.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfc != null && nfc.isEnabled()) {
            nfc.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
            log("Reader mode روشن شد");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfc != null) nfc.disableReaderMode(this);
    }

    private void log(final String s) {
        final String line = fmt.format(new Date()) + "  " + s + "\n";
        runOnUiThread(() -> {
            logText.append(line);
            logView.setText(logText.toString());
            int off = logView.getLayout() == null ? 0
                : logView.getLayout().getLineTop(logView.getLineCount()) - logView.getHeight();
            logView.scrollTo(0, Math.max(off, 0));
        });
    }

    /** ارسال فرمان با ثبت در لاگ */
    private byte[] tx(NfcA a, String name, byte[] cmd) throws IOException {
        log("TX " + name + ": " + PwdCalc.hex(cmd));
        try {
            byte[] r = a.transceive(cmd);
            log("RX " + name + ": " + (r == null ? "null" : PwdCalc.hex(r)));
            return r;
        } catch (IOException e) {
            log("RX " + name + " خطا: " + e.getClass().getSimpleName() + " " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        log("--- تگ پیدا شد ---");
        log("Techs: " + String.join(",", tag.getTechList()));
        NfcA a = NfcA.get(tag);
        if (a == null) { log("❌ تگ NfcA نیست"); return; }
        byte[] uid = tag.getId();
        log("UID: " + PwdCalc.hex(uid));
        log("ATQA: " + PwdCalc.hex(a.getAtqa()) + "  SAK: " + a.getSak());
        byte[] pwd = PwdCalc.getPwd(uid);
        log("Password محاسبه‌شده: " + PwdCalc.hex(pwd));
        log("Reset code معادل: 1B" + PwdCalc.hex(pwd) + ",3008,A20800000000");
        try {
            connect(a);
            auth(a, pwd);
            log("صفحه 8 قبل: " + PwdCalc.hex(tx(a, "READ p8", new byte[] {0x30, 0x08})));
            try {
                tx(a, "WRITE p8", new byte[] {(byte) 0xA2, 0x08, 0, 0, 0, 0});
            } catch (IOException ignore) {
                log("(پاسخ ACK دریافت نشد؛ با خواندن مجدد بررسی می‌شود)");
            }
            try { a.close(); } catch (Exception ignore) {}
            log("اتصال مجدد برای تأیید...");
            connect(a);
            auth(a, pwd);
            byte[] after = tx(a, "READ p8", new byte[] {0x30, 0x08});
            log("صفحه 8 بعد: " + PwdCalc.hex(after));
            boolean ok = after != null && after.length >= 4
                && after[0] == 0 && after[1] == 0 && after[2] == 0 && after[3] == 0;
            log(ok ? "✅ ریست شد" : "❌ نوشتن انجام نشد");
        } catch (Exception e) {
            log("❌ خطا: " + e.getMessage() + "\nگوشی را ثابت نگه دار و دوباره امتحان کن.");
        } finally {
            try { a.close(); } catch (Exception ignore) {}
            log("اتصال بسته شد");
        }
    }

    private void connect(NfcA a) throws IOException {
        a.connect();
        a.setTimeout(3000);
        log("متصل شد (maxTransceive=" + a.getMaxTransceiveLength() + ")");
    }

    private void auth(NfcA a, byte[] pwd) throws IOException {
        byte[] cmd = {0x1B, pwd[0], pwd[1], pwd[2], pwd[3]};
        byte[] pack = tx(a, "PWD_AUTH", cmd);
        if (pack == null || pack.length < 2) throw new IOException("رمز پذیرفته نشد");
        log("احراز هویت OK (PACK=" + PwdCalc.hex(pack) + ")");
    }
}
