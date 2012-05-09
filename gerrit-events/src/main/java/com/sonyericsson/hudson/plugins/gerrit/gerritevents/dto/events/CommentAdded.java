/*
 *  The MIT License
 *
 *  Copyright 2012 Hewlett-Packard Development Company, L.P.
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

import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritEventType;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.dto.GerritJsonEvent;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DTO representation of the comment-added Gerrit Event.
 * @author James E. Blair &lt;jeblair@hp.com&gt;
 */
public class CommentAdded extends GerritTriggeredEvent implements GerritJsonEvent {
    private static final Logger logger = LoggerFactory.getLogger(CommentAdded.class);
    private JSONArray approvals;

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.COMMENT_ADDED;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    /**
     * Check the comment added matches the trigger set.
     * @param category gerrit short text category to match
     * @param value value to test
     * @return true if matches
     */
    public boolean matchesApproval(String category, String value) {
        for (int i = 0; i < approvals.size(); i++) {
            logger.debug("approval");
            JSONObject approval = approvals.getJSONObject(i);
            if (approval.containsKey("type") && approval.containsKey("value")) {
                String apptype = approval.getString("type");
                String appvalue = approval.getString("value");
                logger.debug(apptype);
                logger.debug(appvalue);
                if (apptype.equals(category) && appvalue.equals(value)) {
                    logger.debug("approved");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        logger.debug("from json");
        if (json.containsKey("approvals")) {  ///TODO: constant
            logger.debug("approvals");
            this.approvals = json.getJSONArray("approvals");
        }
    }

}
