package com.javarush.task.classLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/* 
classLoader - что это такое?
*/
public class Solution {
    public static void main(String[] args)  {
        Set<? extends Animal> allAnimals = getAllAnimals(Solution.class.getProtectionDomain().getCodeSource().getLocation().getPath() + Solution.class.getPackage().getName().replaceAll("[.]", "/") + "/data");
        System.out.println(allAnimals);
    }

    public static Set<? extends Animal> getAllAnimals(String pathToAnimals)  {
        return  Arrays.stream(new File(pathToAnimals).listFiles())
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".class"))
                .map( f -> new FileToClass().load(f) )
                .filter(Animal.class::isAssignableFrom)
                .map(c -> {
                    try {
                        return c.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException ignore) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(o -> (Animal)o).collect(Collectors.toSet());
    }

    public static class FileToClass extends ClassLoader {
        public Class<?> load(File file) {
            try {
                byte[] b = Files.readAllBytes(file.toPath());
                return defineClass(null, b, 0, b.length); //Converts an array of bytes into an instance of class Class.
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
