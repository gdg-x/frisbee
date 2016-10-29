package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.plus.ImageInfo;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class PlusImageUrlConverter implements Interceptor {

    private static final Pattern mPlusPattern
        = Pattern.compile("http[s]?://plus\\..*google\\.com.*(\\+[a-zA-Z] +|[0-9]{21}).*");
    private final PlusApi plusApi;

    public PlusImageUrlConverter(PlusApi plusApi) {
        this.plusApi = plusApi;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Matcher matcher = mPlusPattern.matcher(request.url().toString());
        if (!matcher.matches()) {
            return chain.proceed(request);
        }

        String gplusId = matcher.group(1);
        retrofit2.Response<ImageInfo> imageInfoResponse = plusApi.getImageInfo(gplusId).execute();
        if (imageInfoResponse.isSuccessful()) {
            ImageInfo imageInfo = imageInfoResponse.body();
            if (imageInfo.getImage() != null && imageInfo.getImage().getUrl() != null) {
                String imageUrl = imageInfo.getImage().getUrl().replace("sz=50", "sz=196");
                return chain.proceed(request.newBuilder().url(imageUrl).build());
            }
        }

        return null;
    }
}
