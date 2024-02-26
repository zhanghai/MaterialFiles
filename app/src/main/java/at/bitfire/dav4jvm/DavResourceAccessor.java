/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package at.bitfire.dav4jvm;

import java.io.IOException;

import androidx.annotation.NonNull;
import at.bitfire.dav4jvm.exception.DavException;
import at.bitfire.dav4jvm.exception.HttpException;
import kotlin.jvm.functions.Function0;
import okhttp3.Response;

public class DavResourceAccessor {
    private DavResourceAccessor() {}

    public static void checkStatus(@NonNull DavResource davResource, @NonNull Response response)
            throws HttpException {
        davResource.checkStatus(response);
    }

    public static Response followRedirects(@NonNull DavResource davResource,
            @NonNull Function0<Response> sendRequest) throws DavException, IOException {
        return davResource.followRedirects$build(sendRequest);
    }
}
