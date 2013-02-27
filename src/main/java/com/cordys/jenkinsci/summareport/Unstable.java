package com.cordys.jenkinsci.summareport;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.AbstractTestResultAction;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class Unstable extends Recorder {

    private final int numberOfFailingTestCases;

    @DataBoundConstructor
    public Unstable(int numberOfFailingTestCases) {
        this.numberOfFailingTestCases = numberOfFailingTestCases;
    }

    public int getNumberOfFailingTestCases() {
        return numberOfFailingTestCases;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        final hudson.tasks.test.TestResult result = new hudson.tasks.test.SimpleCaseResult() {
            /**
             * Gets the total number of failed tests.
             */
            @Override
            public int getFailCount() {
                return numberOfFailingTestCases;
            }

            /**
             * Gets the total number of tests.
             */
            @Override
            public int getTotalCount() {
                return getFailCount() + getSkipCount() + getPassCount();
            }
        };


        build.getActions().add(new AbstractTestResultAction(build) {

            /**
             * Gets the number of failed tests.
             */
            @Override
            public int getFailCount() {
                return result.getFailCount();  //To change body of implemented methods use File | Settings | File Templates.
            }

            /**
             * Gets the total number of tests.
             */
            @Override
            public int getTotalCount() {
                return result.getTotalCount();  //To change body of implemented methods use File | Settings | File Templates.
            }

            /**
             * Returns the object that represents the actual test result.
             * This method is used by the remote API so that the XML/JSON
             * that we are sending won't contain unnecessary indirection
             * (that is, {@link hudson.tasks.test.AbstractTestResultAction} in between.
             * <p/>
             * <p/>
             * If such a concept doesn't make sense for a particular subtype,
             * return <tt>this</tt>.
             */
            @Override
            public Object getResult() {
                return this;
            }
        });


        build.setResult(Result.UNSTABLE);
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Report failure";
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
