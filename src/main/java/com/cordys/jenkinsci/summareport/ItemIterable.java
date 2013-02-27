package com.cordys.jenkinsci.summareport;

import hudson.model.TopLevelItem;
import hudson.model.View;

import java.util.Collection;

import static com.cordys.jenkinsci.summareport.helpers.ReflectionHelper.invokeGetter;

public abstract class ItemIterable {

    public abstract Collection<TopLevelItem> getItems();

    public static class ViewWrapper extends ItemIterable {
        final View inner;

        public ViewWrapper(View inner) {
            this.inner = inner;
        }

        @Override
        public Collection<TopLevelItem> getItems() {
            return inner.getItems();
        }
    }

    public static class SectionedListWrapper extends ItemIterable {
        final Object inner;

        public SectionedListWrapper(Object inner) {
            this.inner = inner;
        }

        @Override
        public Collection<TopLevelItem> getItems() {
            try {
                return invokeGetter("items", inner);
            } catch (Exception e) {
                // NOPMD
            }
            return null;
        }
    }
}
