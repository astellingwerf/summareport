package com.cordys.jenkinsci.summareport;

import com.cordys.jenkinsci.summareport.helpers.CollectionHelper;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.Interval;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.GregorianCalendar;

public class ReportJobStall extends ReportJobBase {

    @DataBoundConstructor
    public ReportJobStall(String viewName, String sectionName) {
        super(sectionName, viewName);
    }

    @Override
    protected boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, ItemIterable itemIterable, String viewDescription) {

        final Collection<Job> collection = CollectionUtils.select(itemIterable.getItems(), new CollectionHelper.Predicate<TopLevelItem>() {
            @Override
            public boolean eval(TopLevelItem object) {
                if (!(object instanceof Job))
                    return false;
                Job job = (Job)object;
                          new Interval(job.getLastCompletedBuild()   .getTimeInMillis(), new GregorianCalendar().getTimeInMillis()).;
            }
        });

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


}
