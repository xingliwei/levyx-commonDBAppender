package com.xlw.levyx.dblog;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by levyx on 2017-07-14
 */
public class DailyDBAppender extends AppenderSkeleton {

    private final Vector<Lite> buffer;

    private int bufferSize = 10;

    private String url;
    private String username;
    private String password;
    private String driverClass;

    private int initialSize = 5;
    private int maxTotal = 50;
    private int maxIdle = 8;
    private int maxWaitMillis = 5000;
    private int minIdle = 30000;

    private DataSource dataSource;

    private static Logger logger = Logger.getLogger(DailyDBAppender.class);

    public DailyDBAppender() {

        buffer = new Vector<>();

        Timer timer = new Timer();

        timer.schedule( new FlushTimerTask() , 10000, 3000 );
    }

    protected void append(LoggingEvent event) {

        String message = (String) event.getMessage();

        Lite lite = Lite.from(message);

        if(lite == null) {

            logger.warn("could not parse: + '"+ message +"'.");
            return ;
        }

        buffer.add(lite);

        if( buffer.size() >= bufferSize ) {

            (new Thread(this::flush)).start();
        }
    }

    private void flush() {

        if (dataSource == null) {
            dataSource = initDataSource(url,
                    driverClass,
                    username,
                    password,
                    initialSize,
                    maxTotal,
                    maxIdle,
                    maxWaitMillis,
                    minIdle);
        }

        Calendar calendar = Calendar.getInstance();

        synchronized (buffer) {

            try (Connection connection = dataSource.getConnection()) {

                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                String tableName = "performance_log_" + dayOfMonth;

                String sql = "insert into "+ tableName +" (tag, start, duration) values (?, ?, ?)";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    int size = buffer.size();
                    Iterator<Lite> iterator = buffer.iterator();

                    while(iterator.hasNext()) {

                        Lite lite = iterator.next();

                        statement.setString(1, lite.getName());
                        statement.setLong(2, lite.getStart());
                        statement.setInt(3, lite.getDuration());

                        statement.executeUpdate();

                        iterator.remove();
                    }

                    logger.info("flush "+ size +" lites to db.\t\t\t\t\t" + buffer.size());
                }

            } catch (SQLException e) {
                e.printStackTrace();
                logger.warn("could not write dblog message to db. " + e.getMessage());
            }
        }

    }

    public void close() {  }

    public boolean requiresLayout() {
        return false;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private DataSource initDataSource(String url,
                                      String driverClass,
                                      String username,
                                      String password,
                                      int initialSize,
                                      int maxTotal,
                                      int maxIdle,
                                      int maxWaitMillis,
                                      int minIdle) {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(url);
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxTotal(maxTotal);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMaxWaitMillis(maxWaitMillis);
        dataSource.setMinIdle(minIdle);

        return dataSource;
    }

    private final class FlushTimerTask extends TimerTask {

        @Override
        public void run() {

            logger.debug("start to flush lites to db.");

            if(buffer.size() > 0) {

                flush();

            }

        }
    }
}
