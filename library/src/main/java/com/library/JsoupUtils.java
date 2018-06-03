package com.library;

import com.library.annontation.Pick;
import com.library.constant.Attrs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsoupUtils {

    public <T> T fromHTML(String html, Class<T> clazz) {
        T t = null;
        Pick pickClazz;
        try {
            //先用Jsoup实例化待解析的字符串
            Document rootDocument = Jsoup.parse(html);
            //获取实体类的的注解
            pickClazz = clazz.getAnnotation(Pick.class);
            //构建一个实体类的无参构造方法并生成实例
            t = clazz.getConstructor().newInstance();
            //获取注解的一些参数
            String clazzAttr = pickClazz.attr();
            String clazzValue = pickClazz.value();
            //用Jsoup选择到待解析的节点
            Element rootNode = getRootNode(rootDocument, clazzValue);
            //获取实体类的所有成员变量
            Field[] fields = clazz.getDeclaredFields();
            //遍历并解析这些成员变量
            for (Field field : fields) {
                dealFieldType(field, rootNode, t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    private Field dealFieldType(Field field, Element rootNode, Object t) throws Exception {
        //设置成员变量为可修改的
        field.setAccessible(true);
        Pick pickField = field.getAnnotation(Pick.class);
        if (pickField == null) return null;
        String fieldValue = pickField.value();
        String fieldAttr = pickField.attr();
        //获取field的类型
        Class<?> type = field.getType();
        //目前此工具类只能解析两种类型的成员变量,一种是String的,另一种是带泛型参数的List,泛型参数必须是自定义
        //子实体类,或者String,自定义子实体类如果是内部类,必须用public static修饰
        if (type == String.class) {
            String nodeValue = getStringNode(rootNode, fieldAttr, fieldValue);
            field.set(t, nodeValue);
        } else if (type == List.class) {
            Elements elements = getListNode(rootNode, fieldValue);
            field.set(t, new ArrayList<>());
            List<Object> fieldList = (List<Object>) field.get(t);
            for (Element ele : elements) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
                    Class<?> aClass = Class.forName(((Class) args[0]).getName());
                    Object object = aClass.newInstance();
                    Field[] childFields = aClass.getDeclaredFields();
                    for (Field childField : childFields) {
                        dealFieldType(childField, ele, object);
                    }
                    fieldList.add(object);
                }
            }
            field.set(t, fieldList);
        }
        return field;
    }

    /**
     * 获取一个Elements对象
     */
    private Elements getListNode(Element rootNode, String fieldValue) {
        return rootNode.select(fieldValue);
    }

    /**
     * 获取返回值为String的节点
     * <p>
     * 由于Jsoup不支持JQuery的一些语法结构,例如  :first  :last,所以这里手动处理了下,自己可参考JQuery选择器
     * 扩展其功能
     */
    private String getStringNode(Element rootNode, String fieldAttr, String fieldValue) {
        if (fieldValue.contains(":first")) {
            fieldValue = fieldValue.replace(":first", "");
            if (Attrs.TEXT.equals(fieldAttr))
                return rootNode.select(fieldValue).first().text();
            return rootNode.select(fieldValue).first().attr(fieldAttr);
        } else if (fieldValue.contains(":last")) {
            fieldValue = fieldValue.replace(":last", "");
            if (Attrs.TEXT.equals(fieldAttr))
                return rootNode.select(fieldValue).last().text();
            return rootNode.select(fieldValue).last().attr(fieldAttr);
        } else {
            if (Attrs.TEXT.equals(fieldAttr))
                return rootNode.select(fieldValue).text();
            return rootNode.select(fieldValue).attr(fieldAttr);
        }
    }

    /**
     * 获取根节点,通常在类的注解上使用
     */
    private Element getRootNode(Document rootDocument, String value) {
        return rootDocument.selectFirst(value);
    }
}
