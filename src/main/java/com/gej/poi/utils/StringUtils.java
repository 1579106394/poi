package com.gej.poi.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 增强StringUtils
 *
 * @author
 */
public final class StringUtils {
    private static final String LOCAL_HOST_IP = "127.0.0.1";

    /**
     * Excel科学计数法关键字
     */
    private static final String EXCEL_F_E = "E";

    private StringUtils() {
    }

    /**
     * 下划线转驼峰
     */
    public static String upperTable(String str) {
        StringBuilder result = new StringBuilder();
        String[] a = str.split("_");
        for (String s : a) {
            if (!str.contains("_")) {
                result.append(s);
                continue;
            }
            if (result.length() == 0) {
                result.append(s.toLowerCase());
            } else {
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 中文钱数
     *
     * @param money
     * @return
     */
    public static String getChineseMoney(BigDecimal money) {
        if (money != null) {
            String s = new DecimalFormat("#.00").format(money.abs());
            // 将字符串中的"."去掉
            s = s.replaceAll("\\.", "");
            char[] d = {'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'};
            String unit = "仟佰拾兆仟佰拾亿仟佰拾万仟佰拾元角分";
            int c = unit.length();
            StringBuffer sb = new StringBuffer(unit);
            for (int i = s.length() - 1; i >= 0; i--) {
                sb.insert(c - s.length() + i, d[s.charAt(i) - 0x30]);
            }
            s = sb.substring(c - s.length(), c + s.length());
            s = s.replaceAll("零[仟佰拾]", "零").replaceAll("零{2,}", "零").replaceAll("零([兆万元Ԫ])", "$1").replaceAll("零[角分]", "");
            if (BigDecimal.ZERO.compareTo(money) == 1) {
                return "负" + s + "整";
            }
            if (BigDecimal.ZERO.compareTo(money) == 0) {
                return "零元整";
            }
            return s + "整";
        }
        return "";
    }

    /**
     * 获取列对应的下标字母
     */
    public static String getExcelCellIndex(int index) {
        StringBuilder rs = new StringBuilder();
        while (index > 0) {
            index--;
            rs.insert(0, ((char) (index % 26 + 'A')));
            index = (index - index % 26) / 26;
        }
        return rs.toString();
    }

    public static String toString(Object obj) {
        return Objects.toString(obj, "");
    }

    public static Boolean equal(Object obj1, Object obj2) {
        return toString(obj1).equals(toString(obj2));
    }

    /**
     * 把中文转成Unicode码
     *
     * @param str 字符
     * @return 转码后的字符
     */
    public static String chinaToUnicode(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            int chr1 = str.charAt(i);
            // 汉字范围 \u4e00-\u9fa5 (中文)
            if (chr1 >= 19968 && chr1 <= 171941) {
                result.append("\\u").append(Integer.toHexString(chr1));
            } else {
                result.append(str.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * 判断字符串是否是链接
     *
     * @param href
     * @return
     */
    public static Boolean isHref(String href) {
        boolean foundMatch;
        try {
            foundMatch = href.matches("(?sm)^https?://[-\\w+&@#/%=~_|$?!:,.\\\\*]+$");
        } catch (Exception e) {
            return false;
        }
        return foundMatch;
    }

    /**
     * 字串是否包含字串集合中的某一个字串
     *
     * @param str      字串
     * @param contains 被包含字串集合
     * @return
     */
    public static boolean stringContainsList(String str, List<String> contains) {
        for (String item : contains) {
            if (str.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否包含数组中的某一项
     */
    public static boolean stringContainsArr(String uri, String[] arr) {
        for (String s : arr) {
            if (uri.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取异常堆栈信息
     *
     * @param e
     * @return
     */
    public static String getStackTraceInfo(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.getBuffer().toString();
    }

    /**
     * 获取指定包名的异常
     *
     * @param e
     * @return
     */
    public static String getPackageException(Throwable e, String packageName) {
        String exception = getStackTraceInfo(e);
        StringBuilder returnStr = new StringBuilder();
        Pattern pattern = Pattern.compile("^.*(" + packageName + "|Exception:|Cause).*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(exception);
        while (matcher.find()) {
            returnStr.append(matcher.group()).append("\n");
        }
        return returnStr.toString();
    }

    /**
     * 对象是否为无效值
     *
     * @param obj 要判断的对象
     * @return 是否为有效值（不为null 和 "" 字符串）
     */
    public static boolean isNullOrEmpty(Object obj) {
        return obj == null || "".equals(obj.toString());
    }

    /**
     * 参数是否是有效整数
     *
     * @param obj 参数（对象将被调用string()转为字符串类型）
     * @return 是否是整数
     */
    public static boolean isInt(Object obj) {
        if (isNullOrEmpty(obj)) {
            return false;
        }
        if (obj instanceof Integer) {
            return true;
        }
        return obj.toString().matches("[-+]?\\d+");
    }

    /**
     * 判断一个对象是否为boolean类型,包括字符串中的true和false
     *
     * @param obj 要判断的对象
     * @return 是否是一个boolean类型
     */
    public static boolean isBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return true;
        }
        String strVal = String.valueOf(obj);
        return "true".equalsIgnoreCase(strVal) || "false".equalsIgnoreCase(strVal);
    }

    /**
     * 对象是否为true
     *
     * @param obj
     * @return
     */
    public static boolean isTrue(Object obj) {
        return "true".equals(String.valueOf(obj));
    }

    public static boolean matches(String regex, String input) {
        return (null != regex && null != input) && Pattern.matches(regex, input);
    }

    /**
     * 生成指定位数的随机数
     *
     * @return
     */
    public static String getRandom(int num) {
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < num; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }

    /**
     * double to String 防止科学计数法
     *
     * @param value
     * @return
     */
    public static String doubleToString(Double value) {
        String temp = value.toString();
        if (temp.contains(EXCEL_F_E)) {
            BigDecimal bigDecimal = new BigDecimal(temp);
            temp = bigDecimal.toPlainString();
        }
        return temp;
    }

}
