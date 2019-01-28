package com.javarush.task.logParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Solution {
    public static void main(String[] args) throws IOException {

        String logdir = System.getProperty("user.dir")+"\\src\\com\\javarush\\task\\logParser\\logs" ;
        LogParser logParser = new LogParser(Paths.get(logdir));

        String query = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            while (!query.equals("end")){
                query = reader.readLine();
                logParser.execute(query).forEach(System.out::println);
            }

        }

    }
}

/*

It`s only demo version!
Big task from "Javarush" was shorted by me
to make the code more understandable and easier to review.

App logic is contained inside "LogParser" class.
Log files should be in "logs" directory.

query examples:
get date for user = "Amigo"
get user for event = "LOGIN"
get ip
get status

query to exit the program:
end

 */