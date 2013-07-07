package org.gdg.frisbee.android.api;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 19:31
 * To change this template use File | Settings | File Templates.
 */
public class JsoupRequest<T> extends GdgRequest<T> {

    private ParseListener<T> mParseListener;

    public JsoupRequest(int method,
                       String url,
                       ParseListener<T> parseListener,
                       Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        mParseListener = parseListener;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
        try {
            String html = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
            return Response.success(mParseListener.parse(Jsoup.parse(html)),
                    HttpHeaderParser.parseCacheHeaders(networkResponse));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    public interface ParseListener<T> {
        T parse(Document doc);
    }
}
