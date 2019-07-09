package com.att.ingestion.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;

import com.att.ingestion.utils.StringUtils;

import java.nio.ByteBuffer;

public class PublisherUtil{

    public static Long simpleDateToTime(String date) throws ParseException {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime();
		} catch (Exception e) {
			return 0l;
		}
    }
    
    public static List<String> splitByComma(String string) {
		return splitBy(string, ",");
    }
    
    public static List<String> splitBy(String string, String separator) {
		return splitBy(string, separator, Integer.MAX_VALUE, true);
    }
    
    public static List<String> splitBy(String string, String separator, int limit, boolean trim) {
		if (string == null || separator == null) {
			return new ArrayList<String>();
		}
		String[] r = string.trim().split(separator, limit);
		if (trim) {
			for (int i = 0; i < r.length; i++) {
				r[i] = r[i].trim();
			}
		}
		return Arrays.asList(r);
	}
	
	public static long calculateOvContentId(String input) {
		return generateRandomId(input);

	}

	static long generateRandomId(String string) {
		byte[] result = DigestUtils.sha1(string.getBytes());
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {result[0], result[1], result[2], result[3], result[4], result[5], result[6], result[7]});
		long resultLong = bb.getInt();
		if (resultLong < 0) {
				resultLong *= -1;
		}
		return resultLong;
}

  public static Long advancedDateConverter(String input) {
		String date = input;
		String timezone = null;
		
		if (input.indexOf(",") > 0) {
			List<String> values = splitByComma(input);
			date = values.get(0);
			timezone = values.get(1);
		}
		
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			if (StringUtils.isNotBlank(timezone)) {
				dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
			}
			return dateFormat.parse(date).getTime();
		} catch (Exception e1) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				if (StringUtils.isNotBlank(timezone)) {
					dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
				}
				return dateFormat.parse(date).getTime();
			} catch (Exception e2) {
				try {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					if (StringUtils.isNotBlank(timezone)) {
						dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
					}
					return dateFormat.parse(date).getTime();
				} catch (Exception e3) {
					return null;
				}
			}
		}
	}


}