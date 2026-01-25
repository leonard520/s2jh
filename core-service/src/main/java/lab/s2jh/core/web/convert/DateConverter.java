package lab.s2jh.core.web.convert;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date converter utility class for parsing and formatting dates.
 * Replaces the Struts/OGNL-based DateConverter.
 */
public class DateConverter {

    private static final Logger logger = LoggerFactory.getLogger(DateConverter.class);

    private static final String DATETIME_WITHOUTSEC_PATTERN = "yyyy-MM-dd HH:mm";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String MONTH_PATTERN = "yyyy-MM";

    /**
     * Convert value to Date
     */
    public Object convertValue(Object value, Class<?> toType) {
        Object result = null;
        if (toType == Date.class) {
            result = doConvertToDate(value);
        } else if (toType == String.class) {
            result = doConvertToString(value);
        }
        return result;
    }

    /**
     * Convert String to Date
     */
    public Date doConvertToDate(Object value) {
        if (value == null) {
            return null;
        }
        Date result = null;

        if (value instanceof String) {
            try {
                String date = (String) value;
                if (StringUtils.isBlank(date)) {
                    return null;
                }
                result = DateUtils.parseDate((String) value, new String[] { 
                    DATE_PATTERN, DATETIME_PATTERN, MONTH_PATTERN, DATETIME_WITHOUTSEC_PATTERN 
                });
            } catch (Exception e) {
                logger.warn("Date conversion error: " + value, e);
            }
        } else if (value instanceof Object[] && ((Object[]) value).length > 0) {
            return doConvertToDate(((Object[]) value)[0]);
        } else if (value instanceof Date) {
            result = (Date) value;
        }
        return result;
    }

    /**
     * Convert Date to String
     */
    private String doConvertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_PATTERN);
            return sdf.format((Date) value);
        }
        return value.toString();
    }
}
