package com.cordys.jenkinsci.summareport;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

import static com.cordys.jenkinsci.summareport.helpers.CollectionHelper.getUniqueJobsByName;

public class ReportJobTests extends ReportJobBase {

    @DataBoundConstructor
    public ReportJobTests(String viewName, String sectionName) {
        super(sectionName, viewName);
    }

    @Override
    protected boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, ItemIterable itemIterable, String viewDescription) {
        FailedAndUnstableSeparator failedAndUnstable = new FailedAndUnstableSeparator(itemIterable).invoke();
        AbstractCollection<Job> failedJobs = failedAndUnstable.getFailedJobs();
        AbstractCollection<Job> unstableJobs = failedAndUnstable.getUnstableJobs();


        // Report failing builds
        if (!failedJobs.isEmpty()) {
            printHeading(build, listener.getLogger(), "Failed " + viewDescription + " jobs", RED);

            for (Job job : failedJobs) {
                println(build, listener.getLogger(), getLink(Jenkins.getInstance().getRootUrl() + job.getUrl(), job.getDisplayName()) + BR);

                ReportContext.getInstance(build).addFailingJob(job);
            }
        }

        // Report unstable builds
        if (!unstableJobs.isEmpty()) {
            printHeading(build, listener.getLogger(), "Unstable " + viewDescription + " jobs", YELLOW);

            for (Job job : getUniqueJobsByName(unstableJobs)) {
                println(build, listener.getLogger(), getLink(Jenkins.getInstance().getRootUrl() + job.getUrl(), job.getDisplayName()) + BR);

                if (job.getLastCompletedBuild() instanceof AbstractBuild) {
                    AbstractBuild lastCompletedBuild = (AbstractBuild) job.getLastCompletedBuild();
                    AbstractTestResultAction testResult = lastCompletedBuild.getTestResultAction();
                    if (testResult != null) {
                        println(build, listener.getLogger(), "Failures: " + testResult.getFailCount() + BR, 4);
                        if (testResult.getFailCount() <= 5) //TODO: Property?
                        {
                            println(build, listener.getLogger(), "Details: <BR>", 4);
                            for (CaseResult failedTest : (List<CaseResult>) testResult.getFailedTests()) {
                                println(build, listener.getLogger(), "Age " + failedTest.getAge() + " <A href=\"" + Jenkins.getInstance().getRootUrl() + job.getUrl() + job.getLastCompletedBuild().number + "/testReport" + failedTest.getUrl() + "\">" + failedTest.getSimpleName() + failedTest.getDisplayName() + "</A>:<I> " + truncate(failedTest.getErrorDetails()) + "</I><BR>", 6);
                            }
                        } else {
                            int firstFailure = 0;
                            int repeatedFailure = 0;
                            for (CaseResult failedTest : (List<CaseResult>) testResult.getFailedTests()) {
                                if (failedTest.getAge() == 1) {
                                    firstFailure++;
                                } else {
                                    repeatedFailure++;
                                }
                            }
                            println(build, listener.getLogger(), " <B>First time failures: " + firstFailure + " repeated failures: " + repeatedFailure + "</B><BR>", 2);

                            ReportContext.getInstance(build).addUnstableJob(job, repeatedFailure + firstFailure);
                        }
                    }
                }
            }
        }

        // Report all OK
        if (failedJobs.isEmpty() && unstableJobs.isEmpty()) {
            printHeading(build, listener.getLogger(), "All " + viewDescription + " jobs OK", GREEN);
        }

        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    private static class FailedAndUnstableSeparator {
        private final ItemIterable itemIterable;
        private ArrayList<Job> failedJobs;
        private ArrayList<Job> unstableJobs;

        public FailedAndUnstableSeparator(ItemIterable itemIterable) {
            this.itemIterable = itemIterable;
        }

        public AbstractCollection<Job> getFailedJobs() {
            return failedJobs;
        }

        public AbstractCollection<Job> getUnstableJobs() {
            return unstableJobs;
        }

        public FailedAndUnstableSeparator invoke() {
            List<Job> jobs = Util.filter(itemIterable.getItems(), Job.class);

            failedJobs = new ArrayList<Job>(jobs.size());
            unstableJobs = new ArrayList<Job>(jobs.size());

            for (Job job : jobs) {
                if (job.isBuildable() && job.getLastCompletedBuild() != null) {
                    Result lastResult = job.getLastCompletedBuild().getResult();
                    if (Result.FAILURE.equals(lastResult)) {
                        failedJobs.add(job);
                    } else if (Result.UNSTABLE.equals(lastResult)) {
                        unstableJobs.add(job);
                    }
                }

            }
            return this;
        }
    }


}
