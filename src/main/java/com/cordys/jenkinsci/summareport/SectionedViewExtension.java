package com.cordys.jenkinsci.summareport;

import hudson.model.View;

import static com.cordys.jenkinsci.summareport.helpers.ReflectionHelper.invokeGetter;

public class SectionedViewExtension {
    private SectionedViewExtension() {
        // static access only
    }

    public static ItemIterable getSection(View sectionedView, String sectionName) {
        if (sectionName != null && sectionedView.getClass().getCanonicalName().equals("hudson.plugins.sectioned_view.SectionedView")) {
            try {
                ItemIterable result = null;
                final Iterable<Object> sections = invokeGetter("sections", sectionedView);
                for (Object v : sections) {
                    if (invokeGetter("name", v).equals(sectionName)) {
                        if (result != null)
                            return null;
                        result = new ItemIterable.SectionedListWrapper(v);
                    }
                }
                return result;
            } catch (Exception e) {
                // NOPMD
            }
        }
        return null;
    }

}
