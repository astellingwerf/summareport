package com.cordys.jenkinsci.summareport;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public abstract class ReportJobBase extends ReportBase {
    protected final String viewName;
    protected final String sectionName;

    public ReportJobBase(String sectionName, String viewName) {
        this.sectionName = sectionName;
        this.viewName = viewName;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Items items = new Items(viewName, sectionName).invoke();


        return perform(build, launcher, listener, items.getItemIterable(), items.getViewDescription());
    }

    protected abstract boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, ItemIterable itemIterable, String viewDescription);

    public String getSectionName() {
        return sectionName;
    }

    public String getViewName() {
        return viewName;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Report status of jobs";
        }

        /**
         * Invoked when the global configuration page is submitted.
         * <p/>
         * Can be overriden to store descriptor-specific information.
         *
         * @param json The JSON object that captures the configuration data for this {@link hudson.model.Descriptor}.
         *             See http://wiki.jenkins-ci.org/display/JENKINS/Structured+Form+Submission
         * @return false
         *         to keep the client in the same config page.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return super.configure(req, json);
        }
    }
}
