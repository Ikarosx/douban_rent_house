package cn.ikarosx.job;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 许培宇
 * @since 2023/4/11 11:11
 */
class DoubanRentHouseJobTest {


    @Test
    void testIdentifyPrice() {
        String testString = "要去上海工作了 急需转租福田中心区岗厦地铁站 彩天名苑小区电梯大单间3280豪华装修 智能家电 押一付一";
        List<String> patterns = Arrays.asList("(\\d{1,5})元/月", "(\\d{1,5})/月", "租金(\\d{1,5})(?!\\d)", "租(\\d{1,5})(?!\\d)", "(\\d{1,5})每月");
        for (String pattern : patterns) {
            Pattern compile = Pattern.compile(pattern);
            Matcher matcher = compile.matcher(testString);
            if (matcher.find()) {
                System.out.println("find1 ");
                System.out.println(matcher.group(1));
            }

        }
        // 兜底,匹配4位数
        Pattern compile = Pattern.compile("(?<!\\d)(\\d{4})(?!\\d)");
        Matcher matcher = compile.matcher(testString);
        if (matcher.find()) {
            System.out.println("find2 ");
            System.out.println(matcher.group(1));
        }
    }

    @Test
    void testCount() {
        String test = "1333-5555a1236";
        Pattern compile = Pattern.compile("(?<![\\d\\-])(\\d{4})(?![\\d\\-])");
        Matcher matcher = compile.matcher(test);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        }
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme