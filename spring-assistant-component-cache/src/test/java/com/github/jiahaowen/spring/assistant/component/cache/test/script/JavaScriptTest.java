package com.github.jiahaowen.spring.assistant.component.cache.test.script;

import com.github.jiahaowen.spring.assistant.component.cache.script.AbstractScriptParser;
import com.github.jiahaowen.spring.assistant.component.cache.script.JavaScriptParser;
import com.github.jiahaowen.spring.assistant.component.cache.test.Simple;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * @author: jiahaowen
 */
public class JavaScriptTest extends TestCase {

    AbstractScriptParser scriptParser=new JavaScriptParser();

    public void testJavaScript() throws Exception {
        String javaVersion=System.getProperty("java.version");
        System.out.println(javaVersion);
        int ind=0;
        for(int i=0; i < 2; i++) {
            ind=javaVersion.indexOf(".", ind);
            ind++;
        }
        javaVersion=javaVersion.substring(0, ind);
        javaVersion=javaVersion.replaceAll("\\.", "");
        System.out.println(Integer.parseInt(javaVersion));

        String keySpEL="'test_'+args[0]+'_'+args[1]";
        Object[] arguments=new Object[]{"1111", "2222"};
        String res=scriptParser.getDefinedCacheKey(keySpEL, null, arguments, null, false);
        System.out.println(res);
        // 自定义函数使用
        Boolean rv=scriptParser.getElValue("empty(args[0])", null, arguments, Boolean.class);
        assertFalse(rv);
    }

    public void testJavaScript2() throws Exception {

        String keySpEL="'test_'+args[0]+'_'+args[1]";

        Simple simple=new Simple();
        simple.setAge(18);
        simple.setName("刘德华");
        simple.setSex(0);
        Object[] arguments=new Object[]{"1111", "2222", simple};

        String res=scriptParser.getDefinedCacheKey(keySpEL, null, arguments, null, false);
        System.out.println(res);
        assertEquals("test_1111_2222", res);
        // 自定义函数使用
        Boolean rv=scriptParser.getElValue("empty(args[0])", null, arguments, Boolean.class);
        assertFalse(rv);

        String val=null;
        val=scriptParser.getElValue("hash(args[0])", null, arguments, String.class);
        System.out.println(val);
        assertEquals("1111", val);

        val=scriptParser.getElValue("hash(args[1])", null, arguments, String.class);
        System.out.println(val);
        assertEquals("2222", val);

        val=scriptParser.getElValue("hash(args[2])", null, arguments, String.class);
        System.out.println(val);
//        assertEquals("-290203482_-550943035_-57743508_-1052004462", val);

        val=scriptParser.getElValue("hash(args)", null, arguments, String.class);
        System.out.println(val);
//        assertEquals("322960956_-1607969343_673194431_1921252123", val);
    }

    /**
     * @throws Exception
     */
    public void testReturnIsMapWithHfield() throws Exception {

        String keySpEL=" (retVal['rid'])";

        keySpEL="typeof(retVal);";// object
        keySpEL="(typeof retVal['rid'])";// undefined
        keySpEL="typeof retVal.rid";// undefined
        keySpEL="retVal.get('rid')";// undefined

        Object[] arguments=new Object[]{"1111", "2222"};
        Map returnObj=new HashMap(1);
        returnObj.put("rid", "iamrid");
        String res=scriptParser.getDefinedCacheKey(keySpEL, null, arguments, returnObj, true);
        System.out.println(res);

        assertEquals("iamrid", res);

        Simple simple=new Simple();
        simple.setAge(18);
        simple.setName("刘德华");
        simple.setSex(0);
        keySpEL="retVal.name";

        res=scriptParser.getDefinedCacheKey(keySpEL, null, arguments, simple, true);
        System.out.println(res);
        assertEquals("刘德华", res);

        // 自定义函数使用
        Boolean rv=scriptParser.getElValue("empty(args[0])", null, arguments, Boolean.class);
        assertFalse(rv);
    }
}
