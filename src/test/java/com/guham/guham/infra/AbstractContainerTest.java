package com.guham.guham.infra;

import org.testcontainers.containers.MySQLContainer;

abstract public class AbstractContainerTest {
    static final MySQLContainer MY_SQL_CONTAINER;
    static{
        MY_SQL_CONTAINER = new MySQLContainer("mysql:8");
        MY_SQL_CONTAINER.start();
    }
}
