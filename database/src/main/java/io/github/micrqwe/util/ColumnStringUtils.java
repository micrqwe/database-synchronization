package io.github.micrqwe.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shaowenxing@cnstrong.cn
 * @since 13:38
 */
public class ColumnStringUtils {
    private static Logger logger = LoggerFactory.getLogger(ColumnStringUtils.class);
    private static final HashMap<String, String> sqlTokens;
    private static Pattern sqlTokenPattern;

    static {
        //MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
        String[][] search_regex_replacement = new String[][]
                {
                        //search string     search regex        sql replacement regex
                        {"\u0000", "\\x00", "\\\\0"},
                        {"'", "'", "\\\\'"},
                        {"\"", "\"", "\\\\\""},
                        {"\b", "\\x08", "\\\\b"},
                        {"\n", "\\n", "\\\\n"},
                        {"\r", "\\r", "\\\\r"},
                        {"\t", "\\t", "\\\\t"},
                        {"\u001A", "\\x1A", "\\\\Z"},
                        {"\\", "\\\\", "\\\\\\\\"}
                };

        sqlTokens = new HashMap<String, String>();
        String patternStr = "";
        for (String[] srr : search_regex_replacement) {
            sqlTokens.put(srr[0], srr[2]);
            patternStr += (patternStr.isEmpty() ? "" : "|") + srr[1];
        }
        sqlTokenPattern = Pattern.compile('(' + patternStr + ')');
    }

    public static String escape(String s) {
        Matcher matcher = sqlTokenPattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static void columeAppend(StringBuilder stringBuffer, int sqlType, String value) {
        if (StringUtils.isEmpty(value)) {
            stringBuffer.append("NULL");
            return;
        }
        if (sqlType == JDBCType.INTEGER.getVendorTypeNumber() ||
                sqlType == JDBCType.BIGINT.getVendorTypeNumber() ||
                sqlType == JDBCType.TINYINT.getVendorTypeNumber() ||
                sqlType == JDBCType.FLOAT.getVendorTypeNumber() ||
                sqlType == JDBCType.REAL.getVendorTypeNumber() ||
                sqlType == JDBCType.NUMERIC.getVendorTypeNumber() ||
                sqlType == JDBCType.DOUBLE.getVendorTypeNumber()) {
            stringBuffer.append(value);
            return;
        }
        stringBuffer.append("'");
        stringBuffer.append(escape(value));
        stringBuffer.append("'");
    }

    public static void close(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("数据库关闭出现错误");
        }
    }
}
