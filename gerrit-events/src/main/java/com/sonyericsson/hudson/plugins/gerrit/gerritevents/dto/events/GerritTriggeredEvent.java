/*
 *  The MIT License
 *
 *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.events;

import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.CHANGE;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.PATCHSET;
import static com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventKeys.PATCH_SET;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sonyericsson.hudson.plugins.gerrit.gerritevents.GerritQueryException;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.GerritQueryHandler;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.attr.Account;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.attr.Change;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.attr.PatchSet;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.events.lifecycle.GerritEventLifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A DTO representation of a Gerrit triggered Event.
 * @author David Pursehouse &lt;david.pursehouse@sonyericsson.com&gt;
 */
public class GerritTriggeredEvent extends GerritEventLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GerritTriggeredEvent.class);

    /**
     * The Gerrit change the event is related to.
     */
    protected Change change;

    /**
     * Refers to a specific patchset within a change.
     */
    protected PatchSet patchSet;

    /**
     * The account that triggered the event.
     */
    protected Account account;

    /**
     * The changed files in this patchset.
     */
    protected List<String> files;

    /**
     * Takes a JSON object and fills its internal data-structure.
     * @param json the JSON Object.
     */
    public void fromJson(JSONObject json) {
        if (json.containsKey(CHANGE)) {
            change = new Change(json.getJSONObject(CHANGE));
        }
        if (json.containsKey(PATCH_SET)) {
            patchSet = new PatchSet(json.getJSONObject(PATCH_SET));
        } else if (json.containsKey(PATCHSET)) {
            patchSet = new PatchSet(json.getJSONObject(PATCHSET));
        }
    }

    /**
     * The account that triggered the event.
     * @return the account.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * The account that triggered the change.
     * @param account the account.
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * The Change.
     * @return the change.
     */
    public Change getChange() {
        return change;
    }

    /**
     * The Change.
     * @param change the change.
     */
    public void setChange(Change change) {
        this.change = change;
    }

    /**
     * The patchSet.
     * @return The patchSet.
     */
    public PatchSet getPatchSet() {
        return patchSet;
    }

    /**
     * The patchSet.
     * @param patchset the patchSet.
     */
    public void setPatchset(PatchSet patchset) {
        this.patchSet = patchset;
    }

    /**
     * Queries gerrit for the files included in this patch set.
     * @param gerritQueryHandler the query handler, responsible for the queries to gerrit.
     * @return a list of files that are part of this patch set.
     */
    public List<String> getFiles(GerritQueryHandler gerritQueryHandler) {
        if (files == null) {
            files = new LinkedList<String>();
            try {
                List<JSONObject> jsonList = gerritQueryHandler.queryFiles("change:" + getChange().getId());
                for (JSONObject json : jsonList) {
                    if (json.has("type") && "stats".equalsIgnoreCase(json.getString("type"))) {
                        continue;
                    }
                    if (json.has("currentPatchSet")) {
                        JSONObject currentPatchSet = json.getJSONObject("currentPatchSet");
                        if (currentPatchSet.has("files")) {
                            JSONArray changedFiles = currentPatchSet.optJSONArray("files");
                            for (int i = 0; i < changedFiles.size(); i++) {
                                JSONObject file = changedFiles.getJSONObject(i);
                                files.add(file.getString("file"));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("IOException occured. ", e);
            } catch (GerritQueryException e) {
                logger.error("Bad query. ", e);
            }
        }
        return files;
    }
}
