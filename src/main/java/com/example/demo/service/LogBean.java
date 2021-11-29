package com.example.demo.service;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogBean {
    private PersonIdent authorIdent;
    private PersonIdent committerIdent;
    private String branchNameSHA1;
    private ObjectId id;
    private int commitTime;
    private String fullMessage;
    private String shortMessage;

    public LogBean(PersonIdent authorIdent, PersonIdent committerIdent, String branchNameSHA1, ObjectId id, int commitTime, String fullMessage, String shortMessage) {
        this.authorIdent = authorIdent;
        this.committerIdent = committerIdent;
        this.branchNameSHA1 = branchNameSHA1;
        this.id = id;
        this.commitTime = commitTime;
        this.fullMessage = fullMessage;
        this.shortMessage = shortMessage;
    }

    public PersonIdent getAuthorIdent() {
        return authorIdent;
    }

    public void setAuthorIdent(PersonIdent authorIdent) {
        this.authorIdent = authorIdent;
    }

    public PersonIdent getCommitterIdent() {
        return committerIdent;
    }

    public void setCommitterIdent(PersonIdent committerIdent) {
        this.committerIdent = committerIdent;
    }

    public String getBranchNameSHA1() {
        return branchNameSHA1;
    }

    public void setBranchNameSHA1(String branchNameSHA1) {
        this.branchNameSHA1 = branchNameSHA1;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }

    public String getFullMessage() {
        return fullMessage;
    }

    public void setFullMessage(String fullMessage) {
        this.fullMessage = fullMessage;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getAuthorTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(authorIdent.getWhen());
    }

    public String getAuthorEmail() {
        return authorIdent.getEmailAddress();
    }

    public String getCommitterTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(committerIdent.getWhen());
    }

    public String getCommitterEmail() {
        return committerIdent.getEmailAddress();
    }
}
