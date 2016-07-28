package com.cc.apptroy.request;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.util.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by CwT on 16/7/28.
 */
public class ResourceDump extends CommandHandler {

    private final Context mContext;
    private final int sourceId;
    private final String action;
    private String fileName = "default.xml";
    private final String TYPE_LAYOUT = "layout";
    private final String TYPE_STRING = "string";

    public ResourceDump(int sourceID, String type) {
        mContext = ModuleContext.getInstance().getAppContext();
        this.sourceId = sourceID;
        action = type;
    }

    public ResourceDump(int sourceID, String type, String fileName) {
        this(sourceID, type);
        this.fileName = fileName;
    }

    @Override
    public void doAction() {
        if (action.equals(TYPE_LAYOUT)) {
            Logger.log_file(getLayout(sourceId), fileName);
        } else if (action.equals(TYPE_STRING)) {
            Logger.log(getString(sourceId));
        }
    }

    private String getString(int id) {
        return mContext.getString(id);
    }

    private String getLayout(int id) {
        XmlResourceParser parser = mContext.getResources().getLayout(id);
        try {
            StringBuilder sb = new StringBuilder();
            int eventType;
            while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        sb.append("<");
                        sb.append(parser.getName());
                        getAttributes(sb, parser);
                        sb.append(">");
                        break;
                    case XmlPullParser.END_TAG:
                        sb.append("</");
                        sb.append(parser.getName());
                        sb.append(">");
                        break;
                    case XmlPullParser.TEXT:
                        sb.append(parser.getText());
                        break;
                }
            }
            return sb.toString();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void getAttributes(StringBuilder sb, XmlResourceParser parser) throws XmlPullParserException {
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
            String namespace = parser.getAttributeNamespace(i);
            if (namespace != null) {
                int index = namespace.lastIndexOf("/");
                if (index > 0) {
                    namespace = namespace.substring(index + 1);
                }
                sb.append(namespace);
                sb.append(":");
            }
            sb.append(parser.getAttributeName(i));
            sb.append("=");
            sb.append(parser.getAttributeValue(i));
        }
    }
}
