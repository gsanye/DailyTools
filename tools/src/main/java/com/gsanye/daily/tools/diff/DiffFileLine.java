package com.gsanye.daily.tools.diff;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DiffFileLine {
    public static final String p1 = "";
    public static final String p2 = "";

    public static void main(String[] args) throws IOException {
        FileContext fc1 = new FileContext(p1).init();
        FileContext fc2 = new FileContext(p2).init();
        fc1.diff(fc2);
        fc2.diff(fc1);
        logResult(fc1, fc2);
    }

    private static void diff2Collections(FileContext fc1, FileContext fc2) {
        Set<String> c1 = fc1.lineSet;
        Set<String> diffSet = new HashSet<>(c1);
        Set<String> c2 = fc2.lineSet;
        diffSet.removeAll(c2);
        log.info("diff2Collections collection1:{},collection2:{},diffSet:{}", c1.size(), c2.size(), diffSet.size());
        fc1.diffSet = diffSet;

    }

    private static void readLineSkipEmpty(FileContext fileContext) throws IOException {
        String path = fileContext.fileName;
        File file = new File(path);
        List<String> strings = FileUtils.readLines(file);
        List<String> result = strings.stream()
                .map(s -> StringUtils.replace(s, " ", ""))
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        log.info("readLineSkipEmpty readLineSkipEmpty:{},result:{}", path, result.size());
        fileContext.simpleName = file.getName();
        fileContext.lines = result;
    }

    private static FileContext list2Set(FileContext fileContext) {
        List<String> source = fileContext.lines;
        Set<String> set = new HashSet<>(source.size());
        Set<String> repeatSet = new HashSet<>(source.size());
        for (String s : source) {
            if (set.contains(s)) {
                repeatSet.add(s);
            } else {
                set.add(s);
            }
        }
        log.info("list2Set source:{},set:{},repeatSet:{},repeatSet:{}", source.size(), set.size(), repeatSet.size(), JSON.toJSONString(repeatSet));
        fileContext.repeatSet = repeatSet;
        fileContext.lineSet = set;
        return fileContext;
    }

    private static void logResult(FileContext fc1, FileContext fc2) {
        String name1 = StringUtils.substringBeforeLast(fc1.simpleName, ".");
        String name2 = StringUtils.substringBeforeLast(fc2.simpleName, ".");
        log.info("{} 原始数据:{},去重后数量:{},重复数量:{},重复集合:{}", name1, fc1.lines.size(), fc1.lineSet.size(), fc1.repeatSet.size(), JSON.toJSONString(fc1.repeatSet));
        log.info("{} 原始数据:{},去重后数量:{},重复数量:{},重复集合:{}", name2, fc2.lines.size(), fc2.lineSet.size(), fc2.repeatSet.size(), JSON.toJSONString(fc2.repeatSet));
        log.info("{}-{} 差集数量:{},差集内容:{}", name1, name2, fc1.diffSet.size(), JSON.toJSONString(fc1.diffSet));
        log.info("{}-{} 差集数量:{},差集内容:{}", name2, name1, fc2.diffSet.size(), JSON.toJSONString(fc2.diffSet));
    }

    private static class FileContext {

        private String fileName;
        private String simpleName;
        private List<String> lines;
        private Set<String> lineSet;
        private Set<String> repeatSet;
        private Set<String> diffSet;

        public FileContext(String fileName) {
            this.fileName = fileName;
        }

        public FileContext init() throws IOException {
            readLineSkipEmpty(this);
            list2Set(this);
            return this;
        }

        public void diff(FileContext other) {
            diff2Collections(this, other);
        }
    }

}
