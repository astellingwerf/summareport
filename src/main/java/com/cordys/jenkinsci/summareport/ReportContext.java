package com.cordys.jenkinsci.summareport;

import hudson.model.AbstractBuild;
import hudson.model.InvisibleAction;
import hudson.model.Job;
import hudson.model.Node;

import java.util.*;

public class ReportContext extends InvisibleAction {
    StringBuilder sb = new StringBuilder();

    Set<String> offlineSlaves = new HashSet<String>();
    Set<String> failingJobs = new HashSet<String>();
    Map<String, Integer> unstableJobs = new HashMap<String, Integer>();

    public static ReportContext getInstance(AbstractBuild<?, ?> b) {
        ReportContext action = b.getAction(ReportContext.class);
        if (action == null) {
            action = new ReportContext();
            b.addAction(action);
        }
        return action;
    }

    public ReportContext append(String text) {
        sb.append(text);
        return this;
    }

    public String getText() {
        sb.trimToSize();
        return sb.toString();
    }

    public void addOfflineSlave(Node n)
    {
        offlineSlaves.add(n.getNodeName());
    }

    public Set<String> getOfflineSlaves() {
        return Collections.unmodifiableSet(offlineSlaves);
    }

    public Set<String> getFailingJobs() {
        return Collections.unmodifiableSet(failingJobs);
    }

    public Map<String, Integer> getUnstableJobs() {
        return Collections.unmodifiableMap(unstableJobs);
    }

    public void addFailingJob(Job job) {
        failingJobs.add(job.getName());
    }
    public void addUnstableJob(Job job, int failingCases) {
        unstableJobs.put(job.getName(), failingCases);
    }
}
