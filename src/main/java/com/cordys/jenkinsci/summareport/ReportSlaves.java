package com.cordys.jenkinsci.summareport;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.labels.LabelAtom;
import hudson.node_monitors.DiskSpaceHelper;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.cordys.jenkinsci.summareport.helpers.CollectionHelper.Closure;
import static com.cordys.jenkinsci.summareport.helpers.CollectionHelper.Predicate;
import static org.apache.commons.collections.CollectionUtils.forAllDo;
import static org.apache.commons.collections.CollectionUtils.select;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ReportSlaves extends ReportBase {

    private final Label labels;

    @DataBoundConstructor
    public ReportSlaves(String labels) throws ANTLRException {
        this.labels = Label.parseExpression(labels);
    }

    private static String getNodeDescription(Node node) {
        if (isNotBlank(node.getNodeDescription())) {
            return " Description: " + node.getNodeDescription();
        }
        return "";
    }

    private static String getLabelsHTML(String labels) {
        String[] labelSet = labels.split("\\s+");

        if (labelSet.length > 1 || isNotBlank(labelSet[0])) {
            String returnValue = " Labels: ";
            for (String label : labelSet) {
                returnValue += "[" + label + "] ";
            }
            return returnValue;
        }
        return "";
    }

    private String offlineCause(hudson.slaves.OfflineCause cause) {
        if (cause != null) {
            String reason = DiskSpaceHelper.getOfflineCause(cause);
            if (reason != null)
                return BR + StringUtils.repeat(NBSP, 4) + reason;
            return BR + StringUtils.repeat(NBSP, 4) + cause.toString();
        }
        return "";
    }

    public String getLabels() {
        return labels.getExpression();
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        final List<Node> allNodes = Jenkins.getInstance().getNodes();
        final Collection<Node> offlineNodesMatchingLabels = select(allNodes, new Predicate<Node>() {
            public boolean eval(Node node) {
                return node.toComputer().isOffline() && labels.contains(node);
            }
        });

        if (!offlineNodesMatchingLabels.isEmpty()) {
            printHeading(build, listener.getLogger(), "Offline slaves", RED);
            forAllDo(offlineNodesMatchingLabels, new Closure<Node>() {
                public void exec(Node node) {
                    println(build, listener.getLogger(),
                            getLink(Jenkins.getInstance().getRootUrl() + node.toComputer().getUrl(), node.getDisplayName()) +
                                    getNodeDescription(node) +
                                    getLabelsHTML(node.getLabelString()) +
                                    offlineCause(node.toComputer().getOfflineCause()) + BR);

                    ReportContext.getInstance(build).addOfflineSlave(node);
                }
            });

        } else {
            printHeading(build, listener.getLogger(), "All nodes online", GREEN);
        }

        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Report status of slaves";
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

        public FormValidation doCheckLabels(@QueryParameter String labels) {
            try {
                Label.parseExpression(labels);
                return FormValidation.ok();
            } catch (ANTLRException e) {
                return FormValidation.error("There's a problem here:\n" + e.getLocalizedMessage());
            }
        }
    }

    private static class LabelToExpression implements Transformer {

        private static LabelToExpression INSTANCE = new LabelToExpression();

        public Object transform(Object input) {
            return ((LabelAtom) input).getExpression();
        }
    }
}
