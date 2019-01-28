package com.javarush.task.logParser;

import com.javarush.task.logParser.query.QLQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LogParser implements QLQuery {
    private Path logDir;
    private SimpleDateFormat dateFormat;
    public LogParser(Path logDir) {
        this.logDir = logDir;
        dateFormat = new SimpleDateFormat("d.M.y H:m:s");
    }

    @Override //QLQuery Impl
    public Set<Object> execute(String query) {
        Set<Object> result = new HashSet<>();
        if (!query.startsWith("get")) return result;
        if (query.split(" ").length == 2) result = shortQuery(query); // ex.: get ip
        if (query.contains("=")) {                                        // ex.: get user for event = "DONE_TASK"
                                            // Общий формат запроса с параметром: get field1 for field2 = "value1"
            String[] queryBody = (Arrays.asList(query.split("=")).get(0).trim() ).split(" ") ;
            String queryAfterEquals = Arrays.asList(query.split("=")).get(1).trim();
            if ( !(queryBody[2].equals("for")) ) return result;
            if ( !queryAfterEquals.startsWith("\"") || !queryAfterEquals.endsWith("\"")) return result; //checking query syntax
            final String value1 = queryAfterEquals.replaceAll("\"", "") ;
            final  String field1 = queryBody[1];
//            System.out.println("get "+field1+" from "+queryBody[3]+" = "+value1); //for testing
// просматриваем записи в логе, если поле field2 имеет значение value1,то добавляем поле field1 в множество, которое затем будет возвращено методом execute
            if (queryBody[3].equals("id")) result.addAll( queryCasesToAdd(field1, value1, 0) ) ; // queryBody[3] is query field2, we give integer field2index parameter instead of it
            if (queryBody[3].equals("user")) result.addAll( queryCasesToAdd(field1, value1, 1) ) ;
            if (queryBody[3].equals("date")) result.addAll( queryCasesToAdd(field1, value1, 2) ) ;
            if (queryBody[3].equals("event")) result.addAll( queryCasesToAdd(field1, value1, 3) ) ;
            if (queryBody[3].equals("status")) result.addAll( queryCasesToAdd(field1, value1, 4) ) ;
        }
        return result;
    }



    private Set<Object> shortQuery(String query){
        Set<Object> result = new HashSet<>();
        switch (query.trim()) {
            case "get ip": // execute("get ip") класса LogParser должен возвращать множество (Set<String>) содержащее все уникальные IP адреса.
                result.addAll(getUniqueIPs(null, null));
                break;
            case "get user": //execute("get user") класса LogParser должен возвращать множество (Set<String>) содержащее всех уникальных пользователей.
                result.addAll(getAllUsers());
                break;
            case "get date": // execute("get date") класса LogParser должен возвращать множество (Set<Date>) содержащее все уникальные даты.
                result.addAll(getLines(null, null).stream()
                        .map(line -> Arrays.asList(line.split("\\t")).get(2))
                        .map(sDate -> {
                            try {
                                return dateFormat.parse(sDate);
                            } catch (ParseException ignore) {
                                return null;
                            }
                        })
                        .collect(Collectors.toSet()));
                break;
            case "get event": //  execute("get event") класса LogParser должен возвращать множество (Set<Event>) содержащее все уникальные события.
                result.addAll(getAllEvents(null, null));
                break;
            case "get status": // execute("get status") класса LogParser должен возвращать множество (Set<Status>) содержащее все уникальные статусы.
                result.addAll(getLines(null, null).stream()
                        .map(line -> Arrays.asList(line.split("\\t")).get(4))
                        .flatMap(line -> Arrays.stream(Status.values())
                                .filter(status -> status.toString().equals(line)))
                        .collect(Collectors.toSet()));
                break;
        }
        return result;
    }

    private Set<Object> queryCasesToAdd(String field1, String value1, int field2index) {
        Set<Object> result = new HashSet<>();
        switch (field1){
            case ("ip"):
                result.addAll( getLines(null,null).stream()
                        .filter(s -> Arrays.asList(s.split("\\t")).get(field2index).contains(value1))
                        .map(s -> Arrays.asList(s.split("\\t")).get(0))
                        .collect(Collectors.toSet()) );
                break;
            case ("user"):
                result.addAll( getLines(null,null).stream()
                        .filter(s -> Arrays.asList(s.split("\\t")).get(field2index).contains(value1))
                        .map(s -> Arrays.asList(s.split("\\t")).get(1))
                        .collect(Collectors.toSet()) );
                break;
            case ("date"):
                    result.addAll( getLines(null,null).stream()
                            .filter(s -> Arrays.asList(s.split("\\t")).get(field2index).contains(value1))
                            .map(s -> {
                                try {
                                    return dateFormat.parse( Arrays.asList(s.split("\\t")).get(2) );
                                } catch (ParseException ignore) {
                                    return null;
                                }
                            })
                            .collect(Collectors.toSet()) );
                break;
            case ("event"):
                result.addAll( getLines(null,null).stream()
                        .filter(s -> Arrays.asList(s.split("\\t")).get(field2index).contains(value1))
                        .map(s -> Event.valueOf( Arrays.asList(Arrays.asList(s.split("\\t")).get(3).split(" ")).get(0)  ) )
                        .collect(Collectors.toSet()) );
                break;
            case ("status"):
                result.addAll( getLines(null,null).stream()
                        .filter(s -> Arrays.asList(s.split("\\t")).get(field2index).contains(value1))
                        .map(s -> Event.valueOf( Arrays.asList(s.split("\\t")).get(4) ) )
                        .collect(Collectors.toSet()) );
                break;
        }
        return result;
    }



    public List<String> getLines(Date after, Date before) { // useful for all other methods
        List<String> result = new ArrayList<>();
        File[] files = logDir.toFile().listFiles();
        for (File file: files
        ) {
            if (file.getName().endsWith(".log"))
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    result.addAll( br.lines().filter(   line -> {
                        String sDate = Arrays.asList(line.split("\\t")).get(2);
                        try {
                            Date date = dateFormat.parse(sDate);
                            return  ( (after == null || !date.before(after)) && (before == null || !date.after(before)) );
                        } catch (ParseException ignore) {
                            return false;
                        }
                    }  ).collect(Collectors.toList()) );
                }catch (IOException e){
                    e.printStackTrace();
                }
        }
        return result;
    }


    public Set<String> getUniqueIPs(Date after, Date before) {
        return getLines(after, before).stream().map( line -> Arrays.asList(line.split("\\t")).get(0))
                .collect(Collectors.toSet());
    }

    public Set<String> getAllUsers() {
        //  getAllUsers() должен возвращать множество содержащее всех пользователей.
        return getLines(null, null).stream().map( line -> Arrays.asList(line.split("\\t")).get(1))
                .collect(Collectors.toSet());
    }

    public Set<Event> getAllEvents(Date after, Date before) {
        // getAllEvents(Date, Date) должен возвращать множество уникальных событий за выбранный период.
        return getLines(after, before).stream()
                .map(line -> Arrays.asList(line.split("\\t")).get(3))
                .map(line -> Arrays.asList(line.split(" ")).get(0))
                .flatMap(line -> Arrays.stream(Event.values())
                        .filter(event -> event.toString().equals(line)))
                .collect(Collectors.toSet());
    }

}