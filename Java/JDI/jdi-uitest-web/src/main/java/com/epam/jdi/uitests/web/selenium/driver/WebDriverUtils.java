package com.epam.jdi.uitests.web.selenium.driver;
/*
 * Copyright 2004-2016 EPAM Systems
 *
 * This file is part of JDI project.
 *
 * JDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JDI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JDI. If not, see <http://www.gnu.org/licenses/>.
 */


import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.os.WindowsUtils;

import java.util.Map;

import static com.epam.commons.LinqUtils.first;
import static com.epam.commons.LinqUtils.where;
import static com.epam.commons.TryCatchUtil.tryGetResult;

/**
 * Created by 12345 on 26.01.2015.
 */
public final class WebDriverUtils {
    private WebDriverUtils() { }
    private static final String KILL = "taskkill /F /IM ";
    public static void killAllRunWebDrivers() {
        try {
     /*       String line;
            List<String> list = new ArrayList<>();
            Process p = Runtime.getRuntime().exec
                    (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                list.add(line);
            }
            String s =first(where(list, el -> el.contains("firefox") && el.contains("-foreground")
                    || el.contains("chromedriver")
                    || el.contains("IEDriverServer")));
            //for ( String serviceName : list)
             //   Runtime.getRuntime().exec(KILL + serviceName);*/
            String pid = getPid();
            while (pid != null) {
                killPID(pid);
                pid = getPid();
            }
        } catch (Exception ignore) {
            // Ignore in case of not windows Operation System or any other errors
        }
    }

    private static String getPid() {
        return first(where((Map<String, String>) tryGetResult(WindowsUtils::procMap), el -> el.getKey() != null
                && (el.getKey().contains("firefox") && el.getKey().contains("-foreground")
                || el.getKey().contains("chromedriver")
                || el.getKey().contains("IEDriverServer"))));
    }

    private static void killPID(String processID) {
        new CommandLine("taskkill", "/f", "/t", "/pid", processID).execute();
    }
}