package com.junior.scanner;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.*;
import android.view.View;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    TextView status, result;
    Uri selectedUri;
    final String[] patterns = {
        "anti.?cheat","easyanticheat","easy.?anti.?cheat",
        "battleye","be.?anti.?cheat","vgk","vanguard",
        "nprotect","gameguard","integrity.?check",
        "tamper","cheat.?detection"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28,28,28,28);

        TextView title = new TextView(this);
        title.setText("Junior");
        title.setTextSize(30);
        title.setGravity(17);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Anti-Cheat Scanner");
        sub.setTextSize(18);
        sub.setGravity(17);
        root.addView(sub);

        Button browse = new Button(this);
        browse.setText("Browse data.unity3d");
        root.addView(browse);

        Button scan = new Button(this);
        scan.setText("Scan");
        root.addView(scan);

        status = new TextView(this);
        status.setText("Choose a data.unity3d file.");
        root.addView(status);

        result = new TextView(this);
        result.setPadding(0,20,0,20);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(result);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));

        Button save = new Button(this);
        save.setText("Save Report to Downloads");
        root.addView(save);

        setContentView(root);

        browse.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 10);
        });

        scan.setOnClickListener(v -> doScan());
        save.setOnClickListener(v -> saveReport());
    }

    @Override protected void onActivityResult(int r,int c,Intent d) {
        super.onActivityResult(r,c,d);
        if(r==20 && c==RESULT_OK && d!=null) { try { OutputStream o=getContentResolver().openOutputStream(d.getData()); o.write(pendingReport.getBytes("UTF-8")); o.close(); Toast.makeText(this,"Report saved.",Toast.LENGTH_SHORT).show(); } catch(Exception e) { Toast.makeText(this,"Save failed.",Toast.LENGTH_SHORT).show(); } return; }\n        if(r==10 && c==RESULT_OK && d!=null) {
            selectedUri=d.getData();
            status.setText("File selected. Ready to scan.");
        }
    }

    void doScan() {
        if(selectedUri==null) {
            Toast.makeText(this,"Select a data.unity3d file first.",Toast.LENGTH_SHORT).show();
            return;
        }
        status.setText("Scanning for anti-cheat indicators...");
        try {
            InputStream in=getContentResolver().openInputStream(selectedUri);
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buf=new byte[8192]; int n;
            while((n=in.read(buf))!=-1) out.write(buf,0,n);
            in.close();
            String data=new String(out.toByteArray(),"ISO-8859-1");
            ArrayList<String> found=new ArrayList<>();
            for(String p:patterns)
                if(Pattern.compile(p,Pattern.CASE_INSENSITIVE).matcher(data).find()) found.add(p);

            StringBuilder sb=new StringBuilder();
            sb.append("Scan complete.\n\n");
            if(found.isEmpty()) sb.append("No known anti-cheat indicators were found.\n");
            else {
                sb.append("Potential anti-cheat indicators found:\n");
                for(String x:found) sb.append("• ").append(x).append("\n");
            }
            sb.append("\nNo file data was modified.");
            result.setText(sb.toString());
            status.setText("Scan complete.");
        } catch(Exception e) {
            status.setText("Scan failed.");
            result.setText("Error: "+e.getMessage());
        }
    }

    void saveReport() {
        String text=result.getText().toString();
        if(text.isEmpty()) {
            Toast.makeText(this,"Run a scan first.",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TITLE,"data-Junior-AntiCheat-Scan.txt");
        startActivityForResult(i,20);
        pendingReport=text;
    }
    String pendingReport="";
}
