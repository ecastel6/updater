package app.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return simpleDateFormat.format(new Date(record.getMillis())) + "::" +
                record.getSourceClassName() + "::"
                + record.getSourceMethodName() + "::"
                + record.getMessage() + "\n";
    }

}