package edu.stanford.protege.gateway;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MappingHelper {

    public static Date getClosestToTodayDate(List<Date> dates) {
        if(dates != null && !dates.isEmpty()) {
            return dates.stream().filter(Objects::nonNull)
                    .sorted().toList().get(0);
        }

        return null;
    }

}
