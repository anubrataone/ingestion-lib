package com.att.ingestion.utils;

import javax.xml.transform.stream.StreamSource;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sc233q
 */
public class StringUtils extends org.apache.commons.lang.StringUtils {

    /**
     * Removes all double-quote characters from a String
     *
     * @param inputString
     * @return
     */
    public static String removeDoubleQuotes(String inputString) {
        if(inputString == null)
            return inputString;

        return inputString.replaceAll("\"\"", "\"");
    }

    /**
     * Convert all multiple whitespaces to single whitespaces
     *
     * @param inputString
     * @return String with multiple whitespaces normalized to single space
     */
    public static String removeMultipleWhiteSpaces(String inputString) {
        return inputString.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Collapse multiple whitespaces to single whitespaces and replace with an "_"
     *
     * @param inputString
     * @return String with whitespace normalized to "_"
     */
    public static String replaceWhiteSpaces(String inputString) {
        return inputString.replaceAll("\\s+", "_").trim();
    }

    /**
     * Remove all characters except letters, digits, - and _
     * @param str
     * @return string with characters removed
     */
    public static String removeSpecialCharacters(String str) {
        StringBuffer sb = new StringBuffer();
        char[] array = str.toCharArray();
        for (char c : array) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c == '-') || (c == '_')) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Truncate the given string to the given length
     *
     * @param length
     * @param string
     * @param returnTail if true - tail of string is returned, if false head is returned
     * @return truncated string
     */
    public static String truncateString(int length, String string, boolean returnTail) {
        String truncatedString = string;
        if (truncatedString != null) {
            int strLength = truncatedString.length();

            if (strLength > length) {
                int offset = strLength - length;
                if (returnTail) {
                    truncatedString = truncatedString.substring(offset, strLength);
                } else {
                    truncatedString = truncatedString.substring(0, length);
                }
            }
        }

        return truncatedString;
    }

    /**
     * Converts a string to an integer. If it is blank or bad format, null is returned.
     * @param intStr
     * @return
     */
    public static Integer intFromString(String intStr) {
        if (isNotBlank(intStr)) {
            try {
                Integer toReturn = Integer.parseInt(intStr.trim());
                return toReturn;
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
    
    public static List<String> splitToList(String string, String separator) {
        List<String> strTokens = new ArrayList<String>();
        if (!isBlank(string)) {
            String[] arr = split(string, separator);
            if (arr != null && arr.length > 0) {
                for (String str : arr) {
                    strTokens.add(str.trim());
                }
            }
        }

        return strTokens;
    }
    
    public static String[] splitAndTrim(String string, String separator) {
        String[] strTokens = {}; 
        if (!isBlank(string)) {
            String[] arr = split(string, separator);
            if (arr != null && arr.length > 0) {
                strTokens = stripAll(arr);
            }
        }
        
        return strTokens;
    }

    public static String getDurationInSec(String value) {
        Double length = null;
        try {
            length = new Double(value);
            value = String.valueOf(Math.round(Math.floor(length)));
        } catch (NumberFormatException e) {
            Matcher matcher2 = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})\\.[0-9]+").matcher(value);
            if (matcher2.matches()) {
                long hour = 0;
                try {
                    hour = Long.parseLong(matcher2.group(1));
                } catch (NumberFormatException ex) {}
                long min = 0;
                try {
                    min = Long.parseLong(matcher2.group(2));
                } catch (NumberFormatException ex) {}
                long sec = 0;
                try {
                    sec = Long.parseLong(matcher2.group(3));
                } catch (NumberFormatException ex) {}
                value = String.valueOf(hour * 60 * 60 + min * 60 + sec);
            }
        }
        return value;
    }

    public static String getDurationInMilliSec(String value) {
        Double length = null;
        try {
            length = new Double(value);
            value = String.valueOf(Math.round(Math.floor(length*1000.0))/1000.0);
        } catch (NumberFormatException e) {
            Matcher matcher2 = null;
            if(value.contains(".")) {
                String decimalValue = value.substring(value.indexOf(".")+1, value.length());
                int decimalLength = decimalValue.length();
                String regexStr = String.format("([0-9]{2}):([0-9]{2}):([0-9]{2})\\.([0-9]{%d})", decimalLength);
                matcher2 = Pattern.compile(regexStr).matcher(value);
            }
            else {
                matcher2 = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})").matcher(value);
            }

            if (matcher2.matches()) {
                long hour = 0;
                try {
                    hour = Long.parseLong(matcher2.group(1));
                } catch (NumberFormatException ex) {}
                long min = 0;
                try {
                    min = Long.parseLong(matcher2.group(2));
                } catch (NumberFormatException ex) {}
                double sec = 0.0;
                try {
                    if(matcher2.groupCount() > 3) {
                        sec = Double.parseDouble(String.format("%s.%s", matcher2.group(3), matcher2.group(4)));
                    }
                    else {
                        sec = Double.parseDouble(matcher2.group(3));
                    }
                } catch (NumberFormatException ex) {}


                value = String.valueOf(hour * 60 * 60 + min * 60 + sec);
            }
        }
        return value;
    }

    /**
     * Convert comma separated string to Array of StreamSource
     *
     * @param input comma separated list
     * @return array of StreamSources
     */
    public static StreamSource[] toStreamSourceArray ( final String input) {
        final String[] schemaLocations = org.apache.commons.lang.StringUtils.split(input, ',');
        final List<StreamSource> schemas = new ArrayList<StreamSource>(schemaLocations.length);
        for (final String s : schemaLocations) {
            schemas.add(new StreamSource(StringUtils.class.getResourceAsStream(s)));
        }
        return schemas.toArray(new StreamSource[schemaLocations.length]);

    }

    /**
     * Using UTF8 encoding, reads content from InputStream and returns the content as a String value.
     *
     * @param inputStream
     * @return
     */
    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String theString = writer.toString();
        return theString;
    }

    public static InputStream stringToInputStream(String inputString) {
        InputStream stream = null;
        if(inputString != null) {
            try {
                stream = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        }
        return stream;
    }

    public static String map2String(final Map<Object, Object> map) {
        final StringBuilder sb = new StringBuilder();
        if (map == null) {
            sb.append("{null}");
        } else {
            sb.append(map.getClass().getName()).append(":{");
            for (Object key : map.keySet()) {
                sb.append(key.toString()).append(":").append(map.get(key)).append("|");
            }
            if (sb.toString().endsWith("|")) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
