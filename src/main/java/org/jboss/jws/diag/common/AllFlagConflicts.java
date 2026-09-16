package org.jboss.jws.diag.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects options that have nothing to apply to when {@code --all} is set.
 *
 * <p>With {@code --all}, every instance's paths come from its own running process, so an
 * explicit path cannot be honoured. Commands reject the combination rather than silently
 * ignoring the option, and exit with {@link ExitCodes#TOOL_FAILURE}.
 *
 * <pre>
 * String conflict = AllFlagConflicts.when(all)
 *         .option("--catalina-home", catalinaHome)
 *         .option("--catalina-base", catalinaBase)
 *         .message();
 * </pre>
 */
public final class AllFlagConflicts {

    private final boolean all;
    private final List<String> conflicting = new ArrayList<>();

    private AllFlagConflicts(boolean all) {
        this.all = all;
    }

    public static AllFlagConflicts when(boolean all) {
        return new AllFlagConflicts(all);
    }

    /** Records {@code name} as conflicting if {@code --all} is set and the option was given. */
    public AllFlagConflicts option(String name, Object value) {
        if (all && value != null) {
            conflicting.add(name);
        }
        return this;
    }

    /** The error to print, or null when nothing conflicts with {@code --all}. */
    public String message() {
        if (conflicting.isEmpty()) {
            return null;
        }
        return "ERROR: " + joinNames() + " cannot be combined with --all. "
                + "Each instance's paths come from its own running process.";
    }

    private String joinNames() {
        int last = conflicting.size() - 1;
        if (last == 0) {
            return conflicting.get(0);
        }
        return String.join(", ", conflicting.subList(0, last)) + " and " + conflicting.get(last);
    }
}
