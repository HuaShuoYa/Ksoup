package com.library.converter;

import com.library.JsoupUtils;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class HtmlResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private JsoupUtils mPicker;
    private Type mType;

    HtmlResponseBodyConverter(JsoupUtils fruit, Type type) {
        mPicker = fruit;
        mType = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            String response = value.string();
            return mPicker.fromHTML(response, (Class<T>) mType);
        } finally {
            value.close();
        }
    }
}
