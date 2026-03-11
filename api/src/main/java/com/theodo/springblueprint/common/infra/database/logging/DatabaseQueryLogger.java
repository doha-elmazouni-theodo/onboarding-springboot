package com.theodo.springblueprint.common.infra.database.logging;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import java.util.List;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseQueryLogger extends NoOpQueryExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger("DatabaseQueryLogger");
    private static final int FORMAT_CACHE_INITIAL_CAPACITY = 200; // ⚠️ this is NOT a max capacity !
    private static final ConcurrentMutableMap<String, String> formattedQueryCache = new ConcurrentHashMap<>(
        FORMAT_CACHE_INITIAL_CAPACITY
    );

    @Override
    @SuppressWarnings("PMD.UseEclipseCollectionsInNonPrivateMethods")
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        for (QueryInfo queryInfo : queryInfoList) {
            String formattedQuery = formatQuery(queryInfo.getQuery());
            logger.debug("{}", formattedQuery);
        }
    }

    private String formatQuery(String sql) {
        return formattedQueryCache.getIfAbsentPut(sql, () -> {
            logger.debug("Caching new formatted query");
            return SqlFormatter.format(sql);
        });
    }
}
