package com.cookietest.retrofit;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class StringConverter implements Converter {

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            return IOUtils.toString(body.in());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public TypedOutput toBody(final Object object) {
        return new TypedOutput() {
            @Override
            public String fileName() {
                return null;
            }

            @Override
            public String mimeType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            public long length() {
                return object.toString().length();
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                out.write(object.toString().getBytes());
            }
        };
    }
}
