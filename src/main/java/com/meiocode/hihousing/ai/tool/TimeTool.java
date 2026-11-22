package com.meiocode.hihousing.ai.tool;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import dev.langchain4j.agent.tool.Tool;

public class TimeTool {

    @Tool("getCurrentTime")
    public String getCurrentTimeInShanghai() {
        ZonedDateTime shanghaiTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        return shanghaiTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
