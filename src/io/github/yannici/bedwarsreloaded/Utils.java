package io.github.yannici.bedwarsreloaded;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Utils {

    public static String implode(String glue, ArrayList<String> strings) {
        if(strings.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(strings.remove(0));

        for(String str : strings) {
            builder.append(glue);
            builder.append(str);
        }

        return builder.toString();
    }
    
    public static boolean isNumber(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    
    public static Method getColorableMethod(Material mat) {
        try {
            ItemStack tempStack = new ItemStack(mat, 1);
            Method method = tempStack.getItemMeta().getClass().getMethod("setColor", new Class[]{Color.class});
            if(method != null) {
                return method;
            }
        } catch(Exception ex) {
            // it's no error
        }
        
        return null;
    }
    
    public static boolean checkBungeePlugin()
	  {
	    try
	    {
	      Class.forName("net.md_5.bungee.BungeeCord");
	      return true;
	    }
	    catch (Exception e) {}
	    
	    return false;
	  }
    
    @SuppressWarnings("resource")
	public static String[] getResourceListing(Class<?> clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
          /* A file path: easy enough */
          return new File(dirURL.toURI()).list();
        } 

        if (dirURL == null) {
          /* 
           * In case of a jar file, we can't actually find a directory.
           * Have to assume the same jar as clazz.
           */
          String me = clazz.getName().replace(".", "/")+".class";
          dirURL = clazz.getClassLoader().getResource(me);
        }
        
        if (dirURL.getProtocol().equals("jar")) {
          /* A JAR path */
          String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
          JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
          Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
          Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
          while(entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith(path)) { //filter according to the path
              String entry = name.substring(path.length());
              int checkSubdir = entry.indexOf("/");
              if (checkSubdir >= 0) {
                // if it is a subdirectory, we just return the directory name
                entry = entry.substring(0, checkSubdir);
              }
              result.add(entry);
            }
          }
          return result.toArray(new String[result.size()]);
        } 
          
        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }

}
