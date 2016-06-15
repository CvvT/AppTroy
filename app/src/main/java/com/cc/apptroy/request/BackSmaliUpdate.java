package com.cc.apptroy.request;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cc.apptroy.smali.MemoryBackSmali;
import com.cc.apptroy.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by CwT on 16/5/15.
 */
public class BackSmaliUpdate extends CommandHandler {

    String path;

    public BackSmaliUpdate(String path) {
        this.path = path;
    }

    /**
     * update.json template
     * {
     *      "class": ["com.cc.test.MainActivity", ],
     *      "method": [
     *          {"clsName": "", "mthName": "", "signature": ""},
     *      ]
     *  }
     */
    @Override
    public void doAction() {
        File file = new File(path);
        if (!file.exists()) {
            Log.e("cc", "update.json doesn't exist");
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
                sb.append(line);
            JSONObject obj = JSON.parseObject(sb.toString());
            JSONArray classes = obj.getJSONArray("class");
            JSONArray methods = obj.getJSONArray("method");
            for (int i = 0; i < classes.size(); i++) {
                String clsName = classes.getString(i);
                MemoryBackSmali.disassembleClass(Utility.getClassName(clsName));
            }
            for (int i = 0; i < methods.size(); i++) {
                JSONObject each = methods.getJSONObject(i);
                String clsName = each.getString("clsName");
                String mthName = each.getString("mthName");
                String signature = each.getString("signature");
                MemoryBackSmali.disassembleMethod(clsName, mthName, signature);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
