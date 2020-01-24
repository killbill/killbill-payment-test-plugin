package org.killbill.billing.plugin.payment;

import org.killbill.billing.platform.test.PlatformDBTestingHelper;
import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.payment.dao.PaymentTestDao;
import org.killbill.commons.embeddeddb.EmbeddedDB;

import java.io.IOException;
import java.sql.SQLException;

public class EmbeddedDbHelper {

    private static final String DDL_FILE_NAME = "ddl.sql";

    private static final EmbeddedDbHelper INSTANCE = new EmbeddedDbHelper();
    private              EmbeddedDB       embeddedDB;

    public static EmbeddedDbHelper instance() {
        return INSTANCE;
    }

    public void startDb() throws Exception {
        this.embeddedDB = PlatformDBTestingHelper.get().getInstance();
        this.embeddedDB.initialize();
        this.embeddedDB.start();

        final String databaseSpecificDDL = "ddl-" + this.embeddedDB.getDBEngine().name().toLowerCase() + ".sql";
        try {
            this.embeddedDB.executeScript(TestUtils.toString(databaseSpecificDDL));
        }
        catch (final IllegalArgumentException e) {
            // Ignore, no engine specific DDL
        }

        final String ddl = TestUtils.toString(DDL_FILE_NAME);
        this.embeddedDB.executeScript(ddl);
        this.embeddedDB.refreshTableNames();
    }

    public PaymentTestDao getPaymentTestDao() throws IOException, SQLException {
        return new PaymentTestDao(this.embeddedDB.getDataSource());
    }

    public void resetDB() throws Exception {
        this.embeddedDB.cleanupAllTables();
    }

    public void stopDB() throws Exception {
        this.embeddedDB.stop();
    }
}
