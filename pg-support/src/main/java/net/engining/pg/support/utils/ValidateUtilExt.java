package net.engining.pg.support.utils;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;


/**
 * 参数检查工具.
 *
 * @see org.apache.commons.lang3.Validate
 */
public abstract class ValidateUtilExt extends Validate {

    /**
     * 邮箱
     */
    private static final String REG_EMAIL = "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$";
    /**
     * 固话
     */
    private static final String REG_PHONE = "\\d{3}-\\d{8}|\\d{4}-\\d{7}";
    /**
     * 手机号码
     */
    private static final String REG_MOBILE = "0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-9])[0-9]{8}";
    /**
     * url
     */
    private static final String REG_URL = "^((https|http|ftp|rtsp|mms)://)?[A-Za-z0-9]+\\.[A-Za-z0-9]+[\\/=\\?%\\-&_~`@\\':+!]*([^<>\\\"\\\"])*$";
    /**
     * 身份证号码
     */
    private static final String REG_IDCARD = "\\d{15}|\\d{18}";
    /**
     * 是否是数字
     */
    private static final String REG_NUMBER = "\\d+";
    /**
     * 邮编
     */
    private static final String REG_ZIP = "^[1-9]\\d{5}$";
    /**
     * QQ
     */
    private static final String REG_QQ = "[1-9]\\d{4,13}";
    /**
     * 整数
     */
    private static final String REG_INTEGER = "[-\\+]?\\d+";
    /**
     * 正整数
     */
    private static final String REG_INTEGER_P = "^[1-9]\\d*$";
    /**
     * 负整数
     */
    private static final String REG_INTEGER_N = "^-[1-9]\\d*$";
    /**
     * 浮点数
     */
    private static final String REG_FLOAT = "^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$";
    /**
     * 正浮点数
     */
    private static final String REG_FLOAT_P = "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";
    /**
     * 负浮点数
     */
    private static final String REG_FLOAT_N = "^-([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*)$";
    /**
     * 小数
     */
    private static final String REG_DOUBLE = "[-\\+]?\\d+(\\.\\d+)?";
    /**
     * 英文
     */
    private static final String REG_ENGLISH = "^[A-Za-z]+$";
    /**
     * 大写英文
     */
    private static final String REG_ENGLISH_UPPER = "^[A-Z]+$";
    /**
     * 小写英文
     */
    private static final String REG_ENGLISH_LOWER = "^[a-z]+$";
    /**
     * 英文字母或数字
     */
    private static final String REG_ENGLISH_DIGIT = "^[A-Za-z0-9]+$";
    /**
     * 数字或字母或下划线
     */
    private static final String REG_ENGLISH_DIGIT_LINE = "^\\w+$";
    /**
     * 中文
     */
    private static final String REG_CHINESE = "^[\\u0391-\\uFFE5]+$";
    /**
     * IP
     */
    private static final String REG_IP =
        "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])";
    /**
     * 金额
     */
    private static final String REG_MONEY = "^(([1-9]\\\\d{0,9})|0)(\\\\.\\\\d{1,2})?$";

    /**
     * 私有化构造器
     */
    private ValidateUtilExt() {
    }

    /**
     * 判断是否是邮箱地址
     */
    public static void isEmail(String email) {
        //不区分大小写
        Pattern pattern = Pattern.compile(REG_EMAIL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        /**
         * 注意find与matches()的区别
         * 调用find方法，只执行尽量匹配
         * 而调用matches方法，则执行严格的匹配
         */
        isTrue(StringUtils.isNotEmpty(email) && matcher.matches(), "邮箱格式错误.");
    }

    /**
     * 判断是否是电话号码,电话(固话)
     */
    public static void isPhone(String phone) {
        Pattern patter = Pattern.compile(REG_PHONE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(phone);

        isTrue(StringUtils.isNotEmpty(phone) && matcher.matches(), "固定电话格式错误.");
    }

    /**
     * 判断是否是手机号码
     */
    public static void isMobile(String mobile) {
        Pattern patter = Pattern.compile(REG_MOBILE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(mobile);

        isTrue(StringUtils.isNotEmpty(mobile) && matcher.matches(), "移动电话格式错误.");
    }

    /**
     * 判断是否是正确的url
     */
    public static void isUrl(String url) {
        Pattern patter = Pattern.compile(REG_URL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(url);

        isTrue(StringUtils.isNotEmpty(url) && matcher.matches(), "URL路径格式错误.");
    }

    /**
     * 判断是否是合法的身份证号码
     */
    public static void isIdCard(String idCard) {
        Pattern patter = Pattern.compile(REG_IDCARD, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(idCard);

        isTrue(StringUtils.isNotEmpty(idCard) && matcher.matches(), "身份证号码格式错误.");
    }

    /**
     * 判断是否是数字
     */
    public static void isNumber(String number) {
        Pattern patter = Pattern.compile(REG_NUMBER, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(number);

        isTrue(StringUtils.isNotEmpty(number) && matcher.matches(), "非全为数字.");
    }

    /**
     * 判断邮编是否合法
     */
    public static void isZip(String zip) {
        Pattern patter = Pattern.compile(REG_ZIP, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(zip);

        isTrue(StringUtils.isNotEmpty(zip) && matcher.matches(), "邮编格式错误.");
    }

    /**
     * 判断QQ号是否合法
     */
    public static void isQq(String qq) {
        Pattern patter = Pattern.compile(REG_QQ, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(qq);

        isTrue(StringUtils.isNotEmpty(qq) && matcher.matches(), "QQ格式错误.");
    }

    /**
     * 判断是否是整数
     */
    public static void isInteger(String integer) {
        Pattern patter = Pattern.compile(REG_INTEGER, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(integer);

        isTrue(StringUtils.isNotEmpty(integer) && matcher.matches(), "非整形数字.");
    }
    
    /**
     * 判断是否是正整数
     */
    public static void isPositiveInteger(String integer) {
        Pattern patter = Pattern.compile(REG_INTEGER_P, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(integer);

        isTrue(StringUtils.isNotEmpty(integer) && matcher.matches(), "非正整形数字.");
    }
    
    /**
     * 判断是否是负整数
     */
    public static void isNegativeInteger(String integer) {
        Pattern patter = Pattern.compile(REG_INTEGER_N, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(integer);

        isTrue(StringUtils.isNotEmpty(integer) && matcher.matches(), "非负整形数字.");
    }

    /**
     * 判断是否是浮点数
     */
    public static void isFloat(String str) {
        Pattern patter = Pattern.compile(REG_FLOAT, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(str);

        isTrue(StringUtils.isNotEmpty(str) && matcher.matches(), "非浮点数.");
    }
    
    /**
     * 判断是否是正浮点数
     */
    public static void isPositiveFloat(String str) {
        Pattern patter = Pattern.compile(REG_FLOAT_P, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(str);

        isTrue(StringUtils.isNotEmpty(str) && matcher.matches(), "非正浮点数.");
    }
    
    /**
     * 判断是否是负浮点数
     */
    public static void isNegativeFloat(String str) {
        Pattern patter = Pattern.compile(REG_FLOAT_N, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(str);

        isTrue(StringUtils.isNotEmpty(str) && matcher.matches(), "非负浮点数.");
    }
    
    /**
     * 判断是否是小数
     */
    public static void isDouble(String str) {
        Pattern patter = Pattern.compile(REG_DOUBLE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(str);

        isTrue(StringUtils.isNotEmpty(str) && matcher.matches(), "非小数数字.");
    }

    /**
     * 判断是否是英文
     */
    public static void isEnglish(String english) {
        Pattern patter = Pattern.compile(REG_ENGLISH, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(english);

        isTrue(StringUtils.isNotEmpty(english) && matcher.matches(), "非全为英文字母.");
    }
    
    /**
     * 判断是否是大写英文
     */
    public static void isUpperEnglish(String english) {
        Pattern patter = Pattern.compile(REG_ENGLISH_UPPER);
        Matcher matcher = patter.matcher(english);

        isTrue(StringUtils.isNotEmpty(english) && matcher.matches(), "非全为大写英文字母.");
    }
    
    /**
     * 判断是否是小写英文
     */
    public static void isLowerEnglish(String english) {
        Pattern patter = Pattern.compile(REG_ENGLISH_LOWER);
        Matcher matcher = patter.matcher(english);

        isTrue(StringUtils.isNotEmpty(english) && matcher.matches(), "非全为小写英文字母.");
    }
    
    /**
     * 判断是否是英文字母或数字
     */
    public static void isEnglishAndDigit(String english) {
        Pattern patter = Pattern.compile(REG_ENGLISH_DIGIT, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(english);

        isTrue(StringUtils.isNotEmpty(english) && matcher.matches(), "非全为英文字母或数字.");
    }
    
    /**
     * 判断是否是数字或英文字母或下划线
     */
    public static void isEnglishAndDigitAndLine(String english) {
        Pattern patter = Pattern.compile(REG_ENGLISH_DIGIT_LINE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(english);

        isTrue(StringUtils.isNotEmpty(english) && matcher.matches(), "非全为数字或英文字母或下划线.");
    }

    /**
     * 判断是否全为汉子
     */
    public static void isChinese(String chinese) {
        Pattern patter = Pattern.compile(REG_CHINESE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(chinese);

        isTrue(StringUtils.isNotEmpty(chinese) && matcher.matches(), "非全为汉字.");
    }

    /**
     * 判断IP是否合法
     *
     * @param ip
     * @return
     */
    public static void isIp(String ip) {
        Pattern patter = Pattern.compile(REG_IP, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(ip);

        isTrue(StringUtils.isNotEmpty(ip) && matcher.matches(), "IP格式错误.");
    }
    
    /**
     * 判断是否金额
     *
     * @param ip
     * @return
     */
    public static void isMoney(String money) {
        Pattern patter = Pattern.compile(REG_MONEY, Pattern.CASE_INSENSITIVE);
        Matcher matcher = patter.matcher(money);

        isTrue(StringUtils.isNotEmpty(money) && matcher.matches(), "金额格式错误.");
    }
    
    /**
     * 检查对象是否是不为null 或 空.
     */
    public static boolean isNotNullOrEmpty(Object value) {
        return !isNullOrEmpty(value);
    }

    /**
     * 检查对象是否是null 或 空， 支持对象，字符串，集合
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        // 字符串
        if (obj instanceof CharSequence) {
            return StringUtils.isBlank((CharSequence) obj);
        }

        // collections 支持的类型
        if (isCollectionsSupportType(obj)) {
            return CollectionUtils.sizeIsEmpty(obj);
        }
        return false;
    }

    /**
     * 检查是否是集合类型
     *
     * @return true：是集合类型; false: 不是集合类型
     */
    private static boolean isCollectionsSupportType(Object obj) {
        return obj instanceof Collection // 集合
            || obj instanceof Map// map
            || obj instanceof Enumeration // 枚举
            || obj instanceof Iterator// Iterator迭代器
            || obj.getClass().isArray()//判断数组
            ;
    }

}
