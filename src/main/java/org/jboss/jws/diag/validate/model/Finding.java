package org.jboss.jws.diag.validate.model;

import org.jboss.jws.diag.common.SeverityLevels;

public class Finding {
    private final String ruleId;
    private final String category;
    private final SeverityLevels severityLevels;
    private final String summary;
    private final String detail;
    private final String file;
    private final String fix;

    public Finding(String ruleId, String category, SeverityLevels severityLevels,
                   String summary, String detail, String file, String fix)
    {
        this.ruleId = ruleId;
        this.category = category;
        this.severityLevels = severityLevels;
        this.summary = summary;
        this.detail = detail;
        this.file = file;
        this.fix = fix;
    }

    public String getRuleId() {
        return ruleId;
    }
    public String getCategory() {
        return category;
    }
    public SeverityLevels getSeverity() {
        return severityLevels;
    }
    public String getSummary() {
        return summary;
    }
    public String getDetail() {
        return detail;
    }
    public String getFile() {
        return file;
    }
    public String getFix() {
        return fix;
    }
}
