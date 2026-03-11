package com.theodo.springblueprint.common.infra.database.logging;

import com.theodo.springblueprint.Application;
import com.theodo.springblueprint.common.utils.StackTraceUtils;
import net.ttddyy.dsproxy.transform.QueryTransformer;
import net.ttddyy.dsproxy.transform.TransformInfo;

public class CommentingQueryTransformer implements QueryTransformer {

    private static final String FALLBACK_COMMENT = "not triggered within the application code";

    @Override
    public String transformQuery(TransformInfo transformInfo) {
        String sql = transformInfo.getQuery();
        if (sql.startsWith("/*")) {
            return sql;
        }

        StackTraceElement frame = StackTraceUtils.closestAppStackTraceElement(
            Application.BASE_PACKAGE_NAME,
            CommentingQueryTransformer.class
        );

        if (frame == null) {
            return "/* " + FALLBACK_COMMENT + " */ " + sql;
        }

        return "/* " + frame.getClassName() + "#" + frame.getMethodName() + ":" + frame.getLineNumber()
            + " */ " + sql;
    }
}
