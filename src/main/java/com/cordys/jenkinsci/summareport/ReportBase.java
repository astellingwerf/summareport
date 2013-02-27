package com.cordys.jenkinsci.summareport;

import hudson.Plugin;
import hudson.model.AbstractBuild;
import hudson.model.View;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;

import java.awt.*;
import java.io.PrintStream;

import static org.apache.commons.lang.StringUtils.repeat;


public class ReportBase extends Builder {

    public static final String NBSP = "&nbsp;";
    public static final String BR = "<BR>";
    protected static final Color RED = new Color(0xCC0000);
    protected static final Color GREEN = new Color(0x73d216);
    protected static final Color YELLOW = new Color(0xedd400);

    protected void printHeading(AbstractBuild<?, ?> build, PrintStream output, String text, Color color) {
        println(build, output, "<H2 style=\"color:" + getRGBString(color) + ";\">" + text + "</H2>");
    }

    private String getRGBString(Color color) {
        return "#" + String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    protected void println(AbstractBuild<?, ?> build, PrintStream output, String text) {
        println(build, output, text, 0);
    }

    protected void println(AbstractBuild<?, ?> build, PrintStream output, String text, int leadingSpaces) {
        output.print(repeat(NBSP, leadingSpaces));
        output.println(text);

        ReportContext.getInstance(build).append(repeat(NBSP, leadingSpaces)).append(text);
    }

    protected String getLink(String url, String text) {
        return "<A href=\"" + url + "\">" + text + "</A> ";
    }

    protected String truncate(String string) {
        if (string != null) {
            string = string.replace("<", "&lt;");
            string = string.replace(">", "&gt;");
            string = string.replace("\n", BR);
            if (string.length() > 300) {     //TODO: Property?
                return string.substring(0, 300) + " .. truncated ..";
            }
        }
        return string;
    }

    protected class Items {
        private final String viewName;
        private final String sectionName;
        private ItemIterable itemIterable;
        private String viewDescription;

        protected Items(String viewName, String sectionName) {
            this.viewName = viewName;
            this.sectionName = sectionName;
        }

        public ItemIterable getItemIterable() {
            return itemIterable;
        }

        public String getViewDescription() {
            return viewDescription;
        }

        public Items invoke() {
            View view = Jenkins.getInstance().getView(viewName);
            itemIterable = new ItemIterable.ViewWrapper(view);

            viewDescription = viewName;
            final Plugin plugin = Jenkins.getInstance().getPlugin("sectioned-view");
            if (plugin != null) {

                ItemIterable section = SectionedViewExtension.getSection(view, sectionName);
                if (section != null) {
                    itemIterable = section;
                    viewDescription += " " + sectionName;
                }
            }
            return this;
        }
    }
}
