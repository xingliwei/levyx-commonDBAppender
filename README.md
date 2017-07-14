## database appender for `log4j`

**specially for `per4j`**

### How to use?

- add the follow xml config in your `log4j.xml`

```
<appender name="db_appender" class="DailyDBAppender">
    <param name="username" value="YOUR DATABASE USERNAME" />
    <param name="password" value="YOUR DATABASE PASSWORD" />
    <param name="url" value="YOUR DATABASE JDBC CONNECTION STRING" />
    <param name="driverClass" value="YOUR DATABASE JDBC DRIVER" />
</appender>
<logger name="org.perf4j.TimingLogger" additivity="false">
    <level value="INFO" />
    <appender-ref ref="db_appender" />
</logger>
```

- init your `performance_log` table structure in database with `init.sql` file


### Have fun!
