package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.YearMonth;

/**
 * Date: 2019-05-20
 * Time: 09:33
 *
 * @author jonmark
 */
public class CreateProratedMonthRevenuesTask extends AreaTaskImpl<Object> {
    private final YearMonth month;

    CreateProratedMonthRevenuesTask(YearMonth month) {
        this.month = month;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: create ProratedMonthRevenue records for each active type
        for (ProratedRevenueType revenueType : ProratedRevenueType.ACTIVE_TYPES) {
            // jw: first' let's create the ProratedMonthRevenue and allow it to create its wallet.
            ProratedMonthRevenue monthRevenue = new ProratedMonthRevenue(revenueType, month);

            // jw: Save the ProratedMonthRevenue object itself which should cascade save the wallet.
            ProratedMonthRevenue.dao().save(monthRevenue);
        }

        return null;
    }
}
