package com.cookietest.retrofit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.LinkedList;

import com.cookietest.csv.DataRow;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class CsvConverter implements Converter {

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            Reader reader = new InputStreamReader(body.in());
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                line.length();
            }
        } catch (IOException e) {}
        return new LinkedList<DataRow>();
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
