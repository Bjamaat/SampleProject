package com.sampleProject.service.models;

import com.adobe.cq.sightly.WCMUse;
import com.day.cq.wcm.api.Page;
import com.sampleProject.service.utils.BreadcrumbsUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// It was other option to use slingModel which is more prefered in aem 6.2/6.3
public class BreadcrumbsModel extends WCMUse {

    private static final Logger log = LoggerFactory.getLogger(BreadcrumbsModel.class);

    private List<Breadcrumb> breadcrumbs = new ArrayList<>();

    @Override
    public void activate() throws Exception {
        log.debug("breadcrumb starts...");

        // Here we will get multifield of pathbrowser of author to exclude those pages
        List<String> excludePages = getProperties("excludePages");

        // Suppose that we are doing it in Max 5 level
        // 1 > 2 > 3 > 4 > 5
        int level = getConfig("rootPath", 0, Integer.class);

        int currentLevel = getCurrentPage().getDepth();

        while (level < 5) {
            Page currPage = getCurrentPage().getAbsoluteParent(level);
            if (currPage == null) {
                break;
            }
            ValueMap valueMap = currPage.getProperties();

            // BreadcrumbsUtils should help to find more information about the node properties

            String title = BreadcrumbsUtils.getPageOrDirInformation(currPage.getPageTitle(), currPage.getTitle());

            //Is it last page node or not (which is 5 level here)
            boolean leaf = false;

            if (level == (currentLevel - 1)) {
                leaf = true;
            }
            // with currPage we have page resource object and we get path of it
            Breadcrumb bc = new Breadcrumb(currPage.getPath() + ".html", title, leaf);
            // if it is not directory/without title/ it is not in multified list we will add it to breadcrumbs
            if (!shouldSkip(currPage, valueMap, bc) && !isExcludePages(excludePages, currPage.getPath())) {
                breadcrumbs.add(bc);
            }

            level++;
        }

        log.debug("Created breadcrumb {}", breadcrumbs);
    }

    /**
     * @param pageTitle
     * @param title
     * @return result
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

    /**
     *  It will define that if current node is missing the title or
     *  if node is not page and is directory then we will skip dir to show with isDirectory equal true
     *
     * @param page
     * @param valueMap
     * @param bc
     * @return
     */
    private boolean shouldSkip(Page page, ValueMap valueMap, Breadcrumb bc) {
        // Does item has title
        if (StringUtils.isBlank(bc.getText())) {
            log.debug("Has no title. Skipping breadcrumb for {}", page.getPath());
            return true;
        }
        // Does item has 'isDirectory' properties true
        if (BooleanUtils.toBoolean(valueMap.get("isDirectory", Boolean.class))) {
            log.debug("isDirectory = true, Skipping breadcrumb for {}", page.getPath());
            return true;
        }
        return false;
    }

    /**
     * If it is in the multifield list we will set flag to true
     * @param excludePages
     * @param pagePath
     * @return
     */
    private boolean isExcludePages(List<String> excludePages, String pagePath) {
        // is it in excludePages list or not
        boolean isExclude = false;
        int num = excludePages.size();
        while (num > 0 && isExclude == false) {
            if (excludePages.get(num) == pagePath) {
                isExclude = true;
                break;
            }
            num --;
        }
        return isExclude;
    }

    public List<Breadcrumb> getBreadcrumbs() {
        return breadcrumbs;
    }

    /**
     * Get property with preference of current node
     *
     * @param key
     * @param defaultValue
     * @param intClass
     * @return
     */
    private final <T> T getConfig(String key, T defaultValue, Class<T> intClass) {
        T nodeProp = getProperties().get(key, defaultValue);
        if (nodeProp != null) {
            if (intClass.equals(String.class)) {
                if (!StringUtils.isBlank(nodeProp + "")) {
                    return nodeProp;
                }
            } else {
                return nodeProp;
            }
        }

        T styleProp = getCurrentStyle().get(key, defaultValue);
        return styleProp;
    }

    /**
     * Get breadcrumb object
     */
    public static final class Breadcrumb {
        private final String path;
        private final String text;
        private final boolean leaf;

        public Breadcrumb(String path, String text, boolean leaf) {
            this.path = path;
            this.text = text;
            this.leaf = leaf;
        }

        public Breadcrumb(String path, String text) {
            this(path, text, false);
        }

        public String getPath() {
            return path;
        }

        public String getText() {
            return text;
        }

        public boolean isLeaf() {
            return leaf;
        }

        @Override
        public String toString() {
            return "Breadcrumb [path=" + path + ", text=" + text + ", leaf=" + leaf + "]";
        }

    }

}