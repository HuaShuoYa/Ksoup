package com.library.converter;

import com.library.JsoupUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class HtmlConverterFactory extends Converter.Factory {

    private JsoupUtils mPicker;

    public static HtmlConverterFactory create(JsoupUtils fruit) {
        return new HtmlConverterFactory(fruit);
    }

    public static HtmlConverterFactory create() {
        return new HtmlConverterFactory(new JsoupUtils());
    }

    private HtmlConverterFactory(JsoupUtils fruit) {
        mPicker = fruit;
    }

    //我们只需要对返回值做修改,所以仅重写responseBodyConverter方法
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {
        return new HtmlResponseBodyConverter<>(mPicker, type);
    }
}
