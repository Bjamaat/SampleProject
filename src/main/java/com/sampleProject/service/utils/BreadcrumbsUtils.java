package com.sampleProject.service.utils;

import java.io.IOException;
// if we have some share method which is useful in other classes it is good to keep in util like this one or others
// for now I have added to the Model.class

public class BreadcrumbsUtils {

    private BreadcrumbsUtils() {
        super();
    }

    /**
     * @param pageTitle
     * @param title
     * @return
     */
    public static String getPageOrDirInformation(final String pageTitle, final String title) throws IOException {
        String result = null;
        if (pageTitle != null) {
            result = pageTitle;
        } else {
            result = title;
        }
        return result;
    }

}
